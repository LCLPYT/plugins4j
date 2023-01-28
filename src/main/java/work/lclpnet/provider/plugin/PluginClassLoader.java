package work.lclpnet.provider.plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {
    private final URL url;

    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
     */
    public PluginClassLoader(URL url) {
        super(new URL[]{url});
        this.url = url;
    }

    public String getEntrypoint() throws IOException {
        final var url = new URL("jar", "", this.url + "!/");
        final var conn = (JarURLConnection) url.openConnection();
        final var attr = conn.getMainAttributes();

        if (attr == null)
            throw new IOException("No manifest found in " + url);

        final var entryPoint = attr.getValue("Plugin-Entrypoint");

        if (entryPoint == null)
            throw new IOException("Plugin-Entry not configured for " + url);

        return entryPoint;
    }

    public Plugin loadPlugin() throws IOException, ReflectiveOperationException {
        return loadPlugin(getEntrypoint());
    }

    public Plugin loadPlugin(String className) throws ReflectiveOperationException {
        Class<?> c = this.loadClass(className);

        if (!Plugin.class.isAssignableFrom(c))
            throw new InstantiationException("Loading plugin %s: does not implement %s".formatted(className, Plugin.class.getSimpleName()));

        Constructor<?> constructor;
        try {
            constructor = c.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("Class %s must have a constructor with no arguments!".formatted(className));
        }

        return (Plugin) constructor.newInstance();
    }

}
