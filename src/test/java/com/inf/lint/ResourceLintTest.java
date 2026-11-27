package com.inf.lint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inf.advancement.ForgeAdvancements;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Filesystem checks for the resources that are easy to break silently. */
class ResourceLintTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ASSETS = RESOURCES.resolve("assets/inf");
    private static final Path DATA = RESOURCES.resolve("data/inf");
    private static final Path LANG = ASSETS.resolve("lang");

    @Test
    void everyAdvancementParsesAndHasALangEntry() throws IOException {
        Map<String, JsonObject> advancements = loadJson(advancementsDir());
        assertEquals(19, advancements.size());
        for (Map.Entry<String, JsonObject> entry : advancements.entrySet()) {
            JsonObject display = entry.getValue().getAsJsonObject("display");
            assertNotNull(display, entry.getKey() + " has no display");
            assertLangKey(display.getAsJsonObject("title").get("translate").getAsString());
            assertLangKey(
                    display.getAsJsonObject("description").get("translate").getAsString());
            String icon = display.getAsJsonObject("icon").get("id").getAsString();
            if (icon.startsWith("inf:")) {
                assertTrue(
                        Files.exists(ASSETS.resolve("models/item/" + icon.substring(4) + ".json")),
                        entry.getKey() + " icon " + icon + " has no item model");
            } else {
                assertTrue(icon.startsWith("minecraft:"), entry.getKey() + " icon namespace");
            }
        }
    }

    @Test
    void advancementParentsFormOneAcyclicTree() throws IOException {
        Map<String, JsonObject> advancements = loadJson(advancementsDir());
        Set<String> ids = new HashSet<>(advancements.keySet());
        Map<String, String> parents = new HashMap<>();
        for (String id : ids) {
            JsonObject json = advancements.get(id);
            if (!json.has("parent")) {
                continue;
            }
            String parent = json.get("parent").getAsString();
            assertTrue(parent.startsWith("inf:"), id + " parent must be an Inf node");
            String parentId = parent.substring("inf:".length());
            assertTrue(ids.contains(parentId), id + " parent " + parentId + " is missing");
            parents.put(id, parentId);
        }
        assertEquals(ids.size() - 1, parents.size(), "exactly one root is expected");
        for (String id : ids) {
            Set<String> seen = new HashSet<>();
            String current = id;
            while (parents.containsKey(current)) {
                assertTrue(seen.add(current), "cycle through " + id);
                current = parents.get(current);
            }
        }
    }

    @Test
    void forgeAdvancementIdsMatchTheFiles() throws IOException {
        Set<String> files = loadJson(advancementsDir()).keySet();
        assertEquals(files.size(), ForgeAdvancements.allIds().size());
        for (String id : ForgeAdvancements.allIds()) {
            assertTrue(files.contains(id), "missing advancement file for " + id);
        }
        for (WeaponKind kind : WeaponKind.values()) {
            assertTrue(files.contains(ForgeAdvancements.kind(kind)));
        }
        for (WeaponTier tier : WeaponTier.values()) {
            if (tier != WeaponTier.CRUDE) {
                assertTrue(files.contains(ForgeAdvancements.tier(tier)));
            }
        }
        assertFalse(files.contains("tier_crude"), "crude has no own node");
    }

    @Test
    void langFilesHaveIdenticalKeys() throws IOException {
        Set<String> en = loadJsonFile(LANG.resolve("en_us.json")).keySet();
        Set<String> zh = loadJsonFile(LANG.resolve("zh_cn.json")).keySet();
        assertEquals(en, zh);
        assertFalse(en.isEmpty());
    }

    @Test
    void everyModelTextureExistsAndStaysInTheInfNamespace() throws IOException {
        try (Stream<Path> models = Files.walk(ASSETS.resolve("models"))) {
            for (Path model : models.filter(p -> p.toString().endsWith(".json")).toList()) {
                JsonObject json =
                        JsonParser.parseString(Files.readString(model)).getAsJsonObject();
                if (!json.has("textures")) {
                    continue;
                }
                JsonObject textures = json.getAsJsonObject("textures");
                for (String key : textures.keySet()) {
                    String reference = textures.get(key).getAsString();
                    assertFalse(reference.startsWith("minecraft:"), model + " references " + reference);
                    String path = reference.contains(":") ? reference.split(":", 2)[1] : reference;
                    assertTrue(
                            Files.exists(ASSETS.resolve("textures").resolve(path + ".png")),
                            model + " missing texture " + reference);
                }
            }
        }
    }

    @Test
    void recipesAreWellFormedAndProduceInfItems() throws IOException {
        Map<String, JsonObject> recipes = loadJson(DATA.resolve("recipe"));
        assertEquals(7, recipes.size());
        for (Map.Entry<String, JsonObject> entry : recipes.entrySet()) {
            JsonObject result = entry.getValue().getAsJsonObject("result");
            assertTrue(result.get("id").getAsString().startsWith("inf:"), entry.getKey());
            assertTrue(result.get("count").getAsInt() > 0, entry.getKey());
        }
    }

    @Test
    void lootTablesDropTheirOwnBlock() throws IOException {
        Map<String, JsonObject> tables = loadJson(DATA.resolve("loot_table/blocks"));
        assertEquals(Set.of("forge_table"), tables.keySet());
        assertTrue(tables.get("forge_table").toString().contains("inf:forge_table"));
    }

    @Test
    void guiTexturesAndModMetadataExist() {
        assertTrue(Files.exists(ASSETS.resolve("textures/gui/forge_table.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/gui/advancement_background.png")));
        assertEquals(1, countFiles(Path.of("src/main/templates/META-INF"), "neoforge.mods.toml"));
    }

    private static Path advancementsDir() {
        return DATA.resolve("advancement");
    }

    private static JsonObject loadJsonFile(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    private static Map<String, JsonObject> loadJson(Path dir) throws IOException {
        Map<String, JsonObject> result = new HashMap<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                String name = file.getFileName().toString().replace(".json", "");
                result.put(name, JsonParser.parseString(Files.readString(file)).getAsJsonObject());
            }
        }
        return result;
    }

    private static int countFiles(Path dir, String name) {
        try (Stream<Path> files = Files.walk(dir)) {
            return (int)
                    files.filter(p -> p.getFileName().toString().equals(name)).count();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void assertLangKey(String key) throws IOException {
        for (String lang : List.of("en_us.json", "zh_cn.json")) {
            JsonObject json = loadJsonFile(LANG.resolve(lang));
            assertTrue(json.has(key), lang + " missing " + key);
            assertFalse(json.get(key).getAsString().isBlank(), lang + " blank " + key);
        }
    }
}
