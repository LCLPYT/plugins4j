package work.lclpnet.provider.plugin.load;

import work.lclpnet.provider.plugin.Plugin;
import work.lclpnet.provider.plugin.manifest.PluginManifest;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class PluginClassLoader extends URLClassLoader {

    private final PluginManifest manifest;

    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
     */
    public PluginClassLoader(URL url, PluginManifest manifest) {
        super(new URL[] { Objects.requireNonNull(url) });

        this.manifest = Objects.requireNonNull(manifest);
    }

    public Plugin loadPlugin() throws ReflectiveOperationException {
        final var entryPoint = manifest.entryPoint();

        Class<?> c = this.loadClass(entryPoint);

        if (!Plugin.class.isAssignableFrom(c))
            throw new InstantiationException("Loading plugin: %s does not implement %s".formatted(entryPoint, Plugin.class.getSimpleName()));

        Constructor<?> constructor;
        try {
            constructor = c.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("Class %s must have a constructor with no arguments!".formatted(entryPoint));
        }

        return (Plugin) constructor.newInstance();
    }

}
