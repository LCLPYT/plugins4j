package work.lclpnet.plugin;

import org.apache.logging.log4j.Logger;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.PluginAlreadyLoadedException;
import work.lclpnet.plugin.load.PluginLoadException;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class DistinctPluginContainer implements PluginContainer {

    private final Map<String, LoadedPlugin> loadedPlugins = new HashMap<>();
    private final Logger logger;
    private final ReentrantLock lock = new ReentrantLock();

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
    public synchronized void loadPlugin(LoadablePlugin loadable) {
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

        Plugin plugin = loaded.getPlugin();

        boolean loadError = false;

        try {
            plugin.load();
        } catch (Throwable t) {
            logger.error("Plugin %s threw an error on load. Unloading the plugin immediately...".formatted(id), t);
            loadError = true;
        }

        lock.unlock();

        if (loadError) {
            this.unloadPlugin(loaded);
        } else {
            logger.info("Plugin {} has been loaded.", id);
        }
    }

    @Override
    public synchronized void unloadPlugin(LoadedPlugin loadedPlugin) {
        lock.lock();

        final var id = loadedPlugin.getManifest().id();

        logger.info("Unloading plugin '{}'", id);

        // remove the plugin; separate into standalone method so that no references remain on the stack
        removePlugin(loadedPlugin);

        // manually invoke garbage collection; plugin classes are freed here, if they unregistered properly
        System.gc();

        lock.unlock();

        logger.info("Plugin '{}' unloaded.", id);
    }

    private synchronized void removePlugin(LoadedPlugin loadedPlugin) {
        Plugin plugin = loadedPlugin.getPlugin();  // reference to the foreign plugin class

        try {
            plugin.unload();
        } catch (Throwable t) {
            logger.error("Error unloading plugin, unloading anyways...", t);
        }

        loadedPlugins.remove(loadedPlugin.getManifest().id());

        var classLoader = loadedPlugin.getClassLoader();

        if (classLoader instanceof Closeable c) {
            try {
                c.close();
            } catch (IOException ignored) {}
        }

        loadedPlugin.remove();  // remove reference to the foreign plugin instance to enable gc
    }
}
