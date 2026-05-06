package z4na.minecraft.realistic_guns.registry;

import z4na.minecraft.realistic_guns.pack.GunDefinition;

import java.util.*;

/**
 * 読み込み済みの全銃定義を管理するレジストリ
 */
public class GunRegistry {

    private static final Map<String, GunDefinition> GUNS = new LinkedHashMap<>();

    public static void register(GunDefinition def) {
        GUNS.put(def.id, def);
    }

    public static Optional<GunDefinition> get(String id) {
        return Optional.ofNullable(GUNS.get(id));
    }

    public static Collection<GunDefinition> getAllGuns() {
        return Collections.unmodifiableCollection(GUNS.values());
    }

    public static void clear() {
        GUNS.clear();
    }

    public static boolean has(String id) {
        return GUNS.containsKey(id);
    }
}