package work.lclpnet.plugin.mock;

import work.lclpnet.plugin.manifest.BasePluginManifest;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.util.List;
import java.util.Set;

public class ExtendedPluginManifest implements PluginManifest {

    private final BasePluginManifest base;
    private final int test;
    private final List<Boolean> maybePresent;

    public ExtendedPluginManifest(BasePluginManifest base, int test, List<Boolean> maybePresent) {
        this.base = base;
        this.test = test;
        this.maybePresent = maybePresent;
    }

    @Override
    public String version() {
        return base.version();
    }

    @Override
    public String id() {
        return base.id();
    }

    @Override
    public String entryPoint() {
        return base.entryPoint();
    }

    @Override
    public Set<String> dependsOn() {
        return base.dependsOn();
    }

    public int test() {
        return test;
    }

    public List<Boolean> maybePresent() {
        return maybePresent;
    }
}
