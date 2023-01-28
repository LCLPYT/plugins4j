package work.lclpnet.provider.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PluginManager {

    private final Map<String, LoadedPlugin> plugins = new HashMap<>();

    public void loadPlugin(Path path) {
        URL url;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Plugin plugin;
        try (PluginClassLoader classLoader = new PluginClassLoader(url)) {
            plugin = classLoader.loadPlugin();
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException("Failed to load plugin", e);
        }

        String identifier = plugin.getIdentifier();
        if (identifier == null)
            throw new NullPointerException("Plugin identifier must not be null");

        if (plugins.containsKey(identifier))
            throw new IllegalStateException("Plugin with identifier '%s' already loaded".formatted(identifier));

        plugins.put(identifier, new LoadedPlugin(plugin, path));

        plugin.load();
    }

    public Optional<LoadedPlugin> getPlugin(String identifier) {
        return Optional.ofNullable(plugins.get(identifier));
    }

    public void unloadPlugin(LoadedPlugin plugin) {
        unloadPlugin(plugin, false);
    }

    public void unloadPlugin(LoadedPlugin loaded, boolean force) {
        if (!plugins.containsValue(loaded))
            throw new IllegalStateException("Plugin not loaded");

        removePlugin(loaded, force);

        // manually invoke garbage collection, so that the plugin classes are freed now
        System.gc();
    }

    private void removePlugin(LoadedPlugin loaded, boolean force) {
        final var pluginId = loaded.getPlugin().getIdentifier();

        try {
            loaded.getPlugin().unload();
        } catch (Throwable t) {
            if (!force) throw new IllegalStateException("Error unloading plugin", t);
            else System.err.println("Error unloading plugin, trying to removing anyway...");
        }

        plugins.remove(pluginId);
        loaded.dereference();
    }

    public void reloadPlugin(LoadedPlugin loaded) {
        reloadPlugin(loaded, false);
    }

    public void reloadPlugin(LoadedPlugin loaded, boolean force) {
        unloadPlugin(loaded, force);
        loadPlugin(loaded.file);
    }
}
