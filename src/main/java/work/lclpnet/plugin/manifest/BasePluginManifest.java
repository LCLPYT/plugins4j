package work.lclpnet.plugin.manifest;

import java.util.Set;

public record BasePluginManifest(String version, String id, String entryPoint,
                                 Set<String> dependsOn) implements PluginManifest {

}
