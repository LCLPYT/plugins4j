package work.lclpnet.plugin.load;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;

public interface LoadedPlugin {

    Plugin getPlugin();

    void remove();

    Object getSource();

    PluginManifest getManifest();

    default String getId() {
        return getManifest().id();
    }
}
