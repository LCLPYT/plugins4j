package work.lclpnet.plugin;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.mock.TestLoadablePlugin;
import work.lclpnet.plugin.mock.TestPluginDiscovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SimplePluginManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    @Test
    void loadPlugin_one_loaded() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discovery = new TestPluginDiscovery(pluginA);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");

        assertTrue(pluginManager.isPluginLoaded("pluginA"));
        assertEquals(List.of("pluginA"), loadedIds);
        assertEquals(
                Set.of("pluginA"),
                pluginManager.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void loadPlugin_multiple_loaded() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        var discovery = new TestPluginDiscovery(pluginA, pluginB);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");
        pluginManager.loadPlugin("pluginB");

        assertTrue(pluginManager.isPluginLoaded("pluginA"));
        assertTrue(pluginManager.isPluginLoaded("pluginB"));
        assertEquals(Set.of("pluginA", "pluginB"), new HashSet<>(loadedIds));
        assertEquals(
                Set.of("pluginA", "pluginB"),
                pluginManager.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void unloadPlugin_one_unloaded() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discovery = new TestPluginDiscovery(pluginA);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");

        assertTrue(pluginManager.isPluginLoaded("pluginA"));
        assertEquals(List.of("pluginA"), loadedIds);

        var loadedPluginA = pluginManager.getPlugin("pluginA");
        pluginManager.unloadPlugin(loadedPluginA.orElseThrow());

        assertFalse(pluginManager.isPluginLoaded("pluginA"));
        assertTrue(loadedIds.isEmpty());
        assertTrue(pluginManager.getPlugins().isEmpty());
    }

    @Test
    void unloadPlugin_unloadOne_unloaded() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        var discovery = new TestPluginDiscovery(pluginA, pluginB);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");
        pluginManager.loadPlugin("pluginB");

        assertTrue(pluginManager.isPluginLoaded("pluginA"));
        assertTrue(pluginManager.isPluginLoaded("pluginB"));
        assertEquals(Set.of("pluginA", "pluginB"), new HashSet<>(loadedIds));

        var loadedPluginA = pluginManager.getPlugin("pluginA");
        pluginManager.unloadPlugin(loadedPluginA.orElseThrow());

        assertFalse(pluginManager.isPluginLoaded("pluginA"));
        assertTrue(pluginManager.isPluginLoaded("pluginB"));
        assertEquals(List.of("pluginB"), loadedIds);
        assertEquals(
                Set.of("pluginB"),
                pluginManager.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void reloadPlugin() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discovery = new TestPluginDiscovery(pluginA);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");

        var loaded = pluginManager.getPlugin("pluginA").orElseThrow();

        // NOTE: don't actually keep a direct reference of the actual foreign plugin class in production
        var before = loaded.getPlugin();

        pluginManager.reloadPlugin(loaded);

        // check that the actual plugin has been swapped
        assertNotEquals(before, pluginManager.getPlugin("pluginA").orElseThrow().getPlugin());
    }

    @Test
    void getPlugin_one_present() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        var discovery = new TestPluginDiscovery(pluginA);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");

        var plugin = pluginManager.getPlugin("pluginA");
        assertTrue(plugin.isPresent());
    }

    @Test
    void getPlugin_multiple_correct() {
        final var loadedIds = new ArrayList<String>();
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        var discovery = new TestPluginDiscovery(pluginA, pluginB);
        var container = new DistinctPluginContainer(LOGGER);

        var pluginManager = new SimplePluginManager(discovery, container);
        pluginManager.loadPlugin("pluginA");
        pluginManager.loadPlugin("pluginB");

        var plugin = pluginManager.getPlugin("pluginB");
        assertTrue(plugin.isPresent());
        assertEquals("pluginB", plugin.orElseThrow().getId());
    }
}