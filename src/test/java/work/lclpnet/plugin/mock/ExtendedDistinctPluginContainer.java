package work.lclpnet.plugin.mock;

import org.slf4j.Logger;
import work.lclpnet.plugin.DistinctPluginContainer;
import work.lclpnet.plugin.load.LoadablePlugin;
import work.lclpnet.plugin.load.PluginLoadException;

public class ExtendedDistinctPluginContainer extends DistinctPluginContainer {

    public ExtendedDistinctPluginContainer(Logger logger) {
        super(logger);
    }

    @Override
    public void ensurePluginCanBeLoaded(LoadablePlugin loadable) throws PluginLoadException {
        super.ensurePluginCanBeLoaded(loadable);

        // extra conditions can go here

        // version should respect semver
        if (!loadable.getManifest().version().matches("[0-9]+\\.[0-9]+\\.[0-9]+")) {
            throw new PluginLoadException("Plugin version does not respect semver");
        }
    }
}
