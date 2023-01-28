package work.lclpnet.provider;

import work.lclpnet.provider.plugin.PluginManager;

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
            handleInput(line);
        }
    }

    public void handleInput(String input) {
        String[] args = input.split("\\s");

        switch (args[0]) {
            case "quit", "exit" -> {
                System.out.println("Exiting...");
                running = false;
            }
            case "load" -> loadPlugin(args);
            case "unload" -> unloadPlugin(args);
            case "reload" -> reloadPlugin(args);
            default -> System.err.printf("Unknown command '%s' %n", args[0]);
        }
    }

    private void loadPlugin(String[] args) {
        String name = args[1];

        var path = Path.of(name);
        if (!Files.exists(path)) {
            System.err.println("File does not exist");
            return;
        }

        pluginManager.loadPlugin(path);
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
            System.err.println("File does not exist");
            return;
        }

        pluginManager.reloadPlugin(plugin.get());
    }
}
