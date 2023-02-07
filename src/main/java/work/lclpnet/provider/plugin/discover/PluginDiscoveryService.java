package work.lclpnet.provider.plugin.discover;

import work.lclpnet.provider.plugin.load.LoadablePlugin;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public interface PluginDiscoveryService {

    Stream<? extends LoadablePlugin> discover() throws IOException;

    /**
     * Tries to discover a plugin by an identifier object.
     * If the src type can be accepted by the implementation, the service should discover a plugin by that src.
     *
     * @param src The source of the plugin, e.g. a {@link java.nio.file.Path} or something else.
     * @return A {@link LoadablePlugin} if the service found a plugin. Null if no plugin was found or the src type is
     * unsupported.
     * @throws IOException If something went wrong.
     */
    Optional<? extends LoadablePlugin> discoverFrom(Object src) throws IOException;
}
