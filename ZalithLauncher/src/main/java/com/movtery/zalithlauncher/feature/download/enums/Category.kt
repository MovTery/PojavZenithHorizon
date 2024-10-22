package com.movtery.zalithlauncher.feature.download.enums

import net.kdt.pojavlaunch.R

/**
 * CurseForge与Modrinth双平台的类别汇集，记录类别所代指的实际值，以便搜索资源时填入类别。
 * 如果某类别的curseforge id或modrinth id为null，那么就代表此类别在该平台不存在
 */
enum class Category(val classify: Classify, val resNameID: Int, val curseforgeID: String?, val modrinthName: String?, val retraction: Boolean = false) {
    ALL(Classify.ALL, R.string.generic_all, null, null),

    //Mod与ModPack数据来源：https://github.com/Hex-Dragon/PCL2/blob/f40a2990103ae85c34acb5d8d367ab1644aa7ca6/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModComp.vb#L281
    //感谢龙腾猫跃!!!!
    //Mod类别
    MOD_WORLDGEN(Classify.MOD, R.string.category_worldgen, "406", "worldgen"),
    MOD_BIOMES(Classify.MOD, R.string.category_biomes, "407", null, true),
    MOD_DIMENSIONS(Classify.MOD, R.string.category_dimensions, "410", null, true),
    MOD_ORES_RESOURCES(Classify.MOD, R.string.category_ores_resources, "408", null, true),
    MOD_STRUCTURES(Classify.MOD, R.string.category_structures, "409", null, true),
    MOD_TECHNOLOGY(Classify.MOD, R.string.category_technology, "412", "technology"),
    MOD_ITEM_FLUID_ENERGY_TRANSPORT(Classify.MOD, R.string.category_item_fluid_energy_transport, "415", null, true),
    MOD_AUTOMATION(Classify.MOD, R.string.category_automation, "4843", null, true),
    MOD_ENERGY(Classify.MOD, R.string.category_energy, "417", null, true),
    MOD_REDSTONE(Classify.MOD, R.string.category_redstone, "4558", null, true),
    MOD_FOOD(Classify.MOD, R.string.category_food, "436", "food"),
    MOD_FARMING(Classify.MOD, R.string.category_farming, "416", null, true),
    MOD_GAME_MECHANICS(Classify.MOD, R.string.category_game_mechanics, null, "game-mechanics"),
    MOD_TRANSPORT(Classify.MOD, R.string.category_transport, "414", "transportation"),
    MOD_STORAGE(Classify.MOD, R.string.category_storage, "420", "storage"),
    MOD_MAGIC(Classify.MOD, R.string.category_magic, "419", "magic"),
    MOD_ADVENTURE(Classify.MOD, R.string.category_adventure, "422", "adventure"),
    MOD_DECORATION(Classify.MOD, R.string.category_decoration, "424", "decoration"),
    MOD_MOBS(Classify.MOD, R.string.category_mobs, "411", "mobs"),
    MOD_EQUIPMENT(Classify.MOD, R.string.category_equipment, "434", "equipment"),
    MOD_OPTIMIZATION(Classify.MOD, R.string.category_optimization, null, "optimization"),
    MOD_INFORMATION(Classify.MOD, R.string.category_information, "423", null),
    MOD_SOCIAL(Classify.MOD, R.string.category_social, "435", "social"),
    MOD_UTILITY(Classify.MOD, R.string.category_utility, "5191", "utility"),
    MOD_LIBRARY(Classify.MOD, R.string.category_library, "421", "library"),

