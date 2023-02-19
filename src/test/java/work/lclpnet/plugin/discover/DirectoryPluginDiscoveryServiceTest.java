package work.lclpnet.plugin.discover;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.load.DefaultClassLoaderContainer;
import work.lclpnet.plugin.mock.TestManifestLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryPluginDiscoveryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    @Test
    void discover_one_isFound() throws IOException {
        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, LOGGER);

            assertEquals(1, discovery.discover().count());
        }
    }

    @Test
    void discoverFrom_one_isFound() throws IOException {
        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, LOGGER);

            var pluginPath = testPluginsDir.resolve("testPlugin.jar");
            assertTrue(Files.isRegularFile(pluginPath));

            assertTrue(discovery.discoverFrom(pluginPath).isPresent());
        }
    }
}