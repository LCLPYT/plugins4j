package work.lclpnet.plugin;

import work.lclpnet.plugin.load.LoadedPlugin;

import java.util.Optional;
import java.util.Set;

public interface PluginManager {

    Optional<LoadedPlugin> loadPlugin(Object src);

    void unloadPlugin(LoadedPlugin plugin);

    Optional<LoadedPlugin> getPlugin(String id);

    Optional<LoadedPlugin> getPlugin(Plugin pluginInstance);

    void reloadPlugin(LoadedPlugin loaded);

    void reloadPlugins(Set<LoadedPlugin> loaded);

    boolean isPluginLoaded(String id);

    Set<LoadedPlugin> getPlugins();

    void shutdown();
}
