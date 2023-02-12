package work.lclpnet.plugin.load;

import work.lclpnet.plugin.manifest.PluginManifest;

public interface LoadablePlugin {

    PluginManifest getManifest();

    Object getSource();

    LoadedPlugin load() throws PluginLoadException;
}
