package work.lclpnet.testPlugin;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.provider.Instance;
import work.lclpnet.provider.ProviderPlugin;

public class TestPlugin implements Plugin {

    @Override
    public void load() {
        System.out.println("Test plugin loaded.");

        System.out.printf("Provider value: %s%n", ProviderPlugin.providedString);
        System.out.printf("Instance value: %s%n", new Instance("hello world"));
    }

    @Override
    public void unload() {
        System.out.println("Test plugin unloaded.");
    }
}
