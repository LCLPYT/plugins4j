package work.lclpnet.plugin.load;

import work.lclpnet.plugin.LoadedPlugin;
import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.io.IOException;
import java.net.URL;

public class UrlLoadablePlugin implements LoadablePlugin {

    private final PluginManifest manifest;
    private final URL url;
    private final Object source;

    public UrlLoadablePlugin(PluginManifest manifest, URL url, Object source) {
        this.manifest = manifest;
        this.url = url;
        this.source = source;
    }

    @Override
    public PluginManifest getManifest() {
        return manifest;
    }

    @Override
    public LoadedPlugin load() throws PluginLoadException {
        Plugin plugin;
        try (PluginClassLoader classLoader = new PluginClassLoader(url, manifest)) {
            plugin = classLoader.loadPlugin();
        } catch (ReflectiveOperationException | IOException e) {
            throw new PluginLoadException("Failed to load plugin '%s'".formatted(url), e);
        }

        return new LoadedPlugin(plugin, source, manifest);
    }
}
