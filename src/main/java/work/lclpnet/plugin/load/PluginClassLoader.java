package work.lclpnet.plugin.load;

import work.lclpnet.plugin.Plugin;
import work.lclpnet.plugin.manifest.PluginManifest;
import work.lclpnet.plugin.util.CompoundEnumeration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PluginClassLoader extends URLClassLoader {

    private final PluginManifest manifest;
    private final ClassResolver classResolver;
    private final ResourceResolver resourceResolver;
    private final Map<String, Integer> delegatedClassLoading = new HashMap<>();
    private final Map<String, Integer> delegatedResourceLoading = new HashMap<>();

    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url              The url to use as classpath
     * @param parent           A parent class loader used to find other classes.
     * @param manifest         The plugin manifest.
     * @param classResolver    A class resolver for plugin class loaders interop between each other.
     * @param resourceResolver A resource resolver for plugin class laoders interop between each other.
     */
    public PluginClassLoader(URL url, ClassLoader parent, PluginManifest manifest, ClassResolver classResolver,
                             ResourceResolver resourceResolver) {
        this(new URL[] { Objects.requireNonNull(url) }, parent, manifest, classResolver, resourceResolver);
    }

    /**
     * Creates a new JarClassLoader for the specified urls.
     *
     * @param urls             The urls to use as classpath.
     * @param parent           A parent class loader used to find other classes.
     * @param manifest         The plugin manifest.
     * @param classResolver    A class resolver for plugin class loaders interop between each other.
     * @param resourceResolver A resource resolver for plugin class laoders interop between each other.
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent, PluginManifest manifest, ClassResolver classResolver,
                             ResourceResolver resourceResolver) {
        super(Objects.requireNonNull(urls), parent);

        this.manifest = Objects.requireNonNull(manifest);
        this.classResolver = classResolver;
        this.resourceResolver = resourceResolver;
    }

    public Plugin loadPlugin() throws ReflectiveOperationException {
        final var entryPoint = manifest.entryPoint();

        Class<?> c = this.loadClass(entryPoint);

        if (!Plugin.class.isAssignableFrom(c))
            throw new InstantiationException("Loading plugin: %s does not implement %s".formatted(entryPoint, Plugin.class.getSimpleName()));

        Constructor<?> constructor;
        try {
            constructor = c.getConstructor(PluginInit.class);
        } catch (NoSuchMethodException ignored) {
            constructor = null;
        }

        if (constructor != null) {
            var init = new PluginInit(manifest);

            return (Plugin) constructor.newInstance(init);
        }

        try {
            constructor = c.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("Class %s has no constructor () or (PluginInit)".formatted(entryPoint));
        }

        return (Plugin) constructor.newInstance();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> res = super.findClass(name);
            if (res != null) return res;
        } catch (ClassNotFoundException ignored) {}

        // check if class load was delegated by another PluginClassLoader
        synchronized (delegatedClassLoading) {
            if (this.delegatedClassLoading.containsKey(name)) {
                throw new ClassNotFoundException(name);
            }
        }

        // class is not in our plugin jar, ask the other class loaders
        var otherPluginClass = this.classResolver.resolve(name, this);

        if (otherPluginClass.isPresent()) {
            return otherPluginClass.get();
        }

        throw new ClassNotFoundException(name);
    }

    Class<?> loadClassDelegated(String name) throws ClassNotFoundException {
        synchronized (delegatedClassLoading) {
            incr(delegatedClassLoading, name);
        }

        try {
            return this.loadClass(name);
        } finally {
            synchronized (delegatedClassLoading) {
                decr(delegatedClassLoading, name);
            }
        }
    }

    private static void incr(Map<String, Integer> map, String name) {
        var count = map.get(name);
        if (count == null) count = 0;

        map.put(name, ++count);
    }

    private static void decr(Map<String, Integer> map, String name) {
        var count = map.get(name);
        if (count == null) return;

        if (--count <= 0) map.remove(name);
        else map.put(name, count);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> ownResources = super.findResources(name);

        synchronized (delegatedResourceLoading) {
            // prevent infinite delegation
            if (delegatedResourceLoading.containsKey(name)) {
                return ownResources;
            }
        }

        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];

        tmp[0] = ownResources;
        tmp[1] = resourceResolver.resolveResources(name, this);

        return new CompoundEnumeration<>(tmp);
    }

    Enumeration<URL> getResourcesDelegated(String name) throws IOException {
        synchronized (delegatedResourceLoading) {
            incr(delegatedResourceLoading, name);
        }

        try {
            return this.getResources(name);
        } finally {
            synchronized (delegatedResourceLoading) {
                decr(delegatedResourceLoading, name);
            }
        }
    }
}
