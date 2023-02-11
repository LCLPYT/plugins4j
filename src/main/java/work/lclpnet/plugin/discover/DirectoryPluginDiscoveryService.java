package work.lclpnet.plugin.discover;

import org.slf4j.Logger;
import work.lclpnet.plugin.load.ClassLoaderContainer;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.UrlLoadablePlugin;
import work.lclpnet.plugin.manifest.ManifestLoadException;
import work.lclpnet.plugin.manifest.PluginManifest;
import work.lclpnet.plugin.manifest.PluginManifestLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class DirectoryPluginDiscoveryService implements PluginDiscoveryService {

    private final Path directory;
    private final PluginManifestLoader manifestLoader;
    private final ClassLoaderContainer classLoaderContainer;
    private final Logger logger;

    public DirectoryPluginDiscoveryService(Path directory, PluginManifestLoader manifestLoader,
                                           ClassLoaderContainer classLoaderContainer, Logger logger) {
        this.directory = directory;
        this.manifestLoader = manifestLoader;
        this.classLoaderContainer = classLoaderContainer;
        this.logger = logger;
    }

    @Override
    public Stream<LoadablePlugin> discover() throws IOException {
        Set<LoadablePlugin> plugins = new HashSet<>();

        try (var files = Files.list(directory)) {
            var paths = files.collect(Collectors.toUnmodifiableSet());

            for (var path : paths) {
                var plugin = discoverFrom(path);
                plugin.ifPresent(plugins::add);
            }
        }

        return plugins.stream();
    }

    @Override
    public Optional<LoadablePlugin> discoverFrom(Object src) throws IOException {
        // this service only handles paths by now
        if (!(src instanceof Path path)) {
            return Optional.empty();
        }

        State state = new State();
        if (!isPlugin(path, state) || state.manifest == null) {
            return Optional.empty();
        }

        var url = path.toUri().toURL();

        return Optional.of(new UrlLoadablePlugin(state.manifest, url, src, classLoaderContainer));
    }

    private boolean isPlugin(Path path, State state) {
        if (!Files.isRegularFile(path) || !path.toString().endsWith(".jar")) return false;

        try (JarFile file = new JarFile(path.toFile())) {

            ZipEntry entry = file.getEntry("plugin.json");
            if (entry == null) return false;  // manifest does not exist

            try (InputStream in = file.getInputStream(entry)) {
                state.manifest = this.manifestLoader.load(in);
                return true;  // the manifest loader succeeded. thus, the manifest is valid
            } catch (ManifestLoadException e) {
                return false;  // manifest is invalid
            } catch (IOException e) {
                logger.warn("Failed to open plugin manifest in jar %s".formatted(path), e);
                return false;
            }
        } catch (IOException e) {
            logger.warn("Failed to open plugin jar at %s".formatted(path), e);
            return false;
        }
    }

    private static class State {
        private PluginManifest manifest = null;
    }
}
