package work.lclpnet.provider.plugin.manifest;

import work.lclpnet.provider.plugin.PluginException;

import java.io.Serial;

public class ManifestLoadException extends PluginException {

    @Serial
    private static final long serialVersionUID = -5687034244961649788L;

    public ManifestLoadException(String msg) {
        super(msg);
    }

    public ManifestLoadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
