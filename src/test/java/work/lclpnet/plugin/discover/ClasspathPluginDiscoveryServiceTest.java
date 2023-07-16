package work.lclpnet.plugin.discover;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.plugin.load.DefaultClassLoaderContainer;
import work.lclpnet.plugin.manifest.JsonManifestLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathPluginDiscoveryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    @Test
    void discover_provided_all() throws IOException {
        var classpath = getProvidedClasspath();
        var manifestLoader = new JsonManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new ClasspathPluginDiscoveryService(classpath, manifestLoader, clContainer, LOGGER);
            assertEquals(1, discovery.discover().count());
        }
    }

    @Test
    void discoverFrom_provided_one() throws IOException {
        var classpath = getProvidedClasspath();
        var manifestLoader = new JsonManifestLoader();

        var firstClasspathEntry = Arrays.asList(classpath.get(0));
        Collections.reverse(firstClasspathEntry);  // make sure different order also works

        var query = firstClasspathEntry.toArray(URL[]::new);

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new ClasspathPluginDiscoveryService(classpath, manifestLoader, clContainer, LOGGER);

            assertTrue(discovery.discoverFrom(query).isPresent());
        }
    }

    @Test
    void discover_jar_one() throws IOException {
        var pluginsPath = Path.of("src/test/resources/plugins");
        assertTrue(Files.isDirectory(pluginsPath));

        Path path = pluginsPath.resolve("testPlugin.jar");
        assertTrue(Files.isRegularFile(path));

        var manifestLoader = new JsonManifestLoader();

        try (var clContainer = new DefaultClassLoaderContainer()) {
            var discovery = new ClasspathPluginDiscoveryService(List.of(), manifestLoader, clContainer, LOGGER);

            var optPlugin = discovery.discoverFrom(path);
            assertTrue(optPlugin.isPresent());
        }
    }

    private List<URL[]> getProvidedClasspath() {
        String testProp = System.getProperty("test.providerPluginClasspath");
        if (testProp == null) {
            throw new IllegalStateException("System property 'test.providerPluginClasspath' not set. Make sure to run tests using Gradle");
        }

        Path[] testPluginPaths = Arrays.stream(testProp.split(File.pathSeparator))
                .map(Path::of)
                .toArray(Path[]::new);

        assertTrue(Arrays.stream(testPluginPaths).allMatch(Files::isDirectory));

        URL[] testPluginUrls = Arrays.stream(testPluginPaths).map(path -> {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        List<URL[]> classPaths = new ArrayList<>();
        classPaths.add(testPluginUrls);

        return classPaths;
    }
}