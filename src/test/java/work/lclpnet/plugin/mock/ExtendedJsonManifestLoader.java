package work.lclpnet.plugin.mock;

import org.json.JSONObject;
import work.lclpnet.plugin.manifest.BasePluginManifest;
import work.lclpnet.plugin.manifest.JsonManifestLoader;
import work.lclpnet.plugin.manifest.PluginManifest;

import java.util.ArrayList;

public class ExtendedJsonManifestLoader extends JsonManifestLoader {

    @Override
    public PluginManifest loadFromJson(JSONObject obj) {
        var base = (BasePluginManifest) super.loadFromJson(obj);

        require(obj, "test", x -> x instanceof Integer);
        int test = obj.getInt("test");

        optional(obj, "maybePresent", array(x -> x instanceof Boolean));
        var maybePresent = obj.has("maybePresent") ? stream(obj.getJSONArray("maybePresent"))
                .map(e -> (Boolean) e)
                .toList(): new ArrayList<Boolean>();

        return new ExtendedPluginManifest(base, test, maybePresent);
    }
}
