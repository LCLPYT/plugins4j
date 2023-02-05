package work.lclpnet.provider.plugin;

import work.lclpnet.provider.plugin.load.LoadablePlugin;

import java.util.Optional;
import java.util.Set;

public interface PluginContainer {

    boolean isPluginLoaded(String id);

    Set<LoadedPlugin> getPlugins();

    Optional<LoadedPlugin> getPlugin(String id);

    void loadPlugin(LoadablePlugin plugin);

    void unloadPlugin(LoadedPlugin plugin);
}
