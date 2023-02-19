package work.lclpnet.plugin.manifest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonManifestLoader implements PluginManifestLoader {

    public static final int SCHEMA_VERSION = 1;
    protected static final Predicate<Object> STRING = x -> x instanceof String s && !s.isBlank();
    protected static final Predicate<Object> INT = x -> x instanceof Integer;

    @Override
    public PluginManifest load(InputStream in) throws IOException {
        JSONObject obj;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);

            String json = out.toString(StandardCharsets.UTF_8);
            obj = new JSONObject(json);
        } catch (JSONException e) {
            throw new ManifestLoadException("Invalid manifest json", e);
        }

        return loadFromJson(obj);
    }

    public PluginManifest loadFromJson(JSONObject obj) {
        require(obj, "schemaVersion", INT);
        final int schemaVersion = obj.getInt("schemaVersion");

        if (SCHEMA_VERSION != schemaVersion) throw new ManifestLoadException("Invalid manifest version");

        require(obj, "version", STRING);
        final String version = obj.getString("version");

        require(obj, "id", STRING);
        final String id = obj.getString("id");

        require(obj, "entry", STRING);
        final String entry = obj.getString("entry");

        optional(obj, "dependsOn", array(STRING));
        final var dependsOn = obj.has("dependsOn") ? stream(obj.getJSONArray("dependsOn"))
                .map(x -> (String) x)
                .collect(Collectors.toUnmodifiableSet()) : Collections.<String>emptySet();

        return new BasePluginManifest(version, id, entry, dependsOn);
    }

    protected static void require(JSONObject obj, String key, Predicate<Object> predicate) throws ManifestLoadException {
        if (obj.has(key) && predicate.test(obj.get(key))) return;

        throw new ManifestLoadException("Manifest property %s is invalid".formatted(key));
    }

    protected static void optional(JSONObject obj, String key, Predicate<Object> predicate) throws ManifestLoadException {
        if (!obj.has(key) || predicate.test(obj.get(key))) return;

        throw new ManifestLoadException("Manifest property %s is invalid".formatted(key));
    }

    protected static Predicate<Object> array(Predicate<Object> elementPredicate) {
        return x -> x instanceof JSONArray a && stream(a).allMatch(elementPredicate);
    }

    protected static Stream<Object> stream(JSONArray a) {
        return StreamSupport.stream(a.spliterator(), false);
    }
}
