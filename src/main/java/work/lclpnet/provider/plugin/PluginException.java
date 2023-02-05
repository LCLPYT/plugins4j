package work.lclpnet.provider.plugin;

import java.io.Serial;

public class PluginException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3785585256377664405L;

    public PluginException(String msg) {
        super(msg);
    }

    public PluginException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
