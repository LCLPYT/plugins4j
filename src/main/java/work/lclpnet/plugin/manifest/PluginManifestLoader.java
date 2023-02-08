package work.lclpnet.plugin.manifest;

import java.io.IOException;
import java.io.InputStream;

public interface PluginManifestLoader {

    String VERSION = "1";

    PluginManifest load(InputStream in) throws IOException, ManifestLoadException;
}
