package work.lclpnet.plugin;

import work.lclpnet.plugin.load.LoadedPlugin;

import java.util.Optional;
import java.util.Set;

public interface PluginManager {

    void loadPlugin(Object src);

    void unloadPlugin(LoadedPlugin plugin);

    Optional<LoadedPlugin> getPlugin(String id);

    void reloadPlugin(LoadedPlugin loaded);

    void reloadPlugins(Set<LoadedPlugin> loaded);

    boolean isPluginLoaded(String id);

    Set<LoadedPlugin> getPlugins();

    void shutdown();
}
