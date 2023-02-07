package work.lclpnet.plugin.bootstrap;

import work.lclpnet.plugin.PluginContainer;
import work.lclpnet.plugin.discover.PluginDiscoveryService;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.PluginLoadException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

        final var dependencyGraph = new DependencyGraph();

        // construct an acyclic dependency graph
        for (var plugin : plugins) {
            var node = dependencyGraph.getOrCreate(plugin);
            var manifest = plugin.getManifest();

            for (var dependencyId : manifest.dependsOn()) {
                var dependency = pluginsById.get(dependencyId);

                if (dependency == null) {
                    throw new PluginLoadException("Unknown dependency '%s'".formatted(dependencyId));
                }

                var dependencyNode = dependencyGraph.getOrCreate(dependency);

                if (!dependencyNode.addChild(node)) {
                    var pluginId = manifest.id();

                    throw new PluginLoadException("'%s' depends on '%s', which already depends on '%s'!"
                            .formatted(pluginId, dependencyId, pluginId));
                }
            }
        }

        return dependencyGraph.getTopologicalOrder()
                .stream()
                .map(Node::getObj)
                .toList();
    }

    static final class DependencyGraph {
        private final Map<String, Node<LoadablePlugin>> nodes = new ConcurrentHashMap<>();

        public synchronized Node<LoadablePlugin> getOrCreate(LoadablePlugin plugin) {
            return nodes.computeIfAbsent(plugin.getManifest().id(), id -> new Node<>(plugin));
        }

        public List<Node<LoadablePlugin>> getTopologicalOrder() {
            // Kahn's algorithm
            // https://en.wikipedia.org/w/index.php?title=Topological_sorting&oldid=1123299686#Kahn's_algorithm
            final List<Node<LoadablePlugin>> L = new ArrayList<>();
            final Set<Node<LoadablePlugin>> S = nodes.values()
                    .stream()
                    .filter(Node::isRoot)
                    .collect(Collectors.toSet());

            while (!S.isEmpty()) {
                var n = S.iterator().next();
                S.remove(n);
                L.add(n);

                for (var m : n.children) {
                    m.parents.remove(n);

                    if (m.isRoot()) {
                        S.add(m);
                    }
                }
            }

            // validate
            if (!L.stream().allMatch(Node::isRoot)) {
                throw new PluginLoadException("Cyclic dependency graph");
            }

            return L;
        }
    }

    static final class Node<T> {
        private final Set<Node<T>> parents = new HashSet<>();
        private final Set<Node<T>> children = new HashSet<>();
        private final T obj;

        Node(T obj) {
            this.obj = obj;
        }

        public T getObj() {
            return obj;
        }

        public boolean isRoot() {
            return parents.isEmpty();
        }

        public boolean addChild(Node<T> node) {
            if (node.hasChildDeep(this)) return false;

            children.add(node);
            node.parents.add(this);

            return true;
        }

        public boolean hasChildDeep(Node<T> node) {
            for (var child : children) {
                if (node.equals(child) || child.hasChildDeep(node)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return "%s: (%s|%s)".formatted(
                    obj,
                    this.children.stream().map(p -> p.obj.toString()).collect(Collectors.joining(",")),
                    this.parents.stream().map(p -> p.obj.toString()).collect(Collectors.joining(",")));
        }
    }
}
