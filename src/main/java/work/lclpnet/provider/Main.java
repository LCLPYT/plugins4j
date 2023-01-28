package work.lclpnet.provider;

import work.lclpnet.provider.plugin.PluginManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        final var pluginDir = Path.of("plugins");
        if (!Files.isDirectory(pluginDir)) Files.createDirectories(pluginDir);

        var pluginManager = new PluginManager();

        try (var files = Files.list(pluginDir)) {
            files.filter(Main::isPlugin).forEach(pluginManager::loadPlugin);
        }

        new Cli(pluginManager).start();
    }

    public static boolean isPlugin(Path path) {
        return path.toString().endsWith(".jar");
    }
}