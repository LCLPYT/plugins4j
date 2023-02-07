package work.lclpnet.plugin.manifest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.*;

class JsonManifestLoaderTest {

    private static final String TEST_MANIFEST = """
            {
              "id": "testPlugin",
              "entry": "work.lclpnet.testPlugin.TestPlugin",
              "dependsOn": []
            }
            """.trim();

    /** sets all the required properties on a JsonBuilder instance */
    private static final Set<UnaryOperator<JsonBuilder>> REQUIRED = Set.of(
            b -> b.withId("testPlugin"),
            b -> b.withEntry("work.lclpnet.testPlugin.TestPlugin")
    );

    // assertions for certain json types that need to throw an error on manifest load
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_NUMBER = (builder, method) -> {
        method.apply(builder, 5);
        assertThrowsManifestException(builder.build());
        method.apply(builder, 3.4F);
        assertThrowsManifestException(builder.build());
        method.apply(builder, 6.122323D);
        assertThrowsManifestException(builder.build());
        method.apply(builder, (byte) 2);
        assertThrowsManifestException(builder.build());
        method.apply(builder, (short) 1024);
        assertThrowsManifestException(builder.build());
        method.apply(builder, 100_000_000L);
        assertThrowsManifestException(builder.build());
    };
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_BOOLEAN = (builder, method) -> {
        method.apply(builder, true);
        assertThrowsManifestException(builder.build());
        method.apply(builder, false);
        assertThrowsManifestException(builder.build());
    };
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_STRING = (builder, method) -> {
        method.apply(builder, "test123");
        assertThrowsManifestException(builder.build());
        method.apply(builder, 'a');
        assertThrowsManifestException(builder.build());
    };
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_NULL = (builder, method) -> {
        method.apply(builder, null);
        assertThrowsManifestException(builder.build());
    };
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_OBJECT = (builder, method) -> {
        method.apply(builder, new JSONObject("{\"value\": true}"));
        assertThrowsManifestException(builder.build());
    };
    private static final BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>> CHECK_ANY_ARRAY = (builder, method) -> {
        method.apply(builder, new JSONArray("[\"apple\", true, 5, null, {\"val\": true}]"));
        assertThrowsManifestException(builder.build());
    };

    private static void assertIsTestManifest(PluginManifest manifest) {
        assertNotNull(manifest);
        assertEquals("testPlugin", manifest.id());
        assertEquals("work.lclpnet.testPlugin.TestPlugin", manifest.entryPoint());
        assertEquals(emptySet(), manifest.dependsOn());
    }

    private static void assertValidManifest(String json) throws IOException {
        var loader = new JsonManifestLoader();

        PluginManifest manifest;
        try (var in = input(json)) {
            manifest = loader.load(in);
        }

        assertIsTestManifest(manifest);
    }

    private static JsonBuilder createMinimalTestManifest() {
        var builder = json();
        REQUIRED.forEach(o -> o.apply(builder));
        return builder;
    }

    private static void assertThrowsManifestException(String json) {
        var loader = new JsonManifestLoader();
        assertThrows(ManifestLoadException.class, () -> {
            try (var in = input(json)) {
                loader.load(in);
            }
        });
    }

    private static InputStream input(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    private static JsonBuilder json() {
        return new JsonBuilder();
    }

    @SafeVarargs
    private static void assertInvalidPropertyTypes(
            BiFunction<JsonBuilder, Object, JsonBuilder> method,
            BiConsumer<JsonBuilder, BiFunction<JsonBuilder, Object, JsonBuilder>>... checks
    ) throws IOException {

        var builder = createMinimalTestManifest();
        assertValidManifest(builder.build());

        for (var c : checks) {
            c.accept(builder, method);
        }
    }

    private void testMissing(Set<UnaryOperator<JsonBuilder>> required) {
        for (var v : required) {
            var reduced = new HashSet<>(required);
            reduced.remove(v);

            var builder = json();
            reduced.forEach(o -> o.apply(builder));

            var json = builder.build();

            assertThrowsManifestException(json);

            testMissing(reduced);
        }
    }

    @Test
    void load_valid_succeeds() throws IOException {
        assertValidManifest(TEST_MANIFEST);
    }

    @Test
    void load_invalidJson_throws() {
        assertThrowsManifestException("{a");
    }

    @Test
    void load_missingRequired_throws() throws IOException {
        testMissing(REQUIRED);

        // test all required properties to be covered
        JsonBuilder builder = createMinimalTestManifest();

        var json = builder.build();
        assertValidManifest(json);
    }

    @Test
    void load_invalidId_throws() throws IOException {
        assertInvalidPropertyTypes(
                // setter
                JsonBuilder::withId,
                // invalid json types that need to be checked
                CHECK_NUMBER, CHECK_BOOLEAN, CHECK_NULL, CHECK_OBJECT, CHECK_ANY_ARRAY
        );
    }

    @Test
    void load_invalidEntry_throws() throws IOException {
        // type needs to be string
        assertInvalidPropertyTypes(
                JsonBuilder::withId,
                CHECK_NUMBER, CHECK_BOOLEAN, CHECK_NULL, CHECK_OBJECT, CHECK_ANY_ARRAY
        );
    }

    @Test
    void load_invalidDependsOn_throws() throws IOException {
        // type needs to be a string array or null
        assertInvalidPropertyTypes(
                JsonBuilder::withDependsOn,
                CHECK_NUMBER, CHECK_BOOLEAN, CHECK_OBJECT, CHECK_STRING, CHECK_ANY_ARRAY
        );
    }

    private static class JsonBuilder {
        private final JSONObject obj = new JSONObject();

        public JsonBuilder withId(Object id) {
            obj.put("id", id);
            return this;
        }

        public JsonBuilder withEntry(Object entry) {
            obj.put("entry", entry);
            return this;
        }

        public JsonBuilder withDependsOn(Object entry) {
            obj.put("dependsOn", entry);
            return this;
        }

        public String build() {
            return obj.toString();
        }
    }
}