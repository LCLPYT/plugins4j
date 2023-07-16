package work.lclpnet.plugin.discover;

import org.slf4j.Logger;
import work.lclpnet.plugin.load.ClassLoaderContainer;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.UrlLoadablePlugin;
import work.lclpnet.plugin.manifest.JsonManifestLoader;
import work.lclpnet.plugin.manifest.ManifestLoadException;
import work.lclpnet.plugin.manifest.PluginManifest;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class ClasspathPluginDiscoveryService implements PluginDiscoveryService {

    private final List<URL[]> classPaths;
    private final JsonManifestLoader manifestLoader;
    private final ClassLoaderContainer classLoaderContainer;
    private final Logger logger;

    public ClasspathPluginDiscoveryService(List<URL[]> classPaths, JsonManifestLoader manifestLoader,
                                           ClassLoaderContainer classLoaderContainer, Logger logger) {
        this.classPaths = classPaths;
        this.manifestLoader = manifestLoader;
        this.classLoaderContainer = classLoaderContainer;
        this.logger = logger;
    }

    @Override
    public Stream<? extends LoadablePlugin> discover() throws IOException {
        Set<LoadablePlugin> plugins = new HashSet<>();

        for (URL[] classPath : classPaths) {
            var plugin = discoverFrom(classPath);
            plugin.ifPresent(plugins::add);
        }

        return plugins.stream();
    }

    @Override
    public Optional<? extends LoadablePlugin> discoverFrom(Object src) throws IOException {
        final URL[] urls;

        if (src instanceof URL[] urlsSrc) {
            urls = urlsSrc;
        } else if (src instanceof Path[] pathsSrc) {
            urls = new URL[pathsSrc.length];

            for (int i = 0; i < pathsSrc.length; i++) {
                urls[i] = pathsSrc[i].toUri().toURL();
            }
        } else if (src instanceof File[] files) {
            urls = new URL[files.length];

            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        } else if (src instanceof URL url) {
            urls = new URL[] {url};
        } else if (src instanceof Path path) {
            urls = new URL[] {path.toUri().toURL()};
        } else if (src instanceof File file) {
            urls = new URL[] {file.toURI().toURL()};
        } else {
            return Optional.empty();
        }

        final State state = new State();

        if (!isPlugin(urls, state) || state.manifest == null) {
            return Optional.empty();
        }

        return Optional.of(new UrlLoadablePlugin(state.manifest, urls, src, classLoaderContainer));
    }

    @Nullable
    private Map.Entry<URL, InputStream> findFile(URL[] urls, Path query) throws URISyntaxException, IOException {
        for (URL url : urls) {
            InputStream in = findFile(url, query);

            if (in != null) {
                return Map.entry(url, in);
            }
        }

        return null;
    }

    @Nullable
    private InputStream findFile(URL url, Path query) throws URISyntaxException, IOException {
        if ("file".equals(url.getProtocol())) {
            Path base = Path.of(url.toURI());

            if (Files.isDirectory(base)) {
                Path path = base.resolve(query);

                if (Files.isRegularFile(path)) {
                    return Files.newInputStream(path);
                }

                return null;
            }
        }

        // assume jar file
        final String queryName = query.toString().replace(File.separator, "/");

        JarInputStream in = new JarInputStream(url.openStream());
        ZipEntry entry;

        while ((entry = in.getNextEntry()) != null) {
            if (queryName.equals(entry.getName())) {
                return in;
            }
        }

        in.close();

        return null;
    }

    private boolean isPlugin(URL[] classpath, State state) {
        Map.Entry<URL, InputStream> manifestInput;
        try {
            manifestInput = findFile(classpath, Path.of("plugin.json"));
        } catch (URISyntaxException | IOException e) {
            logger.error("Invalid classpath url", e);
            return false;
        }

        if (manifestInput == null) {
            logger.warn("Classpath {} does not contain a plugin.json", Arrays.toString(classpath));
            return false;
        }

        try (InputStream in = manifestInput.getValue()) {
            state.manifest = this.manifestLoader.load(in);
            return true;  // the manifest loader succeeded. thus, the manifest is valid
        } catch (ManifestLoadException e) {
            logger.warn("Invalid plugin manifest at %s".formatted(manifestInput.getKey()), e);
            return false;  // manifest is invalid
        } catch (IOException e) {
            logger.warn("Failed to open plugin manifest at (%s, %s)".formatted(manifestInput.getKey(), "plugin.json"), e);
            return false;
        }
    }

    private static class State {
        private PluginManifest manifest = null;
    }
}
