package work.lclpnet.plugin;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginLoadException;
import work.lclpnet.plugin.manifest.BasePluginManifest;
import work.lclpnet.plugin.mock.ExtendedDistinctPluginContainer;
import work.lclpnet.plugin.mock.TestLoadablePlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DistinctPluginContainerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    @Test
    void loadPlugin_one_loaded() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");

        final var container = new DistinctPluginContainer(LOGGER);

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

        final var container = new DistinctPluginContainer(LOGGER);

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

        final var container = new DistinctPluginContainer(LOGGER);

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

        final var container = new DistinctPluginContainer(LOGGER);

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

        final var container = new DistinctPluginContainer(LOGGER);

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

        final var container = new DistinctPluginContainer(LOGGER);

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

    @Test
    void getOrderedDependants_hasDependants_correctOrder() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginA");

        final var container = new DistinctPluginContainer(LOGGER);

        assertThrows(PluginLoadException.class, () -> container.loadPlugin(pluginB));
        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);

        var loadedPluginA = container.getPlugin(pluginA.getId()).orElseThrow();
        var loadedPluginB = container.getPlugin(pluginB.getId()).orElseThrow();

        var dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(1, dependants.size());
        assertEquals(loadedPluginB, dependants.get(0));

        dependants = container.getOrderedDependants(loadedPluginB);
        assertTrue(dependants.isEmpty());
    }

    @Test
    void getOrderedDependants_unload_correct() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginA");

        final var container = new DistinctPluginContainer(LOGGER);

        assertThrows(PluginLoadException.class, () -> container.loadPlugin(pluginB));
        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);

        var loadedPluginA = container.getPlugin(pluginA.getId()).orElseThrow();
        var loadedPluginB = container.getPlugin(pluginB.getId()).orElseThrow();

        var dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(1, dependants.size());
        assertEquals(loadedPluginB, dependants.get(0));

        container.unloadPlugin(loadedPluginB);

        dependants = container.getOrderedDependants(loadedPluginA);
        assertTrue(dependants.isEmpty());
    }

    @Test
    void getOrderedDependants_complex_correctOrder() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginA");
        final var pluginC = new TestLoadablePlugin(loadedIds, "pluginC", "pluginB");

        final var container = new DistinctPluginContainer(LOGGER);

        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);
        container.loadPlugin(pluginC);

        var loadedPluginA = container.getPlugin(pluginA.getId()).orElseThrow();
        var loadedPluginB = container.getPlugin(pluginB.getId()).orElseThrow();
        var loadedPluginC = container.getPlugin(pluginC.getId()).orElseThrow();

        var dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(List.of(loadedPluginB, loadedPluginC), dependants);

        dependants = container.getOrderedDependants(loadedPluginB);
        assertEquals(List.of(loadedPluginC), dependants);
    }

    @Test
    void getOrderedDependants_complexUnload_correctOrder() {
        final var loadedIds = new ArrayList<String>();

        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA");
        final var pluginB = new TestLoadablePlugin(loadedIds, "pluginB", "pluginA");
        final var pluginC = new TestLoadablePlugin(loadedIds, "pluginC", "pluginB");

        final var container = new DistinctPluginContainer(LOGGER);

        container.loadPlugin(pluginA);
        container.loadPlugin(pluginB);
        container.loadPlugin(pluginC);

        var loadedPluginA = container.getPlugin(pluginA.getId()).orElseThrow();
        var loadedPluginB = container.getPlugin(pluginB.getId()).orElseThrow();
        var loadedPluginC = container.getPlugin(pluginC.getId()).orElseThrow();

        var dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(List.of(loadedPluginB, loadedPluginC), dependants);

        dependants = container.getOrderedDependants(loadedPluginB);
        assertEquals(List.of(loadedPluginC), dependants);

        container.unloadPlugin(loadedPluginC);

        dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(List.of(loadedPluginB), dependants);

        dependants = container.getOrderedDependants(loadedPluginB);
        assertEquals(List.of(), dependants);

        container.loadPlugin(pluginC);
        container.unloadPlugin(loadedPluginB);  // b and c should be unloaded

        dependants = container.getOrderedDependants(loadedPluginA);
        assertEquals(List.of(), dependants);
    }

    @Test
    void getOrderedDependants_unloadDependants_happens() {
        final var loadedIds = new ArrayList<String>();

        final var manifest = new BasePluginManifest("2", "pluginA", null, Collections.emptySet());
        final var pluginA = new TestLoadablePlugin(loadedIds, "pluginA", manifest);

        // distinct plugin container should be extendable
        final var container = new ExtendedDistinctPluginContainer(LOGGER);

        // should not load due to version semver requirement in ExtendedDistinctPluginContainer
        assertThrowsExactly(PluginLoadException.class, () -> container.loadPlugin(pluginA),
                "Plugin version does not respect semver");
    }
}