package work.lclpnet.plugin;

import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.LoadedPlugin;

import java.util.Optional;
import java.util.Set;

public interface PluginContainer {

    boolean isPluginLoaded(String id);

    Set<LoadedPlugin> getPlugins();

    Optional<LoadedPlugin> getPlugin(String id);

    void loadPlugin(LoadablePlugin plugin);

    void unloadPlugin(LoadedPlugin plugin);
}
