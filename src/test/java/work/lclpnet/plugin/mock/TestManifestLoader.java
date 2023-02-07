package work.lclpnet.plugin.mock;

import work.lclpnet.plugin.manifest.ManifestLoadException;
import work.lclpnet.plugin.manifest.PluginManifest;
import work.lclpnet.plugin.manifest.PluginManifestLoader;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class TestManifestLoader implements PluginManifestLoader {

    @Override
    public PluginManifest load(InputStream in) throws ManifestLoadException {
        return manifest("test", Collections.emptySet());
    }

    public static PluginManifest manifest(String id, Set<String> dependencies) {
        return new PluginManifest(id, null, dependencies);
    }
}
