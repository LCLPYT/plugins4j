package work.lclpnet.plugin.discover;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.load.DefaultClassLoaderContainer;
import work.lclpnet.plugin.mock.TestManifestLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryPluginDiscoveryServiceTest {

    @Test
    void discover_one_isFound() throws IOException {
        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var logger = LogManager.getLogger();
        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, logger);

            assertEquals(1, discovery.discover().count());
        }
    }

    @Test
    void discoverFrom_one_isFound() throws IOException {
        var testPluginsDir = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(testPluginsDir));

        var logger = LogManager.getLogger();
        var manifestLoader = new TestManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new DirectoryPluginDiscoveryService(testPluginsDir, manifestLoader, clContainer, logger);

            var pluginPath = testPluginsDir.resolve("testPlugin-1.0.0.jar");
            assertTrue(Files.isRegularFile(pluginPath));

            assertTrue(discovery.discoverFrom(pluginPath).isPresent());
        }
    }
}