package work.lclpnet.consumer;

import work.lclpnet.provider.plugin.Plugin;

public class Consumer implements Plugin {

    @Override
    public void load() {
        System.out.println("Load.");
    }

    @Override
    public void unload() {
        System.out.println("Unload.");
    }
}
