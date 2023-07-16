package work.lclpnet.provider.spi;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public class TestServiceManager {

    private final ServiceLoader<TestServiceProvider> loader;

    public TestServiceManager() {
        loader = ServiceLoader.load(TestServiceProvider.class, this.getClass().getClassLoader());
    }

    public Stream<TestService> services() {
        return loader.stream().map(provider -> provider.get().create());
    }
}
