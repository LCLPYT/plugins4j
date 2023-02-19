package work.lclpnet.plugin.manifest;

import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.mock.ExtendedJsonManifestLoader;
import work.lclpnet.plugin.mock.ExtendedPluginManifest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test ensures that the JsonManifestLoader can easily be extended.
 */
public class ExtendedJsonManifestTest {

    private static final String TEST_MANIFEST = """
            {
              "schemaVersion": 1,
              "version": "0.1.0-SNAPSHOT",
              "id": "testPlugin",
              "entry": "work.lclpnet.testPlugin.TestPlugin",
              "dependsOn": [],
              "test": 11
            }
            """.trim();

    @Test
    void testJsonLoaderIsExtendable() throws IOException {
        ExtendedPluginManifest manifest;
        try (var in = new ByteArrayInputStream(TEST_MANIFEST.getBytes(StandardCharsets.UTF_8))) {
            manifest = (ExtendedPluginManifest) new ExtendedJsonManifestLoader().load(in);
        }

        assertEquals(11, manifest.test());
        assertEquals(Collections.emptyList(), manifest.maybePresent());
    }
}
