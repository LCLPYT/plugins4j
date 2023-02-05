package work.lclpnet.provider;

import org.apache.logging.log4j.LogManager;
import work.lclpnet.provider.plugin.DistinctPluginContainer;
import work.lclpnet.provider.plugin.SimplePluginManager;
import work.lclpnet.provider.plugin.bootstrap.OrderedPluginBootstrap;
import work.lclpnet.provider.plugin.discover.DirectoryPluginDiscoveryService;
import work.lclpnet.provider.plugin.manifest.JsonManifestLoader;

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