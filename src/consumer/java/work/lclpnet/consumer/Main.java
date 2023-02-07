package work.lclpnet.consumer;

import org.apache.logging.log4j.LogManager;
import work.lclpnet.plugin.DistinctPluginContainer;
import work.lclpnet.plugin.SimplePluginManager;
import work.lclpnet.plugin.bootstrap.OrderedPluginBootstrap;
import work.lclpnet.plugin.discover.DirectoryPluginDiscoveryService;
import work.lclpnet.plugin.manifest.JsonManifestLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        final var logger = LogManager.getLogger(Main.class);

        final var pluginDiscoveryService = new DirectoryPluginDiscoveryService(Path.of("plugins"), new JsonManifestLoader(), logger);
        final var pluginContainer = new DistinctPluginContainer(logger);

        final var pluginBootstrap = new OrderedPluginBootstrap(pluginDiscoveryService, pluginContainer);
        pluginBootstrap.loadPlugins();

        final var pluginManager = new SimplePluginManager(pluginDiscoveryService, pluginContainer);

        new Cli(pluginManager).start();
    }
}