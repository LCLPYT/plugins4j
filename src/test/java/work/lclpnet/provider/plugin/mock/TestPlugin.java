package work.lclpnet.provider.plugin.mock;

import work.lclpnet.provider.plugin.Plugin;

import java.util.List;

public class TestPlugin implements Plugin {

    private final List<String> loadedIds;
    private final String id;

    public TestPlugin(String id, List<String> loadedIds) {
        this.loadedIds = loadedIds;
        this.id = id;
    }

    @Override
    public void load() {
        loadedIds.add(id);
    }

    @Override
    public void unload() {
        loadedIds.remove(id);
    }
}
