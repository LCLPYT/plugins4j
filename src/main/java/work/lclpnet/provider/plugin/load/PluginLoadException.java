package work.lclpnet.provider.plugin.load;

import work.lclpnet.provider.plugin.PluginException;

import java.io.Serial;

public class PluginLoadException extends PluginException {
    @Serial
    private static final long serialVersionUID = -2752253133298495893L;

    public PluginLoadException(String msg) {
        super(msg);
    }

    public PluginLoadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
