package work.lclpnet.plugin.load;

import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.manifest.JsonManifestLoader;
import work.lclpnet.plugin.manifest.PluginManifest;
import work.lclpnet.plugin.mock.TestManifestLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class UrlLoadablePluginTest {

    @Test
    void load_testPlugin_succeeds() throws MalformedURLException {
        var testPlugin = Path.of("src/test/resources/plugins/testPlugin-1.0.0.jar");
        assertTrue(Files.isRegularFile(testPlugin));

        var manifest = TestManifestLoader.manifest(
                "testPlugin",
                "work.lclpnet.testPlugin.TestPlugin",
                Collections.emptySet()
        );

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var loadable = new UrlLoadablePlugin(manifest, testPlugin.toUri().toURL(), testPlugin, clContainer);

            var loaded = loadable.load();
            assertNotNull(loaded);
            assertNotNull(loaded.getPlugin());
            assertEquals(testPlugin, loaded.getSource());
            assertEquals("testPlugin", loaded.getId());
            assertEquals(manifest, loaded.getManifest());
        }
    }

    @Test
    void load_testPluginVersion_isValid() throws IOException {
        var testPlugin = Path.of("src/test/resources/plugins/testPlugin-1.0.0.jar");
        assertTrue(Files.isRegularFile(testPlugin));

        PluginManifest manifest;
        try (JarFile jar = new JarFile(testPlugin.toFile())) {
            var entry = jar.getEntry("plugin.json");
            assertNotNull(entry);

            try (var in = jar.getInputStream(entry)) {
                manifest = new JsonManifestLoader().load(in);
            }
        }

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var loadable = new UrlLoadablePlugin(manifest, testPlugin.toUri().toURL(), testPlugin, clContainer);

            var loaded = loadable.load();
            assertNotNull(loaded);
            assertNotNull(loaded.getPlugin());
            assertEquals(testPlugin, loaded.getSource());
            assertEquals("testPlugin", loaded.getId());
            assertEquals(manifest, loaded.getManifest());
        }
    }
}