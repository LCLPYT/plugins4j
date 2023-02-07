package work.lclpnet.plugin.load;

import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.mock.TestManifestLoader;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

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

        var loadable = new UrlLoadablePlugin(manifest, testPlugin.toUri().toURL(), testPlugin);

        var loaded = loadable.load();
        assertNotNull(loaded);
        assertNotNull(loaded.getPlugin());
        assertEquals(testPlugin, loaded.getSource());
        assertEquals("testPlugin", loaded.getId());
        assertEquals(manifest, loaded.getManifest());
    }
}