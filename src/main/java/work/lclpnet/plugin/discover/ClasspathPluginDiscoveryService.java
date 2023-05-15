package work.lclpnet.plugin.discover;

import org.slf4j.Logger;
import work.lclpnet.plugin.load.ClassLoaderContainer;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.UrlLoadablePlugin;
import work.lclpnet.plugin.manifest.JsonManifestLoader;
import work.lclpnet.plugin.manifest.ManifestLoadException;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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
        } else {
            return Optional.empty();
        }

        final State state = new State();

        if (!isPlugin(urls, state) || state.manifest == null) {
            return Optional.empty();
        }

        return Optional.of(new UrlLoadablePlugin(state.manifest, urls, src, classLoaderContainer));
    }

    private Path findFile(URL[] urls, Path query) throws URISyntaxException {
        for (URL url : urls) {
            Path base = Path.of(url.toURI());
            Path path = base.resolve(query);

            if (Files.isRegularFile(path)) {
                return path;
            }
        }

        return null;
    }

    private boolean isPlugin(URL[] classpath, State state) {
        Path manifestPath;
        try {
            manifestPath = findFile(classpath, Path.of("plugin.json"));
        } catch (URISyntaxException e) {
            logger.error("Invalid classpath url", e);
            return false;
        }

        if (manifestPath == null) {
            logger.warn("Classpath {} does not contain a plugin.json", Arrays.toString(classpath));
            return false;
        }

        try (InputStream in = Files.newInputStream(manifestPath)) {
            state.manifest = this.manifestLoader.load(in);
            return true;  // the manifest loader succeeded. thus, the manifest is valid
        } catch (ManifestLoadException e) {
            logger.warn("Invalid plugin manifest at %s".formatted(manifestPath), e);
            return false;  // manifest is invalid
        } catch (IOException e) {
            logger.warn("Failed to open plugin manifest at %s".formatted(manifestPath), e);
            return false;
        }
    }

    private static class State {
        private PluginManifest manifest = null;
    }
}
