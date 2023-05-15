package work.lclpnet.plugin.load;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class UrlLoadablePlugin implements LoadablePlugin {

    private final PluginManifest manifest;
    private final URL[] urls;
    private final Object source;
    private final ClassLoaderContainer classLoaderContainer;

    public UrlLoadablePlugin(PluginManifest manifest, URL url, Object source, ClassLoaderContainer classLoaderContainer) {
        this(manifest, new URL[] { url }, source, classLoaderContainer);
    }

    public UrlLoadablePlugin(PluginManifest manifest, URL[] urls, Object source, ClassLoaderContainer classLoaderContainer) {
        this.manifest = manifest;
        this.urls = urls;
        this.source = source;
        this.classLoaderContainer = classLoaderContainer;
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public LoadedPlugin load() throws PluginLoadException {
        Plugin plugin;

        var classLoader = new PluginClassLoader(urls, getClass().getClassLoader(), manifest, classLoaderContainer);

        try {
            plugin = classLoader.loadPlugin();
        } catch (ReflectiveOperationException e) {
            try {
                classLoader.close();
            } catch (IOException ignored) {}

            throw new PluginLoadException("Failed to load plugin from '%s'".formatted(Arrays.toString(urls)), e);
        }

        return new JarLoadedPlugin(plugin, source, manifest, classLoader, classLoaderContainer);
    }
}
