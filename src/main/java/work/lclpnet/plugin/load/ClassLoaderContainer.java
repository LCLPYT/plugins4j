package work.lclpnet.plugin.load;

public interface ClassLoaderContainer extends ClassResolver, ResourceResolver {

    void add(ClassLoader classLoader);

    void remove(ClassLoader classLoader);
}
