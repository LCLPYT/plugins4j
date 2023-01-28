package work.lclpnet.provider.plugin;

public interface Plugin {

    String getIdentifier();

    void load();

    void unload();
}
