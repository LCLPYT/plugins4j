package work.lclpnet.example;

import work.lclpnet.plugin.PluginManager;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.plugin.load.PluginAlreadyLoadedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Cli {

    private boolean running = true;
    private final PluginManager pluginManager;

    public Cli(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public void start() {
        final Scanner scanner = new Scanner(System.in);

        String line;

        while (running) {
            System.out.print("cli> ");
            try {
                line = scanner.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("End of input - exiting...");
                running = false;
                break;
            }

            try {
                handleInput(line);
            } catch (Throwable t) {
                System.err.println("Error executing command:");
                t.printStackTrace(System.err);
            }
        }
    }

    public void handleInput(String input) {
        String[] args = input.split("\\s");

        switch (args[0]) {
            case "quit", "exit", "q" -> {
                System.out.println("Exiting...");
                running = false;
            }
            case "load" -> loadPlugin(args);
            case "unload" -> unloadPlugin(args);
            case "reload" -> reloadPlugin(args);
            case "plugins", "list" -> listPlugins();
            case "help", "h", "?" -> help();
            default -> System.err.printf("Unknown command '%s'%n", args[0]);
        }
    }

    private void listPlugins() {
        var plugins = pluginManager.getPlugins().stream()
                .map(LoadedPlugin::getId)
                .toList();

        System.out.printf("Loaded plugins: %s%n", plugins);
    }

    private void loadPlugin(String[] args) {
        String name = args[1];

        var path = Path.of(name);
        if (!Files.exists(path)) {
            System.err.println("File does not exist");
            return;
        }

        try {
            pluginManager.loadPlugin(path);
        } catch (PluginAlreadyLoadedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void unloadPlugin(String[] args) {
        final var plugin = pluginManager.getPlugin(args[1]);

        if (plugin.isEmpty()) {
            System.err.println("Plugin not loaded");
            return;
        }

        pluginManager.unloadPlugin(plugin.get());
    }

    private void reloadPlugin(String[] args) {
        final var plugin = pluginManager.getPlugin(args[1]);

        if (plugin.isEmpty()) {
            System.err.println("Plugin not loaded");
            return;
        }

        pluginManager.reloadPlugin(plugin.get());
    }

    private void help() {
        System.out.println("""
                Example plugins4j program
                =========================
                Commands:
                list   - Lists loaded plugins
                load   - Load a plugin from a file
                unload - Unload a plugin by id; all dependants will be unloaded too
                reload - Reload a plugin by id
                exit   - Exit the program; will unload all plugins too
                """);
    }
}
