package work.lclpnet.testPlugin;

import work.lclpnet.plugin.Plugin;

public class TestPlugin implements Plugin {

    @Override
    public void load() {
        System.out.println("Load.");
    }

    @Override
    public void unload() {
        System.out.println("Unload.");
    }
}
