package work.lclpnet.example;

import org.apache.logging.log4j.LogManager;
import work.lclpnet.plugin.DistinctPluginContainer;
import work.lclpnet.plugin.SimplePluginManager;
import work.lclpnet.plugin.bootstrap.OrderedPluginBootstrap;
import work.lclpnet.plugin.discover.DirectoryPluginDiscoveryService;
import work.lclpnet.plugin.load.DefaultClassLoaderContainer;
import work.lclpnet.plugin.manifest.JsonManifestLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        final var logger = LogManager.getLogger(Main.class);

        final var classLoaderContainer = new DefaultClassLoaderContainer();

        final var pluginDiscoveryService = new DirectoryPluginDiscoveryService(
                Path.of("plugins"), new JsonManifestLoader(), classLoaderContainer, logger
        );

        final var pluginContainer = new DistinctPluginContainer(logger);

        final var pluginBootstrap = new OrderedPluginBootstrap(pluginDiscoveryService, pluginContainer);
        pluginBootstrap.loadPlugins();

        final var pluginManager = new SimplePluginManager(pluginDiscoveryService, pluginContainer);

        try {
            new Cli(pluginManager).start();
        } finally {
            pluginManager.shutdown();
            classLoaderContainer.close();
        }
    }
}