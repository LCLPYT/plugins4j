package work.lclpnet.plugin.load;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;

public class JarLoadedPlugin implements LoadedPlugin {

    private Plugin plugin;
    private ClassLoader classLoader;
    private final Object source;
    private final PluginManifest manifest;
    private final ClassLoaderContainer classLoaderContainer;

    public JarLoadedPlugin(Plugin plugin, Object source, PluginManifest manifest, ClassLoader classLoader,
                           ClassLoaderContainer classLoaderContainer) {
        this.plugin = plugin;
        this.source = source;
        this.manifest = manifest;
        this.classLoader = classLoader;
        this.classLoaderContainer = classLoaderContainer;

        this.classLoaderContainer.add(this.classLoader);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void remove() {
        this.classLoaderContainer.remove(this.classLoader);

        this.plugin = null;
        this.classLoader = null;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }
}
