package work.lclpnet.plugin.load;

import java.net.URL;
import java.util.Enumeration;

public interface ResourceResolver {

    Enumeration<URL> resolveResources(String name, ClassLoader delegate);
}
