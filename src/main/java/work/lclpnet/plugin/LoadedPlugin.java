package work.lclpnet.plugin;

import work.lclpnet.plugin.manifest.PluginManifest;

public class LoadedPlugin {

    private Plugin plugin;
    private ClassLoader classLoader;
    private final Object source;
    private final PluginManifest manifest;

    public LoadedPlugin(Plugin plugin, Object source, PluginManifest manifest, ClassLoader classLoader) {
        this.plugin = plugin;
        this.source = source;
        this.manifest = manifest;
        this.classLoader = classLoader;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void remove() {
        this.plugin = null;
        this.classLoader = null;
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
