package work.lclpnet.plugin.load;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DefaultClassLoaderContainer implements ClassLoaderContainer, ClassResolver, Closeable {

    private final Set<ClassLoader> loaders = new HashSet<>();

    @Override
    public void add(ClassLoader classLoader) {
        if (classLoader == null) return;

        synchronized (loaders) {
            loaders.add(classLoader);
        }
    }

    @Override
    public void remove(ClassLoader classLoader) {
        if (classLoader == null) return;

        synchronized (loaders) {
            loaders.remove(classLoader);
        }

        closeIfNecessary(classLoader);
    }

    @Override
    public void close() {
        synchronized (loaders) {
            var it = loaders.iterator();

            while (it.hasNext()) {
                var loader = it.next();
                it.remove();

                closeIfNecessary(loader);
            }
        }
    }

    private static void closeIfNecessary(ClassLoader classLoader) {
        if (classLoader instanceof Closeable c) {
            try {
                c.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public Optional<Class<?>> resolve(String name, ClassLoader delegate) {
        synchronized (loaders) {
            Class<?> res = null;

            for (var loader : loaders) {
                // skip self
                if (delegate != null && delegate.equals(loader)) continue;

                // try to load class from other class loaders
                try {
                    if (loader instanceof PluginClassLoader pcl) {
                        res = pcl.loadClassDelegated(name);  // use specific method to prevent recursive calls
                    } else {
                        res = loader.loadClass(name);
                    }
                } catch (ClassNotFoundException ignored) {}

                if (res != null) return Optional.of(res);
            }
        }

        return Optional.empty();
    }
}
