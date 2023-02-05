package work.lclpnet.provider.plugin.manifest;

import java.util.Set;

public record PluginManifest(String id, String entryPoint, Set<String> dependsOn) {

}
