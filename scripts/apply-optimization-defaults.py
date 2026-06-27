#!/usr/bin/env python3
"""Bake YouHaveTrouble (minecraft-optimization) values into Waffle as source defaults.

Run AFTER `./gradlew applyAllPatches` and BEFORE rebuilding patches. Every edit
asserts its target text occurs exactly once; if not, the script aborts so CI fails
loudly instead of producing a silently-wrong build. Already-default values and
Purpur-only settings (no Paper equivalent) are intentionally omitted.
"""
import glob
import os
import sys

FILES = {
    "WC": "paper-server/src/main/java/io/papermc/paper/configuration/WorldConfiguration.java",
    "SWC": "paper-server/src/main/java/org/spigotmc/SpigotWorldConfig.java",
    "ADR": "paper-server/src/main/java/io/papermc/paper/configuration/type/fallback/ArrowDespawnRate.java",
    "BUKKIT": "paper-server/src/main/resources/configurations/bukkit.yml",
    "DSP": "paper-server/src/minecraft/java/net/minecraft/server/dedicated/DedicatedServerProperties.java",
}


def resolve(rel):
    candidates = [rel, rel.replace("paper-server/", "waffle-server/", 1)]
    for c in candidates:
        if os.path.isfile(c):
            return c
    base = os.path.basename(rel)
    hits = set()
    for root in ("paper-server", "waffle-server", "."):
        if os.path.isdir(root):
            hits.update(glob.glob(os.path.join(root, "**", base), recursive=True))
    hits = sorted(hits)
    if len(hits) == 1:
        return hits[0]
    raise SystemExit(f"resolve failed for {rel!r}: candidates={hits}")


