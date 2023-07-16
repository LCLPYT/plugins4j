package work.lclpnet.plugin.load;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.DistinctPluginContainer;
import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.discover.ClasspathPluginDiscoveryService;
import work.lclpnet.plugin.manifest.JsonManifestLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginClassLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoaderTest.class);

    @Test
    void testSharedResources() throws IOException {
        var pluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(pluginsDir));

        var pluginContainer = new DistinctPluginContainer(logger);

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new ClasspathPluginDiscoveryService(List.of(), new JsonManifestLoader(), clContainer, logger);

            LoadablePlugin providerLoadable = discovery.discoverFrom(pluginsDir.resolve("providerPlugin.jar")).orElseThrow();
            LoadablePlugin testLoadable = discovery.discoverFrom(pluginsDir.resolve("testPlugin.jar")).orElseThrow();

            LoadedPlugin provider = pluginContainer.loadPlugin(providerLoadable).orElseThrow();
            pluginContainer.loadPlugin(testLoadable).orElseThrow();

            PluginClassLoader classLoader = (PluginClassLoader) provider.getPlugin().getClass().getClassLoader();
            var iterator = classLoader.getResources("META-INF/services/work.lclpnet.provider.spi.TestServiceProvider").asIterator();
            var urls = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false).toList();

            assertEquals(1, urls.size());
        }
    }

    @Test
    void testCrossSPI() throws IOException, ReflectiveOperationException {
        var pluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(pluginsDir));

        var pluginContainer = new DistinctPluginContainer(logger);

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new ClasspathPluginDiscoveryService(List.of(), new JsonManifestLoader(), clContainer, logger);

            LoadablePlugin providerLoadable = discovery.discoverFrom(pluginsDir.resolve("providerPlugin.jar")).orElseThrow();
            LoadablePlugin testLoadable = discovery.discoverFrom(pluginsDir.resolve("testPlugin.jar")).orElseThrow();

            LoadedPlugin provider = pluginContainer.loadPlugin(providerLoadable).orElseThrow();
            pluginContainer.loadPlugin(testLoadable);

            invokeSpi(provider.getPlugin());
        }
    }

    private static void invokeSpi(Plugin provider) throws ReflectiveOperationException {
        PluginClassLoader classLoader = (PluginClassLoader) provider.getClass().getClassLoader();

        String className = "work.lclpnet.provider.spi.TestServiceManager";

        var managerClass = Class.forName(className, false, classLoader);
        var constructor = managerClass.getConstructor();
        var method = managerClass.getMethod("services");

        var manager = constructor.newInstance();
        Stream<?> services = (Stream<?>) method.invoke(manager);

        // expecting one service provided by testPlugin.jar
        assertEquals(1, services.count());
    }
}