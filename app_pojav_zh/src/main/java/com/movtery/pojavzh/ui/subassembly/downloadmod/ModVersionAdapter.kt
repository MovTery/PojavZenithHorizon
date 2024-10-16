package com.movtery.pojavzh.ui.subassembly.downloadmod

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.dialog.ModDependenciesDialog
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies.SelectedMod
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType.VersionTypeEnum
import com.movtery.pojavzh.utils.NumberWithUnits.Companion.formatNumberWithUnit
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.ItemModVersionBinding
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import java.util.StringJoiner

class ModVersionAdapter(
    private val mod: SelectedMod,
    private val modDetail: ModDetail,
    private val mData: List<ModVersionItem>?
) : RecyclerView.Adapter<ModVersionAdapter.InnerHolder>(), TaskCountListener {
    private var mTasksRunning = false

    init {
        ProgressKeeper.addTaskCountListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerHolder {
        return InnerHolder(ItemModVersionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.setData(mData!![position])
    }

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }

    inner class InnerHolder(private val binding: ItemModVersionBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private val context: Context = itemView.context

        fun setData(modVersionItem: ModVersionItem) {
            binding.downloadImageview.setImageResource(getDownloadType(modVersionItem.versionType))

            binding.titleTextview.text = modVersionItem.title

            val downloadCountText = StringUtils.insertSpace(
                context.getString(R.string.profile_mods_information_download_count),
                formatNumberWithUnit(modVersionItem.download.toLong(), ZHTools.isEnglish(context))
            )
            binding.downloadCountTextview.text = downloadCountText

            val sj = StringJoiner(", ")
            for (modloader in modVersionItem.modloaders) {
                sj.add(modloader.loaderName)
            }
            val modloaderText = if (sj.length() > 0) sj.toString()
            else context.getString(R.string.generic_unknown)

            binding.modloaderTextview.text = modloaderText

            binding.releaseTypeTextview.text = getDownloadTypeText(modVersionItem.versionType)

            itemView.setOnClickListener { _: View? ->
                if (mTasksRunning) {
                    setViewAnim(itemView, Animations.Shake)
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (modVersionItem.modDependencies.isNotEmpty()) {
                    val dependenciesDialog = ModDependenciesDialog(context, mod, modVersionItem.modDependencies) {
                        Tools.runOnUiThread { startInstall(modVersionItem) }
                    }
                    dependenciesDialog.show()
                    return@setOnClickListener
                }

                startInstall(modVersionItem)
            }
        }

        private fun startInstall(modVersionItem: ModVersionItem) {
            fun start(fileName: String) {
                mod.api.handleInstallation(context, mod.isModpack, mod.modsPath, fileName, modDetail, modVersionItem)
            }

            if (mod.isModpack) {
                start(modVersionItem.name)
            } else {
                EditTextDialog.Builder(context)
                    .setTitle(R.string.profile_mods_download_mod_custom_name)
                    .setEditText(
                        ("[${modDetail.subTitle ?: modDetail.title}] ${modVersionItem.name}")
                            .replace("/", "-").removeSuffix(".jar")
                    )
                    .setConfirmListener { editText: EditText ->
                        val string = editText.text.toString()
                        if (string.contains("/")) {
                            editText.error = context.getString(
                                R.string.profile_mods_download_mod_custom_name_invalid,
                                "/"
                            )
                            return@setConfirmListener false
                        }

                        start(string)
                        true
                    }.buildDialog()
            }
        }

        private fun getDownloadType(versionType: VersionTypeEnum): Int {
            return when (versionType) {
                VersionTypeEnum.BETA -> R.drawable.ic_download_beta
                VersionTypeEnum.ALPHA -> R.drawable.ic_download_alpha
                VersionTypeEnum.RELEASE -> R.drawable.ic_download_release
            }
        }

        private fun getDownloadTypeText(versionType: VersionTypeEnum): String {
            val text = when (versionType) {
                VersionTypeEnum.RELEASE -> context.getString(R.string.version_release)
                VersionTypeEnum.BETA -> context.getString(R.string.profile_mods_information_release_type_beta)
                VersionTypeEnum.ALPHA -> context.getString(R.string.profile_mods_information_release_type_alpha)
            }
            return text
        }
    }
}
