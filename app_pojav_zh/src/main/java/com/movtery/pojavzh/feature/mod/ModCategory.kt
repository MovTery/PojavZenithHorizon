package com.movtery.pojavzh.feature.mod

import android.content.Context
import net.kdt.pojavlaunch.R

class ModCategory {
    companion object {
        //分为两个不同的组，用于区分Mod与ModPack
        private val modCategoriesList: MutableList<Category> = ArrayList()
        private val modpackCategoriesList: MutableList<Category> = ArrayList()

        init {
            Category.entries.forEach { category ->
                if (category.classify == Classify.ALL || category.classify == Classify.MOD) modCategoriesList.add(category)
                if (category.classify == Classify.ALL || category.classify == Classify.MODPACK) modpackCategoriesList.add(category)
            }
        }

        private fun getCategoryName(context: Context, category: Category): String {
            var name = context.getString(category.resNameID)
            if (category.retraction) name = "-\t$name"
            return name
        }

        @JvmStatic
        fun getModCategories(context: Context): List<String> {
            val namesList: MutableList<String> = ArrayList()
            modCategoriesList.forEach {
                namesList.add(getCategoryName(context, it))
            }
            return namesList
        }

        @JvmStatic
        fun getModPackCategories(context: Context): List<String> {
            val namesList: MutableList<String> = ArrayList()
            modpackCategoriesList.forEach {
                namesList.add(getCategoryName(context, it))
            }
            return namesList
        }

        @JvmStatic
        fun getModCategoryFromIndex(index: Int): Category {
            return modCategoriesList[index]
        }

        @JvmStatic
        fun getModPackCategoryFromIndex(index: Int): Category {
            return modpackCategoriesList[index]
        }

        @JvmStatic
        fun getCategoryFromCurseForgeId(id: String): Category? {
            Category.entries.forEach { category ->
                if (category.curseforgeID == id) return category
            }
            return null
        }

        @JvmStatic
        fun getCategoryFromModrinthName(name: String): Category? {
            Category.entries.forEach { category ->
                if (category.modrinthName == name) return category
            }
            return null
        }
    }

    /**
     * CurseForge与Modrinth双平台的类别汇集，记录类别所代指的实际值，以便搜索Mod或者Modpack时填入类别。
     * 如果某类别的curseforge id或modrinth id为null，那么就代表此类别在该平台不存在
     * 数据来源：https://github.com/Hex-Dragon/PCL2/blob/f40a2990103ae85c34acb5d8d367ab1644aa7ca6/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModComp.vb#L281
     * 感谢龙腾猫跃!!!!
     */
    enum class Category(val classify: Classify, val resNameID: Int, val curseforgeID: String?, val modrinthName: String?, val retraction: Boolean = false) {
        ALL(Classify.ALL, R.string.generic_all, null, null),
        //Mod类别
        WORLDGEN(Classify.MOD, R.string.mods_category_worldgen, "406", "worldgen"),
        BIOMES(Classify.MOD, R.string.mods_category_biomes, "407", null, true),
        DIMENSIONS(Classify.MOD, R.string.mods_category_dimensions, "410", null, true),
        ORES_RESOURCES(Classify.MOD, R.string.mods_category_ores_resources, "408", null, true),
        STRUCTURES(Classify.MOD, R.string.mods_category_structures, "409", null, true),
        MOD_TECHNOLOGY(Classify.MOD, R.string.mods_category_technology, "412", "technology"),
        ITEM_FLUID_ENERGY_TRANSPORT(Classify.MOD, R.string.mods_category_item_fluid_energy_transport, "415", null, true),
        AUTOMATION(Classify.MOD, R.string.mods_category_automation, "4843", null, true),
        ENERGY(Classify.MOD, R.string.mods_category_energy, "417", null, true),
        REDSTONE(Classify.MOD, R.string.mods_category_redstone, "4558", null, true),
        FOOD(Classify.MOD, R.string.mods_category_food, "436", "food"),
        FARMING(Classify.MOD, R.string.mods_category_farming, "416", null, true),
        GAME_MECHANICS(Classify.MOD, R.string.mods_category_game_mechanics, null, "game-mechanics"),
        TRANSPORT(Classify.MOD, R.string.mods_category_transport, "414", "transportation"),
        STORAGE(Classify.MOD, R.string.mods_category_storage, "420", "storage"),
        MOD_MAGIC(Classify.MOD, R.string.mods_category_magic, "419", "magic"),
        MOD_ADVENTURE(Classify.MOD, R.string.mods_category_adventure, "422", "adventure"),
        DECORATION(Classify.MOD, R.string.mods_category_decoration, "424", "decoration"),
        MOBS(Classify.MOD, R.string.mods_category_mobs, "411", "mobs"),
        EQUIPMENT(Classify.MOD, R.string.mods_category_equipment, "434", "equipment"),
        MOD_OPTIMIZATION(Classify.MOD, R.string.mods_category_optimization, null, "optimization"),
        INFORMATION(Classify.MOD, R.string.mods_category_information, "423", null),
        SOCIAL(Classify.MOD, R.string.mods_category_social, "435", "social"),
        UTILITY(Classify.MOD, R.string.mods_category_utility, "5191", "utility"),
        LIBRARY(Classify.MOD, R.string.mods_category_library, "421", "library"),
        //整合包类别
        MULTIPLAYER(Classify.MODPACK, R.string.modpacks_category_multiplayer, "4484", "multiplayer"),
        MODPACK_OPTIMIZATION(Classify.MODPACK, R.string.modpacks_category_optimization, null, "optimization"),
        CHALLENGING(Classify.MODPACK, R.string.modpacks_category_challenging, "4479", "challenging"),
        COMBAT(Classify.MODPACK, R.string.modpacks_category_combat, "4483", "combat"),
        QUESTS(Classify.MODPACK, R.string.modpacks_category_quests, "4478", "quests"),
        MODPACK_TECHNOLOGY(Classify.MODPACK, R.string.modpacks_category_technology, "4472", "technology"),
        MODPACK_MAGIC(Classify.MODPACK, R.string.modpacks_category_magic, "4473", "magic"),
        MODPACK_ADVENTURE(Classify.MODPACK, R.string.modpacks_category_adventure, "4475", "adventure"),
        KITCHEN_SINK(Classify.MODPACK, R.string.modpacks_category_kitchen_sink, null, "kitchen-sink"),
        EXPLORATION(Classify.MODPACK, R.string.modpacks_category_exploration, "4476", null),
        MINI_GAME(Classify.MODPACK, R.string.modpacks_category_mini_game, "4477", null),
        SCI_FI(Classify.MODPACK, R.string.modpacks_category_sci_fi, "4471", null),
        SKYBLOCK(Classify.MODPACK, R.string.modpacks_category_skyblock, "4736", null),
        VANILLA(Classify.MODPACK, R.string.modpacks_category_vanilla, "5128", null),
        FTB(Classify.MODPACK, R.string.modpacks_category_ftb, "4487", null),
        MAP_BASED(Classify.MODPACK, R.string.modpacks_category_map_based, "4480", null),
        LIGHTWEIGHT(Classify.MODPACK, R.string.modpacks_category_lightweight, "4481", "lightweight"),
        EXTRA_LARGE(Classify.MODPACK, R.string.modpacks_category_extra_large, "4482", null)
    }

    enum class Classify {
        ALL, MOD, MODPACK
    }
}