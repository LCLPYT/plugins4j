package work.lclpnet.plugin.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.DistinctPluginContainer;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginLoadException;
import work.lclpnet.plugin.mock.TestLoadablePlugin;
import work.lclpnet.plugin.mock.TestPluginDiscovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OrderedPluginBootstrapTest {

    @Test
    void isPluginLoaded_boostrap_loaded() throws IOException {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginA, pluginB);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        boostrap.loadPlugins();

        var expected = Set.of(pluginA, pluginB).stream().map(TestLoadablePlugin::getId).collect(Collectors.toSet());

        assertEquals(expected, new HashSet<>(loadedIds));
        assertEquals(expected, container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet()));
    }

    @Test
    void loadPlugins_order_loaded() throws IOException {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", pluginA.getId());

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginB, pluginA);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        boostrap.loadPlugins();

        var expected = Stream.of(pluginA, pluginB).map(TestLoadablePlugin::getId).toList();
        assertEquals(expected, loadedIds);
    }

    @Test
    void loadPlugins_cyclic_throws() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA", "pluginB");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginA");

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginB, pluginA);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        assertThrows(PluginLoadException.class, boostrap::loadPlugins);
    }

    @Test
    void loadPlugins_bigCyclic_throws() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA", "pluginB");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginC");
        final var pluginC = new TestLoadablePlugin(loadedIds, "pluginC", "pluginA");

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginB, pluginA, pluginC);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        assertThrows(PluginLoadException.class, boostrap::loadPlugins);
    }

    @Test
    void loadPlugins_orderComplex_loaded() throws IOException {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");
        final var pluginC = new TestLoadablePlugin(loadedIds, "pluginC", "pluginF", "pluginA");
        final var pluginD = new TestLoadablePlugin(loadedIds, "pluginD", "pluginB");
        final var pluginE = new TestLoadablePlugin(loadedIds, "pluginE", "pluginD");
        final var pluginF = new TestLoadablePlugin(loadedIds, "pluginF", "pluginB");

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginA, pluginB, pluginC, pluginD, pluginE, pluginF);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        boostrap.loadPlugins();

        BiPredicate<TestLoadablePlugin, TestLoadablePlugin> before = (a, b) -> {
            String idA = a.getId(), idB = b.getId();
            return loadedIds.indexOf(idA) < loadedIds.indexOf(idB);
        };

        assertTrue(before.test(pluginA, pluginC));
        assertTrue(before.test(pluginB, pluginD));
        assertTrue(before.test(pluginB, pluginF));
        assertTrue(before.test(pluginD, pluginE));
        assertTrue(before.test(pluginF, pluginC));
        assertFalse(before.test(pluginE, pluginB));
        assertFalse(before.test(pluginC, pluginB));
    }

    @Test
    void loadPlugins_duplicateIds_throws() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginA");

        final var logger = LogManager.getLogger();
        final var discovery = new TestPluginDiscovery(pluginB, pluginA);
        final var container = new DistinctPluginContainer(logger);

        var boostrap = new OrderedPluginBootstrap(discovery, container);
        assertThrows(PluginLoadException.class, boostrap::loadPlugins);
    }

    @Test
    void Node$addChild_cycle_false() {
        var a = new OrderedPluginBootstrap.Node<>("a");
        var b = new OrderedPluginBootstrap.Node<>("b");

        assertTrue(a.addChild(b));
        assertFalse(b.addChild(a));
    }

    @Test
    void Node$addChild_cycleBig_false() {
        var a = new OrderedPluginBootstrap.Node<>("a");
        var b = new OrderedPluginBootstrap.Node<>("b");
        var c = new OrderedPluginBootstrap.Node<>("c");

        assertTrue(a.addChild(b));
        assertTrue(b.addChild(c));
        assertFalse(c.addChild(a));
    }
}