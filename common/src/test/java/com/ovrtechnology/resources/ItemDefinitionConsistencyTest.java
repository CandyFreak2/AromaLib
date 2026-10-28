package com.ovrtechnology.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ovrtechnology.aromalib.definition.loader.TrackerDefinitionLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item definition consistency")
class ItemDefinitionConsistencyTest {

    private static final String DATA_ROOT = "data/aromalib/";
    private static final String LANG_PATH = "assets/aromalib/lang/en_us.json";
    private static final String ITEM_MODEL_DIR = "assets/aromalib/items/";
    private static final String TEXTURE_DIR = "assets/aromalib/textures/";

    private static final Map<String, String> ITEM_DEFINITION_FILES = Map.of(
            TrackerDefinitionLoader.INSTANCE.getDirectory(), "items"
    );

    private static final Pattern MOD_ITEM_REFERENCE = Pattern.compile("\"aromalib:([a-z0-9_]+)\"");

    @Test
    @DisplayName("every item definition should have a unique id, a bundled texture, a lang entry and an item model")
    void itemDefinitionsShouldBeComplete() throws IOException {
        JsonObject lang = loadObject(LANG_PATH);
        for (Map.Entry<String, String> entry : ITEM_DEFINITION_FILES.entrySet()) {
            List<JsonObject> items = definitions(entry.getKey(), entry.getValue());
            assertThat(items).as("definitions in %s", entry.getKey()).isNotEmpty();

            Set<String> seen = new HashSet<>();
            for (JsonObject item : items) {
                String id = item.get("id").getAsString();
                assertThat(seen.add(id)).as("unique id %s in %s", id, entry.getKey()).isTrue();

                String image = optionalString(item, "image");
                assertThat(image).as("image for %s", id).isNotNull();
                String texturePath = TEXTURE_DIR + image + ".png";
                assertThat(resourceUrl(texturePath)).as("texture %s for %s", texturePath, id).isNotNull();

                String langKey = "item.aromalib." + id;
                assertThat(lang.has(langKey)).as("lang key %s", langKey).isTrue();

                String modelPath = ITEM_MODEL_DIR + id + ".json";
                assertThat(resourceUrl(modelPath)).as("item model %s", modelPath).isNotNull();
            }
        }
    }

    private static List<JsonObject> definitions(String path, String wrapperKey) throws IOException {
        JsonElement root = loadJson(path);
        JsonArray array = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray(wrapperKey);
        assertThat(array).as("array '%s' in %s", wrapperKey, path).isNotNull();
        return array.asList().stream().map(JsonElement::getAsJsonObject).toList();
    }

    private static Set<String> ids(List<JsonObject> items) {
        Set<String> ids = new HashSet<>();
        for (JsonObject item : items) {
            ids.add(item.get("id").getAsString());
        }
        return ids;
    }

    private static String optionalString(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }

    private static List<Path> listJsonFiles(String directory) throws IOException, URISyntaxException {
        URL url = resourceUrl(directory);
        assertThat(url).as("bundled directory %s", directory).isNotNull();
        assertThat(url.getProtocol()).isEqualTo("file");
        try (Stream<Path> files = Files.list(Path.of(url.toURI()))) {
            return files.filter(path -> path.toString().endsWith(".json")).toList();
        }
    }

    private static List<String> listJsonStems(String directory) throws IOException, URISyntaxException {
        return listJsonFiles(directory).stream()
                .map(path -> path.getFileName().toString())
                .map(name -> name.substring(0, name.length() - ".json".length()))
                .toList();
    }

    private static URL resourceUrl(String path) {
        return ItemDefinitionConsistencyTest.class.getClassLoader().getResource(path);
    }

    private static JsonElement loadJson(String path) throws IOException {
        try (InputStream stream = ItemDefinitionConsistencyTest.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("classpath resource %s", path).isNotNull();
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader);
            }
        }
    }

    private static JsonObject loadObject(String path) throws IOException {
        return loadJson(path).getAsJsonObject();
    }
}
