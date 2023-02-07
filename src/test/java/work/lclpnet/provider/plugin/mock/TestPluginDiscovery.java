package work.lclpnet.provider.plugin.mock;

import work.lclpnet.provider.plugin.discover.PluginDiscoveryService;
import work.lclpnet.provider.plugin.load.LoadablePlugin;

import java.util.*;
import java.util.stream.Stream;

public class TestPluginDiscovery implements PluginDiscoveryService {

    private final List<TestLoadablePlugin> testPlugins = new ArrayList<>();

    public TestPluginDiscovery(TestLoadablePlugin... plugins) {
        testPlugins.addAll(Arrays.asList(plugins));
    }

    @Override
    public Stream<? extends LoadablePlugin> discover() {
        return testPlugins.stream();
    }

    @Override
    public Optional<? extends LoadablePlugin> discoverFrom(Object src) {
        if (!(src instanceof String id)) return Optional.empty();

        return testPlugins.stream().filter(p -> p.getId().equals(id)).findAny();
    }
}
