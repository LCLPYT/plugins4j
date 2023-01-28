package work.lclpnet.provider;

public interface Plugin {

    String getIdentifier();

    void load();

    void unload();
}
