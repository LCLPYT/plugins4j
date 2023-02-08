package work.lclpnet.plugin.bootstrap;

import work.lclpnet.plugin.PluginContainer;
import work.lclpnet.plugin.discover.PluginDiscoveryService;
import work.lclpnet.plugin.graph.DAG;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.PluginLoadException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A plugin bootstrap for a single plugin discovery service
 */
public class OrderedPluginBootstrap implements PluginBootstrap {

    private final PluginDiscoveryService pluginDiscoveryService;
    private final PluginContainer pluginContainer;

    public OrderedPluginBootstrap(PluginDiscoveryService pluginDiscoveryService, PluginContainer pluginContainer) {
        this.pluginDiscoveryService = pluginDiscoveryService;
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void loadPlugins() throws IOException {
        var found = pluginDiscoveryService.discover().toList();

        // ensure there are no duplicate plugin ids
        var duplicates = duplicateIds(found);
        if (!duplicates.isEmpty()) {
            throw new PluginLoadException("Duplicate plugin ids have been found: '%s'".formatted(duplicates));
        }

        // determine load order by dependsOn manifest property
        var loadOrder = determineLoadOrder(new HashSet<>(found));

        for (var plugin : loadOrder) {
            pluginContainer.loadPlugin(plugin);
        }
    }

    private Set<String> duplicateIds(List<? extends LoadablePlugin> plugins) {
        final Set<String> ids = new HashSet<>();
        final Set<String> duplicates = new HashSet<>();

        for (var plugin : plugins) {
            var id = plugin.getManifest().id();

            if (ids.contains(id)) {
                duplicates.add(id);
            } else {
                ids.add(id);
            }
        }

        return duplicates;
    }

    private List<LoadablePlugin> determineLoadOrder(Set<? extends LoadablePlugin> plugins) {
        final HashMap<String, LoadablePlugin> pluginsById = new HashMap<>();

        for (var plugin : plugins) {
            pluginsById.put(plugin.getManifest().id(), plugin);
        }

        final var dependencyGraph = new DAG<LoadablePlugin>();

        // construct an acyclic dependency graph
        for (var plugin : plugins) {
            var node = dependencyGraph.getOrCreateNode(plugin.getManifest().id(), plugin);
            var manifest = plugin.getManifest();

            for (var dependencyId : manifest.dependsOn()) {
                var dependency = pluginsById.get(dependencyId);

                if (dependency == null) {
                    throw new PluginLoadException("Unknown dependency '%s'".formatted(dependencyId));
                }

                var dependencyNode = dependencyGraph.getOrCreateNode(dependency.getManifest().id(), dependency);

                if (!dependencyNode.addChild(node)) {
                    var pluginId = manifest.id();

                    throw new PluginLoadException("'%s' depends on '%s', which already depends on '%s'!"
                            .formatted(pluginId, dependencyId, pluginId));
                }
            }
        }

        return dependencyGraph.getTopologicalOrder()
                .stream()
                .map(DAG.Node::getObj)
                .toList();
    }
}
