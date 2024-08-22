package com.movtery.pojavzh.feature.mod

import android.content.Context
import net.kdt.pojavlaunch.R

class ModCategory {
    companion object {
        //分为两个不同的组，用于区分Mod与ModPack
        private val modCategoriesList: MutableList<Category> = ArrayList()
        private val modpackCategoriesList: MutableList<Category> = ArrayList()

        init {
            var index = 0
            Category.entries.forEach { category ->
                if (index in 0..25) modCategoriesList.add(category)
                if (index == 0 || index >= 26) modpackCategoriesList.add(category)
                index++
            }
        }

        private fun getCategoryName(context: Context, category: Category): String {
            var name = context.getString(category.resNameID)
            if (category.retraction) name = "-\t\t$name"
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
    }

    /**
     * CurseForge与Modrinth双平台的类别汇集，记录类别所代指的实际值，以便搜索Mod或者Modpack时填入类别
     * 数据来源：https://github.com/Hex-Dragon/PCL2/blob/f40a2990103ae85c34acb5d8d367ab1644aa7ca6/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModComp.vb#L281
     * 感谢龙腾猫跃!!!!
     */
    enum class Category(val resNameID: Int, val curseforgeID: String?, val modrinthName: String?, val retraction: Boolean = false) {
        ALL(R.string.zh_all, null, null),
        MOD_WORLDGEN(R.string.zh_profile_mods_category_worldgen, "406", "worldgen"),
        MOD_BIOMES(R.string.zh_profile_mods_category_biomes, "407", null, true),
        MOD_DIMENSIONS(R.string.zh_profile_mods_category_dimensions, "410", null, true),
        MOD_ORES_RESOURCES(R.string.zh_profile_mods_category_ores_resources, "408", null, true),
        MOD_STRUCTURES(R.string.zh_profile_mods_category_structures, "409", null, true),
        MOD_TECHNOLOGY(R.string.zh_profile_mods_category_technology, "412", "technology"),
        MOD_ITEM_FLUID_ENERGY_TRANSPORT(R.string.zh_profile_mods_category_item_fluid_energy_transport, "415", null, true),
        MOD_AUTOMATION(R.string.zh_profile_mods_category_automation, "4843", null, true),
        MOD_ENERGY(R.string.zh_profile_mods_category_energy, "417", null, true),
        MOD_REDSTONE(R.string.zh_profile_mods_category_redstone, "4558", null, true),
        MOD_FOOD(R.string.zh_profile_mods_category_food, "436", "food"),
        MOD_FARMING(R.string.zh_profile_mods_category_farming, "416", null, true),
        MOD_TRANSPORT(R.string.zh_profile_mods_category_transport, "414", "transportation"),
        MOD_STORAGE(R.string.zh_profile_mods_category_storage, "420", "storage"),
        MOD_MAGIC(R.string.zh_profile_mods_category_magic, "419", "magic"),
        MOD_ADVENTURE(R.string.zh_profile_mods_category_adventure, "422", "adventure"),
        MOD_DECORATION(R.string.zh_profile_mods_category_decoration, "424", "decoration"),
        MOD_MOBS(R.string.zh_profile_mods_category_mobs, "411", "mobs"),
        MOD_EQUIPMENT(R.string.zh_profile_mods_category_equipment, "434", "equipment"),
        MOD_INFORMATION(R.string.zh_profile_mods_category_information, "423", null),
        MOD_SOCIAL(R.string.zh_profile_mods_category_social, "435", "social"),
        MOD_UTILITY(R.string.zh_profile_mods_category_utility, "5191", "utility"),
        MOD_LIBRARY(R.string.zh_profile_mods_category_library, "421", "library"),
        MOD_GAME_MECHANICS(R.string.zh_profile_mods_category_game_mechanics, null, "game-mechanics"),
        MOD_OPTIMIZATION(R.string.zh_profile_mods_category_optimization, null, "optimization"),

        MODPACK_MULTIPLAYER(R.string.zh_profile_modpacks_category_multiplayer, "4484", "multiplayer"),
        MODPACK_CHALLENGING(R.string.zh_profile_modpacks_category_challenging, "4479", "challenging"),
        MODPACK_COMBAT(R.string.zh_profile_modpacks_category_combat, "4483", "combat"),
        MODPACK_QUESTS(R.string.zh_profile_modpacks_category_quests, "4478", "quests"),
        MODPACK_TECHNOLOGY(R.string.zh_profile_modpacks_category_technology, "4472", "technology"),
        MODPACK_MAGIC(R.string.zh_profile_modpacks_category_magic, "4473", "magic"),
        MODPACK_ADVENTURE(R.string.zh_profile_modpacks_category_adventure, "4475", "adventure"),
        MODPACK_EXPLORATION(R.string.zh_profile_modpacks_category_exploration, "4476", null),
        MODPACK_MINI_GAME(R.string.zh_profile_modpacks_category_mini_game, "4477", null),
        MODPACK_SCI_FI(R.string.zh_profile_modpacks_category_sci_fi, "4471", null),
        MODPACK_SKYBLOCK(R.string.zh_profile_modpacks_category_skyblock, "4736", null),
        MODPACK_VANILLA(R.string.zh_profile_modpacks_category_vanilla, "5128", null),
        MODPACK_FTB(R.string.zh_profile_modpacks_category_ftb, "4487", null),
        MODPACK_MAP_BASED(R.string.zh_profile_modpacks_category_map_based, "4480", null),
        MODPACK_LIGHTWEIGHT(R.string.zh_profile_modpacks_category_lightweight, "4481", "lightweight"),
        MODPACK_EXTRA_LARGE(R.string.zh_profile_modpacks_category_extra_large, "4482", null),
        MODPACK_OPTIMIZATION(R.string.zh_profile_modpacks_category_optimization, null, "optimization"),
        MODPACK_KITCHEN_SINK(R.string.zh_profile_modpacks_category_kitchen_sink, null, "kitchen-sink")
    }
}