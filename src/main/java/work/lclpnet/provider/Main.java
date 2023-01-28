package work.lclpnet.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final Map<String, Plugin> plugins = new HashMap<>();

    public static void main(String[] args) throws IOException {
        final var pluginDir = Path.of("plugins");
        if (!Files.isDirectory(pluginDir)) Files.createDirectories(pluginDir);

        try (var files = Files.list(pluginDir)) {
            files.filter(Main::isPlugin).forEach(Main::loadPlugin);
        }
    }

    public static boolean isPlugin(Path path) {
        return path.toString().endsWith(".jar");
    }

    public static void loadPlugin(Path path) {
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

        plugins.put(identifier, plugin);

        plugin.load();
    }
}