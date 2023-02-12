package work.lclpnet.plugin;

import org.slf4j.Logger;
import work.lclpnet.plugin.graph.DAG;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginAlreadyLoadedException;
import work.lclpnet.plugin.load.PluginLoadException;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DistinctPluginContainer implements PluginContainer {

    private final Map<String, LoadedPlugin> loadedPlugins = new HashMap<>();
    private final Logger logger;
    private final ReentrantLock lock = new ReentrantLock();
    private final DAG<LoadedPlugin> dependencyGraph = new DAG<>();

    public DistinctPluginContainer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isPluginLoaded(String id) {
        return getPlugin(id).isPresent();
    }

    @Override
    public Set<LoadedPlugin> getPlugins() {
        return new HashSet<>(loadedPlugins.values());
    }

    @Override
    public Optional<LoadedPlugin> getPlugin(String id) {
        return Optional.ofNullable(loadedPlugins.get(id));
    }

    @Override
    public List<LoadedPlugin> getOrderedDependants(LoadedPlugin plugin) {
        var dependencyOrder = getOrderedDependencies(Set.of(plugin));

        if (!dependencyOrder.isEmpty()) {
            dependencyOrder.remove(0);  // remove self
        }

        return dependencyOrder;
    }

    @Override
    public List<LoadedPlugin> getOrderedDependencies(Set<LoadedPlugin> plugins) {
        Set<DAG.Node<LoadedPlugin>> rootNodes = new HashSet<>();

        for (LoadedPlugin plugin : plugins) {
            var node = dependencyGraph.getNode(plugin.getId()).orElseThrow();
            rootNodes.add(node);
        }

        var dependencyOrder = dependencyGraph.getTopologicalOrder(rootNodes);

        return dependencyOrder.stream()
                .map(DAG.Node::getObj)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LoadedPlugin> loadPlugin(LoadablePlugin loadable) {
        lock.lock();

        final var id = loadable.getManifest().id();

        logger.info("Loading plugin '{}'", id);

        if (isPluginLoaded(id)) {
            lock.unlock();
            throw new PluginAlreadyLoadedException("Plugin with id '%s' is already loaded".formatted(id));
        }

        for (var dependency : loadable.getManifest().dependsOn()) {
            if (!isPluginLoaded(dependency)) {
                throw new PluginLoadException("Unknown dependency '%s'".formatted(dependency));
            }
        }

        final var loaded = loadable.load();

        loadedPlugins.put(id, loaded);

        addToDependencyGraph(loadable, loaded);

        Plugin plugin = loaded.getPlugin();

        boolean loadError = false;

        try {
            plugin.load();
        } catch (Throwable t) {
            logger.error("Plugin '%s' threw an error on load. Unloading the plugin immediately...".formatted(id), t);
            loadError = true;
        }

        lock.unlock();

        if (loadError) {
            this.unloadPlugin(loaded);
            return Optional.empty();
        } else {
            logger.info("Plugin {} has been loaded.", id);
            return Optional.of(loaded);
        }
    }

    private void addToDependencyGraph(LoadablePlugin loadable, LoadedPlugin loaded) {
        var node = dependencyGraph.getOrCreateNode(loaded.getId(), loaded);

        var loadedDependencies = loadable.getManifest().dependsOn().stream()
                .map(this::getPlugin)
                .map(Optional::orElseThrow)
                .collect(Collectors.toSet());

        for (var dependency : loadedDependencies) {
            var depNode = dependencyGraph.getOrCreateNode(dependency.getId(), dependency);
            depNode.addChild(node);
        }
    }

    @Override
    public void unloadPlugin(LoadedPlugin loadedPlugin) {
        lock.lock();

        if (!isPluginLoaded(loadedPlugin.getId())) return;

        final var dependants = getOrderedDependants(loadedPlugin);
        Collections.reverse(dependants);

        for (var dependency : dependants) {
            unloadPluginInternal(dependency);
        }

        unloadPluginInternal(loadedPlugin);

        lock.unlock();
    }

    private void unloadPluginInternal(LoadedPlugin loadedPlugin) {
        final var id = loadedPlugin.getId();

        if (!isPluginLoaded(id)) return;

        logger.info("Unloading plugin '{}'", id);

        // remove the plugin; separate into standalone method so that no references remain on the stack
        removePlugin(loadedPlugin);

        // manually invoke garbage collection; plugin classes are freed here, if they unregistered properly
        System.gc();

        logger.info("Plugin '{}' unloaded.", id);
    }

    private void removePlugin(LoadedPlugin loadedPlugin) {
        Plugin plugin = loadedPlugin.getPlugin();  // reference to the foreign plugin class

        try {
            plugin.unload();
        } catch (Throwable t) {
            logger.error("Error unloading plugin, unloading anyways...", t);
        }

        var id = loadedPlugin.getManifest().id();

        loadedPlugins.remove(id);
        dependencyGraph.removeNode(id);

        loadedPlugin.remove();  // remove reference to the foreign plugin instance to enable gc
    }
}
