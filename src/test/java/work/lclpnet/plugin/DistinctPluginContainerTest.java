package work.lclpnet.plugin;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.mock.TestLoadablePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DistinctPluginContainerTest {

    @Test
    void loadPlugin_one_loaded() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);

        assertEquals(List.of("pluginA"), loadedIds);

        assertTrue(container.isPluginLoaded(pluginA.getId()));
        assertEquals(
                Set.of("pluginA"),
                container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void loadPlugin_multiple_loaded() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);

        assertTrue(Stream.of(pluginA, pluginB)
                .map(TestLoadablePlugin::getId)
                .allMatch(id -> loadedIds.contains(id) && container.isPluginLoaded(id)));

        assertEquals(
                Set.of("pluginA", "pluginB"),
                container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void getPlugin_one_present() {
        final var pluginA = new TestLoadablePlugin(new ArrayList<>(), "pluginA");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);

        var plugin = container.getPlugin("pluginA");
        assertTrue(plugin.isPresent());
        assertEquals("pluginA", plugin.get().getId());
    }

    @Test
    void getPlugin_multiple_correct() {
        ArrayList<String> loadedIds = new ArrayList<>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);

        var plugin = container.getPlugin("pluginB");
        assertTrue(plugin.isPresent());
        assertEquals("pluginB", plugin.get().getId());
    }

    @Test
    void unloadPlugin_one_unloaded() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);

        assertEquals(List.of("pluginA"), loadedIds);

        assertTrue(container.isPluginLoaded(pluginA.getId()));
        assertEquals(
                Set.of("pluginA"),
                container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );

        var loadedPluginA = container.getPlugin("pluginA");

        container.unloadPlugin(loadedPluginA.orElseThrow());

        assertTrue(loadedIds.isEmpty());

        assertFalse(container.isPluginLoaded(pluginA.getId()));
        assertTrue(container.getPlugins().isEmpty());
    }

    @Test
    void unloadPlugin_unloadOne_unloaded() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB");

        final var logger = LogManager.getLogger();
        final var container = new DistinctPluginContainer(logger);

        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);

        assertTrue(Stream.of(pluginA, pluginB)
                .map(TestLoadablePlugin::getId)
                .allMatch(id -> loadedIds.contains(id) && container.isPluginLoaded(id)));

        assertEquals(
                Set.of("pluginA", "pluginB"),
                container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );

        var loadedPluginA = container.getPlugin("pluginA");

        container.unloadPlugin(loadedPluginA.orElseThrow());

        assertEquals(List.of("pluginB"), loadedIds);

        assertFalse(container.isPluginLoaded(pluginA.getId()));
        assertTrue(container.isPluginLoaded(pluginB.getId()));

        assertEquals(
                Set.of("pluginB"),
                container.getPlugins().stream().map(LoadedPlugin::getId).collect(Collectors.toSet())
        );
    }
}