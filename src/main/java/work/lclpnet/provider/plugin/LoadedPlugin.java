package work.lclpnet.provider.plugin;

import java.nio.file.Path;

public class LoadedPlugin {

    public final Path file;
    private Plugin plugin;

    public LoadedPlugin(Plugin plugin, Path file) {
        this.plugin = plugin;
        this.file = file;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void dereference() {
        this.plugin = null;
    }
}
