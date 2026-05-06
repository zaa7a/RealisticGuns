package z4na.minecraft.realistic_guns.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.registry.GunRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * run/rlguns/ 以下のフォルダを全て走査してパックを読み込む
 *
 * パック構造:
 * run/rlguns/
 * └── [packname]/
 *     ├── pack.json
 *     ├── guns/
 *     │   └── *.json
 *     ├── models/
 *     │   └── *.geo.json
 *     ├── animations/
 *     │   └── *.animation.json
 *     └── textures/
 *         └── *.png
 */
public class GunPackLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadAll() {
        GunRegistry.clear();

        File packsDir = RealisticGuns.GUNPACKS_DIR;
        if (!packsDir.exists() || !packsDir.isDirectory()) {
            RealisticGuns.LOGGER.warn("[RealisticGuns] Gunpacks directory not found: {}", packsDir.getAbsolutePath());
            return;
        }

        File[] packFolders = packsDir.listFiles(File::isDirectory);
        if (packFolders == null || packFolders.length == 0) {
            RealisticGuns.LOGGER.info("[RealisticGuns] No gunpacks found.");
            return;
        }

        for (File packFolder : packFolders) {
            loadPack(packFolder);
        }

        RealisticGuns.LOGGER.info("[RealisticGuns] Loaded {} gun(s) from {} pack(s).",
                GunRegistry.getAllGuns().size(), packFolders.length);
    }

    private static void loadPack(File packFolder) {
        String packName = packFolder.getName();

        File packMeta = new File(packFolder, "pack.json");
        if (!packMeta.exists()) {
            RealisticGuns.LOGGER.warn("[RealisticGuns] Pack '{}' is missing pack.json, skipping.", packName);
            return;
        }

        try {
            JsonObject meta = readJson(packMeta);
            String packDisplayName = meta.has("name") ? meta.get("name").getAsString() : packName;
            RealisticGuns.LOGGER.info("[RealisticGuns] Loading pack: {} ({})", packDisplayName, packName);
        } catch (Exception e) {
            RealisticGuns.LOGGER.warn("[RealisticGuns] Failed to read pack.json for '{}': {}", packName, e.getMessage());
            return;
        }

        File gunsDir = new File(packFolder, "guns");
        if (!gunsDir.exists() || !gunsDir.isDirectory()) {
            RealisticGuns.LOGGER.warn("[RealisticGuns] Pack '{}' has no guns/ folder.", packName);
            return;
        }

        File[] gunFiles = gunsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (gunFiles == null) return;

        for (File gunFile : gunFiles) {
            loadGun(packName, gunFile);
        }
    }

    private static void loadGun(String packName, File gunFile) {
        try {
            GunDefinition def = GSON.fromJson(
                    new InputStreamReader(new FileInputStream(gunFile), StandardCharsets.UTF_8),
                    GunDefinition.class
            );

            String gunName = gunFile.getName().replace(".json", "");
            def.id = packName + ":" + gunName;

            GunRegistry.register(def);
            RealisticGuns.LOGGER.info("[RealisticGuns]   + Loaded gun: {}", def.id);

        } catch (Exception e) {
            RealisticGuns.LOGGER.error("[RealisticGuns] Failed to load gun '{}' in pack '{}': {}",
                    gunFile.getName(), packName, e.getMessage());
        }
    }

    private static JsonObject readJson(File file) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }
}