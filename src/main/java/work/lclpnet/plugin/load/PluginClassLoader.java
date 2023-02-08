package work.lclpnet.plugin.load;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class PluginClassLoader extends URLClassLoader {

    private final PluginManifest manifest;
    private final ClassResolver classResolver;

    /**
     * Creates a new JarClassLoader for the specified url.
     */
    public PluginClassLoader(URL url, ClassLoader parent, PluginManifest manifest, ClassResolver classResolver) {
        super(new URL[] { Objects.requireNonNull(url) }, parent);

        this.manifest = Objects.requireNonNull(manifest);
        this.classResolver = classResolver;
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> res = super.findClass(name);
            if (res != null) return res;
        } catch (ClassNotFoundException ignored) {}

        // class is not in our plugin jar, ask the other class loaders
        var otherPluginClass = this.classResolver.resolve(name, this);

        if (otherPluginClass.isPresent()) {
            return otherPluginClass.get();
        }

        throw new ClassNotFoundException(name);
    }
}
