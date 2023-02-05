package work.lclpnet.provider.plugin.load;

import java.io.Serial;

public class PluginAlreadyLoadedException extends PluginLoadException {

    @Serial
    private static final long serialVersionUID = -4746925938979669540L;

    public PluginAlreadyLoadedException(String msg) {
        super(msg);
    }
}
