package work.lclpnet.provider;

import work.lclpnet.plugin.Plugin;

public class ProviderPlugin implements Plugin {

    public static final String providedString = "test123";

    @Override
    public void load() {
        System.out.println("Provider has been loaded.");
    }

    @Override
    public void unload() {
        System.out.println("Provider has been unloaded.");
    }
}
