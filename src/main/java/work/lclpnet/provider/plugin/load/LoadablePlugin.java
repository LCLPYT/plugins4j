package work.lclpnet.provider.plugin.load;

import work.lclpnet.provider.plugin.LoadedPlugin;
import work.lclpnet.provider.plugin.manifest.PluginManifest;

public interface LoadablePlugin {

    PluginManifest getManifest();

    LoadedPlugin load() throws PluginLoadException;
}
