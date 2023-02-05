package work.lclpnet.provider.plugin.manifest;

import java.io.IOException;
import java.io.InputStream;

public interface PluginManifestLoader {

    PluginManifest load(InputStream in) throws IOException, ManifestLoadException;
}
