package work.lclpnet.provider.plugin;

import work.lclpnet.provider.plugin.manifest.PluginManifest;

public class LoadedPlugin {

    private Plugin plugin;
    private final Object source;
    private final PluginManifest manifest;

    public LoadedPlugin(Plugin plugin, Object source, PluginManifest manifest) {
        this.plugin = plugin;
        this.source = source;
        this.manifest = manifest;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void remove() {
        this.plugin = null;
    }

    public Object getSource() {
        return source;
    }

    public PluginManifest getManifest() {
        return manifest;
    }

    public String getId() {
        return getManifest().id();
    }
}
