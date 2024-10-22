package com.movtery.zalithlauncher.feature.download.utils

import android.content.Context
import com.movtery.zalithlauncher.feature.download.enums.DependencyType
import net.kdt.pojavlaunch.R

class DependencyUtils {
    companion object {
        //1 = EmbeddedLibrary
        //2 = OptionalDependency
        //3 = RequiredDependency
        //4 = Tool
        //5 = Incompatible
        //6 = Include
        fun getDependencyType(type: String?): DependencyType {
            return when (type) {
                "optional", "2" -> DependencyType.OPTIONAL
                "incompatible", "5" -> DependencyType.INCOMPATIBLE
                "embedded", "1" -> DependencyType.EMBEDDED
                "4" -> DependencyType.TOOL
                "6" -> DependencyType.INCLUDE
                "required", "3" -> DependencyType.REQUIRED
                else -> DependencyType.REQUIRED
            }
        }

        fun getTextFromType(context: Context, type: DependencyType?): String {
            return when (type) {
                DependencyType.OPTIONAL -> context.getString(R.string.download_install_dependencies_optional)
                DependencyType.INCOMPATIBLE -> context.getString(R.string.download_install_dependencies_incompatible)
                DependencyType.EMBEDDED -> context.getString(R.string.download_install_dependencies_embedded)
                DependencyType.TOOL -> context.getString(R.string.download_install_dependencies_tool)
                DependencyType.INCLUDE -> context.getString(R.string.download_install_dependencies_include)
                DependencyType.REQUIRED -> context.getString(R.string.download_install_dependencies_required)
                else -> context.getString(R.string.download_install_dependencies_required)
            }
        }
    }
}