package work.lclpnet.provider.plugin.mock;

import work.lclpnet.provider.plugin.LoadedPlugin;
import work.lclpnet.provider.plugin.load.LoadablePlugin;
import work.lclpnet.provider.plugin.load.PluginLoadException;
import work.lclpnet.provider.plugin.manifest.PluginManifest;

import java.util.*;
import java.util.stream.Collectors;

public class TestLoadablePlugin implements LoadablePlugin {

    private final String id;
    private final PluginManifest manifest;
    private final List<String> loadedIds;

    public TestLoadablePlugin(List<String> loadedIds, String id, String... dependencies) {
        this.loadedIds = loadedIds;
        this.id = id;
        this.manifest = new PluginManifest(id, null, Arrays.stream(dependencies).collect(Collectors.toSet()));
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }

    @Override
    public LoadedPlugin load() throws PluginLoadException {
        // simulate plugin loading
        TestPlugin plugin = new TestPlugin(id, loadedIds);

        return new LoadedPlugin(plugin, id, manifest);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
