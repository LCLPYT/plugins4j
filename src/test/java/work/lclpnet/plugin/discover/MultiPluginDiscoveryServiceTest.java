package work.lclpnet.plugin.discover;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.load.DefaultClassLoaderContainer;
import work.lclpnet.plugin.load.UrlLoadablePlugin;
import work.lclpnet.plugin.mock.TestLoadablePlugin;
import work.lclpnet.plugin.mock.TestManifestLoader;
import work.lclpnet.plugin.mock.TestPluginDiscovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiPluginDiscoveryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    @Test
    void discover_multiple_all() throws IOException {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discoveryA = new TestPluginDiscovery(pluginA);

        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discoveryB = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, LOGGER);

            var multiDiscovery = new MultiPluginDiscoveryService(discoveryA, discoveryB);
            var plugins = multiDiscovery.discover().toList();

            assertEquals(DirectoryPluginDiscoveryServiceTest.EXPECTED_PLUGINS + 1, plugins.size());
        }
    }

    @Test
    void discoverFrom_multiple_single() throws IOException {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discoveryA = new TestPluginDiscovery(pluginA);

        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discoveryB = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, LOGGER);

            var multiDiscovery = new MultiPluginDiscoveryService(discoveryA, discoveryB);

            assertEquals(pluginA, multiDiscovery.discoverFrom("pluginA").orElseThrow());
            assertTrue(multiDiscovery.discoverFrom(testPluginsDir.resolve("testPlugin.jar")).orElseThrow() instanceof UrlLoadablePlugin);
        }
    }
}