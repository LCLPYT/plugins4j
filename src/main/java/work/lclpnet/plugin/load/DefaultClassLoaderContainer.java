package work.lclpnet.plugin.load;

import work.lclpnet.plugin.util.CompoundEnumeration;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DefaultClassLoaderContainer implements ClassLoaderContainer, Closeable {

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

    @Override
    public Enumeration<URL> resolveResources(String name, ClassLoader delegate) {
        Enumeration<URL>[] resources = getResources(name, delegate);

        return new CompoundEnumeration<>(resources);
    }

    @Nonnull
    private Enumeration<URL>[] getResources(String name, ClassLoader delegate) {
        synchronized (loaders) {
            final int size = loaders.size();

            @SuppressWarnings("unchecked")
            Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[Math.max(0, size - 1)];

            Iterator<ClassLoader> iterator = loaders.iterator();

            for (int i = 0, j = 0, n = tmp.length; i < size && j < n; i++) {
                ClassLoader loader = iterator.next();

                // skip self
                if (delegate != null && delegate.equals(loader)) continue;

                // try to load class from other class loaders
                try {
                    if (loader instanceof PluginClassLoader pcl) {
                        tmp[j++] = pcl.getResourcesDelegated(name);  // use specific method to prevent recursive calls
                    } else {
                        tmp[j++] = loader.getResources(name);
                    }
                } catch (IOException ignored) {}
            }

            return tmp;
        }
    }
}
