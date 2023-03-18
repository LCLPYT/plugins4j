package work.lclpnet.testPlugin;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.load.PluginInit;
import work.lclpnet.provider.Instance;
import work.lclpnet.provider.ProviderPlugin;

public class TestPlugin implements Plugin {

    private final String pluginId;

    public TestPlugin(PluginInit init) {
        pluginId = init.manifest().id();
    }

    @Override
    public void load() {
        System.out.printf("Test plugin '%s' loaded.%n", pluginId);

        System.out.printf("Provider value: %s%n", ProviderPlugin.providedString);
        System.out.printf("Instance value: %s%n", new Instance("hello world"));
    }

    @Override
    public void unload() {
        System.out.println("Test plugin unloaded.");
    }
}
