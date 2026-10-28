package com.ovrtechnology.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ovrtechnology.aromalib.definition.loader.*;
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
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Definition cross-reference consistency")
class DefinitionReferenceConsistencyTest {

    private static final String DATA_ROOT = "data/aromalib/";
    private static final String SPRITE_DIR = "assets/aromalib/textures/gui/sprites/";
    private static final String MOD_PREFIX = "aromalib:";

    private static final Map<String, String> SPRITE_DIRECTORIES = Map.of(
            BiomeDefinitionLoader.INSTANCE.getDirectory(), "biomes",
            StructureDefinitionLoader.INSTANCE.getDirectory(), "structures"
    );

    private static final List<String> SCENTED_DIRECTORIES = List.of(
            BiomeDefinitionLoader.INSTANCE.getDirectory(),
            BlockDefinitionLoader.INSTANCE.getDirectory(),
            StructureDefinitionLoader.INSTANCE.getDirectory(),
            FlowerDefinitionLoader.INSTANCE.getDirectory(),
            MobDefinitionLoader.INSTANCE.getDirectory(),
            EventDefinitionLoader.DIRECTORY
    );

    @Test
    @DisplayName("every scented definition should reference a defined scent")
    void scentReferencesShouldResolve() throws IOException, URISyntaxException {
        Set<String> scentIds = idsIn(ScentDefinitionLoader.INSTANCE.getDirectory());
        assertThat(scentIds).as("bundled scents").isNotEmpty();

        for (String directory : SCENTED_DIRECTORIES) {
            for (Path file : listJsonFiles(DATA_ROOT + directory)) {
                JsonObject definition = loadObject(file);
                String scentId = optionalString(definition, "scent_id");
                assertThat(scentId).as("scent_id in %s/%s", directory, file.getFileName()).isNotNull();
                assertThat(scentIds)
                        .as("scent '%s' referenced by %s/%s", scentId, directory, file.getFileName())
                        .contains(scentId);
            }
        }
    }

    @Test
    @DisplayName("every perception reference should resolve to a defined category")
    void perceptionReferencesShouldResolve() throws IOException, URISyntaxException {
        Set<String> categoryIds = idsIn(CategoryDefinitionLoader.INSTANCE.getDirectory());
        assertThat(categoryIds).as("bundled categories").isNotEmpty();

        int references = 0;
        for (String directory : SCENTED_DIRECTORIES) {
            for (Path file : listJsonFiles(DATA_ROOT + directory)) {
                String perception = optionalString(loadObject(file), "perception");
                if (perception == null) {
                    continue;
                }
                references++;
                assertThat(categoryIds)
                        .as("perception '%s' referenced by %s/%s", perception, directory, file.getFileName())
                        .contains(perception);
            }
        }
        assertThat(references).as("perception references found").isPositive();
    }

    @Test
    @DisplayName("every biome and structure should have a menu sprite")
    void biomesAndStructuresShouldHaveSprites() throws IOException, URISyntaxException {
        for (Map.Entry<String, String> entry : SPRITE_DIRECTORIES.entrySet()) {
            List<Path> files = listJsonFiles(DATA_ROOT + entry.getKey());
            assertThat(files).as("definitions in %s", entry.getKey()).isNotEmpty();
            for (Path file : files) {
                String id = optionalString(loadObject(file), "id");
                assertThat(id).as("id in %s/%s", entry.getKey(), file.getFileName()).isNotNull();
                int colon = id.indexOf(':');
                assertThat(colon).as("id %s in %s has a namespace", id, file.getFileName()).isPositive();
                String sprite = SPRITE_DIR + entry.getValue() + "/" + id.substring(0, colon) + "/" + id.substring(colon + 1) + ".png";
                assertThat(resourceUrl(sprite))
                        .as("sprite %s for %s", sprite, id)
                        .isNotNull();
            }
        }
    }

    private static JsonArray wrappedArray(String path, String wrapperKey) throws IOException {
        JsonElement root;
        try (InputStream stream = DefinitionReferenceConsistencyTest.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("classpath resource %s", path).isNotNull();
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader);
            }
        }
        JsonArray array = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray(wrapperKey);
        assertThat(array).as("array '%s' in %s", wrapperKey, path).isNotNull();
        return array;
    }

    private static URL resourceUrl(String path) {
        return DefinitionReferenceConsistencyTest.class.getClassLoader().getResource(path);
    }

    private static Set<String> idsIn(String directory) throws IOException, URISyntaxException {
        Set<String> ids = new HashSet<>();
        for (Path file : listJsonFiles(DATA_ROOT + directory)) {
            String id = optionalString(loadObject(file), "id");
            assertThat(id).as("id in %s/%s", directory, file.getFileName()).isNotNull();
            ids.add(id);
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

    private static JsonObject loadObject(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file);
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            assertThat(element.isJsonObject()).as("%s is a JSON object", file.getFileName()).isTrue();
            return element.getAsJsonObject();
        }
    }
}