# (file, old, new)
EDITS = [
    # ---- paper-world-defaults: WorldConfiguration.java ----
    ("WC", "public boolean preventMovingIntoUnloadedChunks = false;",
           "public boolean preventMovingIntoUnloadedChunks = true;"),
    ("WC", "public int maxEntityCollisions = 8;",
           "public int maxEntityCollisions = 2;"),
    ("WC", "public boolean updatePathfindingOnBlockUpdate = true;",
           "public boolean updatePathfindingOnBlockUpdate = false;"),
    ("WC", "public boolean fixClimbingBypassingCrammingRule = false;",
           "public boolean fixClimbingBypassingCrammingRule = true;"),
    ("WC", "public int mobSpawner = 1;",
           "public int mobSpawner = 2;"),
    ("WC", "public int grassSpread = 1;",
           "public int grassSpread = 4;"),
    ("WC", "public RedstoneImplementation redstoneImplementation = RedstoneImplementation.VANILLA;",
           "public RedstoneImplementation redstoneImplementation = RedstoneImplementation.ALTERNATE_CURRENT;"),
    ("WC", "public boolean ignoreOccludingBlocks = false;",
           "public boolean ignoreOccludingBlocks = true;"),
    ("WC", "public boolean optimizeExplosions = false;",
           "public boolean optimizeExplosions = true;"),
    ("WC", "public boolean findAlreadyDiscoveredVillager = false;",
           "public boolean findAlreadyDiscoveredVillager = true;"),
    ("WC", "public BooleanOrDefault findAlreadyDiscoveredLootTable = BooleanOrDefault.USE_DEFAULT;",
           "public BooleanOrDefault findAlreadyDiscoveredLootTable = new BooleanOrDefault(true);"),
    ("WC", "public IntOr.Disabled netherCeilingVoidDamageHeight = IntOr.Disabled.DISABLED;",
           "public IntOr.Disabled netherCeilingVoidDamageHeight = new IntOr.Disabled(OptionalInt.of(127));"),
    ("WC", "public ArrowDespawnRate nonPlayerArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig);",
           "public ArrowDespawnRate nonPlayerArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig, 20);"),
    ("WC", "public ArrowDespawnRate creativeArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig);",
           "public ArrowDespawnRate creativeArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig, 20);"),
    # treasure-maps.enabled (pair with class decl line for uniqueness)
    ("WC",
     "        public class TreasureMaps extends ConfigurationPart {\n"
     "            public boolean enabled = true;",
     "        public class TreasureMaps extends ConfigurationPart {\n"
     "            public boolean enabled = false;"),
    # armor-stands tick + do-collision (combined, unique only as a pair)
    ("WC",
     "            public boolean doCollisionEntityLookups = true;\n"
     "            public boolean tick = true;",
     "            public boolean doCollisionEntityLookups = false;\n"
     "            public boolean tick = false;"),
    # anti-xray.enabled (pair with engineMode line for uniqueness)
    ("WC",
     "            public boolean enabled = false;\n"
     "            public EngineMode engineMode = EngineMode.HIDE;",
     "            public boolean enabled = true;\n"
     "            public EngineMode engineMode = EngineMode.HIDE;"),
    # entity-per-chunk-save-limit (replace the 6 put() calls with the full guide set)
    ("WC",
     "            map.put(EntityTypes.EXPERIENCE_ORB, -1);\n"
     "            map.put(EntityTypes.SNOWBALL, -1);\n"
     "            map.put(EntityTypes.ENDER_PEARL, -1);\n"
     "            map.put(EntityTypes.ARROW, -1);\n"
     "            map.put(EntityTypes.FIREBALL, -1);\n"
     "            map.put(EntityTypes.SMALL_FIREBALL, -1);",
     "            map.put(EntityTypes.AREA_EFFECT_CLOUD, 8);\n"
     "            map.put(EntityTypes.ARROW, 16);\n"
     "            map.put(EntityTypes.BREEZE_WIND_CHARGE, 8);\n"
     "            map.put(EntityTypes.DRAGON_FIREBALL, 3);\n"
     "            map.put(EntityTypes.EGG, 8);\n"
     "            map.put(EntityTypes.ENDER_PEARL, 8);\n"
     "            map.put(EntityTypes.EXPERIENCE_BOTTLE, 3);\n"
     "            map.put(EntityTypes.EXPERIENCE_ORB, 16);\n"
     "            map.put(EntityTypes.EYE_OF_ENDER, 8);\n"
     "            map.put(EntityTypes.FIREBALL, 8);\n"
     "            map.put(EntityTypes.FIREWORK_ROCKET, 8);\n"
     "            map.put(EntityTypes.LLAMA_SPIT, 3);\n"
     "            map.put(EntityTypes.SPLASH_POTION, 8);\n"
     "            map.put(EntityTypes.LINGERING_POTION, 8);\n"
     "            map.put(EntityTypes.SHULKER_BULLET, 8);\n"
     "            map.put(EntityTypes.SMALL_FIREBALL, 8);\n"
     "            map.put(EntityTypes.SNOWBALL, 8);\n"
     "            map.put(EntityTypes.SPECTRAL_ARROW, 16);\n"
     "            map.put(EntityTypes.TRIDENT, 16);\n"
     "            map.put(EntityTypes.WIND_CHARGE, 8);\n"
     "            map.put(EntityTypes.WITHER_SKULL, 4);"),
    # despawn-ranges: hard 72 / soft 30 for every MobCategory
    ("WC",
     "category -> DespawnRangePair.createDefault()",
     "category -> new DespawnRangePair(new DespawnRange(new IntOr.Default(OptionalInt.of(72))), new DespawnRange(new IntOr.Default(OptionalInt.of(30))))"),
    # tick-rates.behavior.villager: validatenearbypoi 60 + acquirepoi 120
    ("WC",
     "table -> table.put(EntityTypes.VILLAGER, \"validatenearbypoi\", -1)",
     "table -> {\n"
     "            table.put(EntityTypes.VILLAGER, \"validatenearbypoi\", 60);\n"
     "            table.put(EntityTypes.VILLAGER, \"acquirepoi\", 120);\n"
     "        }"),
    # tick-rates.sensor.villager: 5 sensors per guide
    ("WC",
     "table -> table.put(EntityTypes.VILLAGER, \"secondarypoisensor\", 40)",
     "table -> {\n"
     "            table.put(EntityTypes.VILLAGER, \"secondarypoisensor\", 80);\n"
     "            table.put(EntityTypes.VILLAGER, \"nearestbedsensor\", 80);\n"
     "            table.put(EntityTypes.VILLAGER, \"villagerbabiessensor\", 40);\n"
     "            table.put(EntityTypes.VILLAGER, \"playersensor\", 40);\n"
     "            table.put(EntityTypes.VILLAGER, \"nearestlivingentitysensor\", 40);\n"
     "        }"),
    # alt-item-despawn-rate: enabled + items list (combined, unique as a pair)
    ("WC",
     "                public boolean enabled = false;\n"
     "                public Reference2IntMap<Item> items = new Reference2IntOpenHashMap<>(Map.of(Items.COBBLESTONE, 300));",
     "                public boolean enabled = true;\n"
     "                public Reference2IntMap<Item> items = new Reference2IntOpenHashMap<>(Map.ofEntries("
     "Map.entry(Items.COBBLESTONE, 300), Map.entry(Items.NETHERRACK, 300), Map.entry(Items.SAND, 300), "
     "Map.entry(Items.RED_SAND, 300), Map.entry(Items.GRAVEL, 300), Map.entry(Items.DIRT, 300), "
     "Map.entry(Items.SHORT_GRASS, 300), Map.entry(Items.PUMPKIN, 300), Map.entry(Items.MELON_SLICE, 300), "
     "Map.entry(Items.KELP, 300), Map.entry(Items.BAMBOO, 300), Map.entry(Items.SUGAR_CANE, 300), "
     "Map.entry(Items.TWISTING_VINES, 300), Map.entry(Items.WEEPING_VINES, 300), Map.entry(Items.OAK_LEAVES, 300), "
     "Map.entry(Items.SPRUCE_LEAVES, 300), Map.entry(Items.BIRCH_LEAVES, 300), Map.entry(Items.JUNGLE_LEAVES, 300), "
     "Map.entry(Items.ACACIA_LEAVES, 300), Map.entry(Items.DARK_OAK_LEAVES, 300), Map.entry(Items.MANGROVE_LEAVES, 300), "
     "Map.entry(Items.CHERRY_LEAVES, 300), Map.entry(Items.CACTUS, 300), Map.entry(Items.DIORITE, 300), "
     "Map.entry(Items.GRANITE, 300), Map.entry(Items.ANDESITE, 300), Map.entry(Items.SCAFFOLDING, 600)));"),

    # ---- spigot.yml: SpigotWorldConfig.java ----
    ("SWC", 'getInt("mob-spawn-range", 8)', 'getInt("mob-spawn-range", 3)'),
    ("SWC", "public int animalActivationRange = 32;", "public int animalActivationRange = 16;"),
    ("SWC", "public int monsterActivationRange = 32;", "public int monsterActivationRange = 24;"),
    ("SWC", "public int raiderActivationRange = 64;", "public int raiderActivationRange = 48;"),
    ("SWC", "public int miscActivationRange = 16;", "public int miscActivationRange = 8;"),
    ("SWC", "public int waterActivationRange = 16;", "public int waterActivationRange = 8;"),
    ("SWC", "public int villagerActivationRange = 32;", "public int villagerActivationRange = 16;"),
    ("SWC", "public int flyingMonsterActivationRange = 32;", "public int flyingMonsterActivationRange = 48;"),
    ("SWC", "public int playerTrackingRange = 128;", "public int playerTrackingRange = 48;"),
    ("SWC", "public int animalTrackingRange = 96;", "public int animalTrackingRange = 48;"),
    ("SWC", "public int monsterTrackingRange = 96;", "public int monsterTrackingRange = 48;"),
    ("SWC", "public int miscTrackingRange = 96;", "public int miscTrackingRange = 32;"),
    ("SWC", "public boolean tickInactiveVillagers = true;", "public boolean tickInactiveVillagers = false;"),
    ("SWC", 'getBoolean("nerf-spawner-mobs", false)', 'getBoolean("nerf-spawner-mobs", true)'),
    ("SWC", 'getDouble("merge-radius.item", 0.5)', 'getDouble("merge-radius.item", 3.5)'),
    ("SWC", 'getDouble("merge-radius.exp", -1)', 'getDouble("merge-radius.exp", 4.0)'),
    ("SWC", 'getInt("ticks-per.hopper-check", 1)', 'getInt("ticks-per.hopper-check", 8)'),

    # ---- bukkit.yml (bundled default resource) ----
    ("BUKKIT", "monsters: 70", "monsters: 20"),
    ("BUKKIT", "animals: 10", "animals: 5"),
    ("BUKKIT", "water-animals: 5", "water-animals: 2"),
    ("BUKKIT", "water-ambient: 20", "water-ambient: 2"),
    ("BUKKIT", "water-underground-creature: 5", "water-underground-creature: 3"),
    ("BUKKIT", "axolotls: 5", "axolotls: 3"),
    ("BUKKIT", "ambient: 15", "ambient: 1"),
    ("BUKKIT", "monster-spawns: 1", "monster-spawns: 10"),
    ("BUKKIT", "water-spawns: 1", "water-spawns: 400"),
    ("BUKKIT", "water-ambient-spawns: 1", "water-ambient-spawns: 400"),
    ("BUKKIT", "water-underground-creature-spawns: 1", "water-underground-creature-spawns: 400"),
    ("BUKKIT", "axolotl-spawns: 1", "axolotl-spawns: 400"),
    ("BUKKIT", "ambient-spawns: 1", "ambient-spawns: 400"),

    # ---- server.properties: DedicatedServerProperties.java (minecraft) ----
    ("DSP", 'this.getMutable("simulation-distance", 10)', 'this.getMutable("simulation-distance", 4)'),
    ("DSP", 'this.getMutable("view-distance", 10)', 'this.getMutable("view-distance", 7)'),
    ("DSP", '"spawn-protection", 16', '"spawn-protection", 0'),

    # ---- ArrowDespawnRate.java: add non-throwing factory used by the WC edits above ----
    ("ADR",
     "    private ArrowDespawnRate(Map<ContextKey<?>, Object> context) {\n"
     "        super(context, OptionalInt.empty());\n"
     "    }",
     "    private ArrowDespawnRate(Map<ContextKey<?>, Object> context) {\n"
     "        super(context, OptionalInt.empty());\n"
     "    }\n\n"
     "    private ArrowDespawnRate(Map<ContextKey<?>, Object> context, OptionalInt value) {\n"
     "        super(context, value);\n"
     "    }"),
    ("ADR",
     "    public static ArrowDespawnRate def(SpigotWorldConfig spigotConfig) {\n"
     "        return new ArrowDespawnRate(FallbackValue.SPIGOT_WORLD_CONFIG.singleton(spigotConfig));\n"
     "    }",
     "    public static ArrowDespawnRate def(SpigotWorldConfig spigotConfig) {\n"
     "        return new ArrowDespawnRate(FallbackValue.SPIGOT_WORLD_CONFIG.singleton(spigotConfig));\n"
     "    }\n\n"
     "    public static ArrowDespawnRate def(SpigotWorldConfig spigotConfig, int value) {\n"
     "        return new ArrowDespawnRate(FallbackValue.SPIGOT_WORLD_CONFIG.singleton(spigotConfig), OptionalInt.of(value));\n"
     "    }"),
]


def main():
    by_file = {}
    for fk, old, new in EDITS:
        by_file.setdefault(fk, []).append((old, new))

    total = 0
    for fk, edits in by_file.items():
        path = resolve(FILES[fk])
        with open(path, encoding="utf-8") as f:
            text = f.read()
        for old, new in edits:
            count = text.count(old)
            if count != 1:
                raise SystemExit(
                    f"[{fk}] expected exactly 1 occurrence, found {count} for:\n"
                    f"-----\n{old}\n-----\n(in {path})"
                )
            text = text.replace(old, new, 1)
            total += 1
        with open(path, "w", encoding="utf-8") as f:
            f.write(text)
        print(f"[{fk}] applied {len(edits)} edit(s) -> {path}")

    print(f"OK: {total} optimization defaults baked.")


if __name__ == "__main__":
    sys.exit(main())
