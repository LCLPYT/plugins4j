package work.lclpnet.provider.plugin;

import java.util.Optional;

public interface PluginManager {

    void loadPlugin(Object src);

    void unloadPlugin(LoadedPlugin plugin);

    Optional<LoadedPlugin> getPlugin(String id);

    void reloadPlugin(LoadedPlugin loaded);
}
