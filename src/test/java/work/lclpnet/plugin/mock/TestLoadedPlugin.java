package work.lclpnet.plugin.mock;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.manifest.PluginManifest;

public class TestLoadedPlugin implements LoadedPlugin {

    private Plugin plugin;
    private final Object source;
    private final PluginManifest manifest;

    public TestLoadedPlugin(Plugin plugin, Object source, PluginManifest manifest) {
        this.plugin = plugin;
        this.source = source;
        this.manifest = manifest;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void remove() {
        this.plugin = null;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }

    @Override
    public String toString() {
        return getId();
    }
}
