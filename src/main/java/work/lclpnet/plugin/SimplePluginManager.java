package work.lclpnet.plugin;

import work.lclpnet.plugin.discover.PluginDiscoveryService;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginLoadException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class SimplePluginManager implements PluginManager {

    private final PluginDiscoveryService discoveryService;
    private final PluginContainer pluginContainer;
    private volatile boolean acceptNewPlugins = true;

    public SimplePluginManager(PluginDiscoveryService discoveryService, PluginContainer pluginContainer) {
        this.discoveryService = discoveryService;
        this.pluginContainer = pluginContainer;
    }

    /**
     * Load a plugin after initial plugin bootstrap.
     * @param src Plugin source. E.g. a {@link Path}.
     */
    @Override
    public Optional<LoadedPlugin> loadPlugin(Object src) {
        if (!acceptNewPlugins) return Optional.empty();

        LoadablePlugin loadable;

        if (src instanceof LoadablePlugin) {
            loadable = (LoadablePlugin) src;
        } else {
            Optional<? extends LoadablePlugin> plugin;
            try {
                plugin = discoveryService.discoverFrom(src);
            } catch (IOException e) {
                throw new PluginLoadException("Discovering plugin failed", e);
            }

            if (plugin.isEmpty()) {
                throw new PluginLoadException("Could not find plugin at %s".formatted(src));
            }

            loadable = plugin.get();
        }

        return pluginContainer.loadPlugin(loadable);
    }

    @Override
    public Optional<LoadedPlugin> getPlugin(String identifier) {
        return pluginContainer.getPlugin(identifier);
    }

    @Override
    public Optional<LoadedPlugin> getPlugin(Plugin pluginInstance) {
        if (pluginInstance == null) return Optional.empty();

        return getPlugins().stream()
                .filter(loaded -> pluginInstance.equals(loaded.getPlugin()))
                .findAny();
    }

    @Override
    public void unloadPlugin(LoadedPlugin plugin) {
        pluginContainer.unloadPlugin(plugin);
    }

    @Override
    public void reloadPlugin(LoadedPlugin loaded) {
        reloadPlugins(Set.of(loaded));
    }

    @Override
    public void reloadPlugins(Set<LoadedPlugin> loaded) {
        var dependants = pluginContainer.getOrderedDependencies(loaded);

        for (var plugin : dependants) {
            unloadPlugin(plugin);
        }

        // dependency order is kept intact, because dependants is ordered accordingly
        for (var plugin : dependants) {
            loadPlugin(plugin.getSource());
        }
    }

    @Override
    public boolean isPluginLoaded(String identifier) {
        return pluginContainer.isPluginLoaded(identifier);
    }

    @Override
    public Set<LoadedPlugin> getPlugins() {
        return pluginContainer.getPlugins();
    }

    @Override
    public void shutdown() {
        acceptNewPlugins = false;
        pluginContainer.getPlugins().forEach(pluginContainer::unloadPlugin);
    }
}
