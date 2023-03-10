package work.lclpnet.plugin.mock;

import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginLoadException;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestLoadablePlugin implements LoadablePlugin {

    private final String id;
    private final PluginManifest manifest;
    private final List<String> loadedIds;

    public TestLoadablePlugin(List<String> loadedIds, String id, String... dependencies) {
        this.loadedIds = loadedIds;
        this.id = id;
        this.manifest = TestManifestLoader.manifest(id, Arrays.stream(dependencies).collect(Collectors.toSet()));
    }

    public TestLoadablePlugin(List<String> loadedIds, String id, PluginManifest manifest) {
        this.loadedIds = loadedIds;
        this.id = id;
        this.manifest = manifest;
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }

    @Override
    public Object getSource() {
        return id;
    }

    @Override
    public LoadedPlugin load() throws PluginLoadException {
        // simulate plugin loading
        TestPlugin plugin = new TestPlugin(id, loadedIds);

        return new TestLoadedPlugin(plugin, id, manifest);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
