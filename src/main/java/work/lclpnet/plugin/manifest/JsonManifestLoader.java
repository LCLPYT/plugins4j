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

    private static final Predicate<Object> STRING = x -> x instanceof String s && !s.isBlank();

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

        require(obj, "id", STRING);
        final String id = obj.getString("id");

        require(obj, "entry", STRING);
        final String entry = obj.getString("entry");

        optional(obj, "dependsOn", array(STRING));
        final var dependsOn = obj.has("dependsOn") ? stream(obj.getJSONArray("dependsOn"))
                .map(x -> (String) x)
                .collect(Collectors.toUnmodifiableSet()) : Collections.<String>emptySet();

        return new PluginManifest(id, entry, dependsOn);
    }

    private static void require(JSONObject obj, String key, Predicate<Object> predicate) throws ManifestLoadException {
        if (obj.has(key) && predicate.test(obj.get(key))) return;

        throw new ManifestLoadException("Manifest property %s is invalid".formatted(key));
    }

    private static void optional(JSONObject obj, String key, Predicate<Object> predicate) throws ManifestLoadException {
        if (!obj.has(key) || predicate.test(obj.get(key))) return;

        throw new ManifestLoadException("Manifest property %s is invalid".formatted(key));
    }

    private static Predicate<Object> array(Predicate<Object> elementPredicate) {
        return x -> x instanceof JSONArray a && stream(a).allMatch(elementPredicate);
    }

    private static Stream<Object> stream(JSONArray a) {
        return StreamSupport.stream(a.spliterator(), false);
    }
}
