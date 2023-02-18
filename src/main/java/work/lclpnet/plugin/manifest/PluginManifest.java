package work.lclpnet.plugin.manifest;

import java.util.Set;

public interface PluginManifest {

    String version();

    String id();

    String entryPoint();

    Set<String> dependsOn();
}
