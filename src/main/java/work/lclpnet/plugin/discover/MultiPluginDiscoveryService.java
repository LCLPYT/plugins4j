package work.lclpnet.plugin.discover;

import work.lclpnet.plugin.load.LoadablePlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MultiPluginDiscoveryService implements PluginDiscoveryService {

    private final PluginDiscoveryService[] children;

    public MultiPluginDiscoveryService(Iterable<PluginDiscoveryService> children) {
        this(StreamSupport.stream(children.spliterator(), false).toArray(PluginDiscoveryService[]::new));
    }

    public MultiPluginDiscoveryService(PluginDiscoveryService... children) {
        this.children = children;
    }

    @Override
    public Stream<? extends LoadablePlugin> discover() throws IOException {
        return unboxIO(() -> {
            Function<PluginDiscoveryService, Stream<? extends LoadablePlugin>> mapper = boxIO(PluginDiscoveryService::discover);
            return Arrays.stream(children).flatMap(mapper);
        });
    }

    @Override
    public Optional<? extends LoadablePlugin> discoverFrom(Object src) throws IOException {
        return unboxIO(() -> Arrays.stream(children)
                .map(boxIO(service -> service.discoverFrom(src)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst());
    }

    private <T, R> Function<T, R> boxIO(IOFunction<T, R> function) {
        return arg -> {
            try {
                return function.apply(arg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T> T unboxIO(Supplier<T> supplier) throws IOException, RuntimeException {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioException) throw ioException;
            else throw e;
        }
    }

    private interface IOFunction<T, R> {
        R apply(T arg) throws IOException;
    }
}