    //整合包类别
    MODPACK_MULTIPLAYER(Classify.MODPACK, R.string.category_multiplayer, "4484", "multiplayer"),
    MODPACK_OPTIMIZATION(Classify.MODPACK, R.string.category_optimization, null, "optimization"),
    MODPACK_CHALLENGING(Classify.MODPACK, R.string.category_challenging, "4479", "challenging"),
    MODPACK_COMBAT(Classify.MODPACK, R.string.category_combat, "4483", "combat"),
    MODPACK_QUESTS(Classify.MODPACK, R.string.category_quests, "4478", "quests"),
    MODPACK_TECHNOLOGY(Classify.MODPACK, R.string.category_technology, "4472", "technology"),
    MODPACK_MAGIC(Classify.MODPACK, R.string.category_magic, "4473", "magic"),
    MODPACK_ADVENTURE(Classify.MODPACK, R.string.category_adventure, "4475", "adventure"),
    MODPACK_KITCHEN_SINK(Classify.MODPACK, R.string.category_kitchen_sink, null, "kitchen-sink"),
    MODPACK_EXPLORATION(Classify.MODPACK, R.string.category_exploration, "4476", null),
    MODPACK_MINI_GAME(Classify.MODPACK, R.string.category_mini_game, "4477", null),
    MODPACK_SCI_FI(Classify.MODPACK, R.string.category_sci_fi, "4471", null),
    MODPACK_SKYBLOCK(Classify.MODPACK, R.string.category_skyblock, "4736", null),
    MODPACK_VANILLA(Classify.MODPACK, R.string.category_vanilla, "5128", null),
    MODPACK_FTB(Classify.MODPACK, R.string.category_ftb, "4487", null),
    MODPACK_MAP_BASED(Classify.MODPACK, R.string.category_map_based, "4480", null),
    MODPACK_LIGHTWEIGHT(Classify.MODPACK, R.string.category_lightweight, "4481", "lightweight"),
    MODPACK_EXTRA_LARGE(Classify.MODPACK, R.string.category_extra_large, "4482", null),

    //资源包类别
    RP_TRADITIONAL(Classify.RESOURCE_PACK, R.string.category_traditional, "403", null),
    RP_STEAMPUNK(Classify.RESOURCE_PACK, R.string.category_steampunk, "399", null),
    RP_COMBAT(Classify.RESOURCE_PACK, R.string.category_combat, null, "combat"),
    RP_DECORATION(Classify.RESOURCE_PACK, R.string.category_decoration, null, "decoration"),
    RP_VANILLA(Classify.RESOURCE_PACK, R.string.category_vanilla, null, "vanilla-like"),
    RP_MODERN(Classify.RESOURCE_PACK, R.string.category_modern, "401", null),
    RP_PHOTO_REALISTIC(Classify.RESOURCE_PACK, R.string.category_photo_realistic, "400", "realistic"),
    RP_ANIMATED(Classify.RESOURCE_PACK, R.string.category_animated, "", null),
    RP_MOD_SUPPORT(Classify.RESOURCE_PACK, R.string.category_mod_support, "4465", "modded"),
    RP_MISCELLANEOUS(Classify.RESOURCE_PACK, R.string.category_miscellaneous, "405", null),
    RP_16X(Classify.RESOURCE_PACK, R.string.category_16x, "393", null),
    RP_32X(Classify.RESOURCE_PACK, R.string.category_32x, "394", null),
    RP_64X(Classify.RESOURCE_PACK, R.string.category_64x, "395", null),
    RP_128X(Classify.RESOURCE_PACK, R.string.category_128x, "396", null),
    RP_256X(Classify.RESOURCE_PACK, R.string.category_256x, "397", null),
    RP_512X(Classify.RESOURCE_PACK, R.string.category_512x, "398", null),

    //世界类别
    WORLD_ADVENTURE(Classify.WORLD, R.string.category_world_adventure, "253", null),
    WORLD_SURVIVAL(Classify.WORLD, R.string.category_survival, "248", null),
    WORLD_CREATION(Classify.WORLD, R.string.category_creation, "249", null),
    WORLD_GAME_MAP(Classify.WORLD, R.string.category_game_map, "250", null),
    WORLD_MODDED_WORLD(Classify.WORLD, R.string.category_modded_world, "4464", null),
    WORLD_PARKOUR(Classify.WORLD, R.string.category_parkour, "251", null),
    WORLD_PUZZLE(Classify.WORLD, R.string.category_puzzle, "252", null)
}