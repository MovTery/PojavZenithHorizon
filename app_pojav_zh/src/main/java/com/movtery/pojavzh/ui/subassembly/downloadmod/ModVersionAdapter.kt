package com.movtery.pojavzh.ui.subassembly.downloadmod

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mod_version, parent, false)
        return InnerHolder(view)
    }

    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.setData(mData!![position])
    }

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }

    inner class InnerHolder(private val mainView: View) : RecyclerView.ViewHolder(
        mainView
    ) {
        private val context: Context = itemView.context
        private val mImageView: ImageView = itemView.findViewById(R.id.mod_download_imageview)
        private val mTitle: TextView = itemView.findViewById(R.id.mod_title_textview)
        private val mDownloadCount: TextView = itemView.findViewById(R.id.zh_mod_download_count_textview)
        private val mModloaders: TextView = itemView.findViewById(R.id.zh_mod_modloader_textview)
        private val mReleaseType: TextView = itemView.findViewById(R.id.zh_mod_release_type_textview)

        fun setData(modVersionItem: ModVersionItem) {
            mImageView.setImageResource(getDownloadType(modVersionItem.versionType))

            mTitle.text = modVersionItem.title

            val downloadCountText = StringUtils.insertSpace(
                context.getString(R.string.zh_profile_mods_information_download_count),
                formatNumberWithUnit(modVersionItem.download.toLong(), ZHTools.isEnglish(context))
            )
            mDownloadCount.text = downloadCountText

            val sj = StringJoiner(", ")
            for (modloader in modVersionItem.modloaders) {
                sj.add(modloader.loaderName)
            }
            val modloaderText = if (sj.length() > 0) sj.toString()
            else context.getString(R.string.zh_unknown)

            mModloaders.text = modloaderText

            mReleaseType.text = getDownloadTypeText(modVersionItem.versionType)

            mainView.setOnClickListener { _: View? ->
                if (mTasksRunning) {
                    setViewAnim(mainView, Techniques.Shake)
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (modVersionItem.modDependencies.isNotEmpty()) {
                    val dependenciesDialog = ModDependenciesDialog(context, mod, modVersionItem.modDependencies) {
                        Tools.runOnUiThread { customFileName(modVersionItem) }
                    }
                    dependenciesDialog.show()
                    return@setOnClickListener
                }

                customFileName(modVersionItem)
            }
        }

        private fun customFileName(modVersionItem: ModVersionItem) {
            EditTextDialog.Builder(context)
                .setTitle(R.string.zh_profile_mods_download_mod_custom_name)
                .setEditText(
                    ("[${modDetail.subTitle ?: modDetail.title}] ${modVersionItem.name}")
                        .replace("/", "-").removeSuffix(".jar")
                )
                .setConfirmListener { editText: EditText ->
                    val string = editText.text.toString()
                    if (string.contains("/")) {
                        editText.error = context.getString(
                            R.string.zh_profile_mods_download_mod_custom_name_invalid,
                            "/"
                        )
                        return@setConfirmListener false
                    }

                    mod.api.handleInstallation(context, mod.isModpack, mod.modsPath, string, modDetail, modVersionItem)
                    true
                }.buildDialog()
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
                VersionTypeEnum.RELEASE -> mainView.context.getString(R.string.zh_profile_mods_information_release_type_release)
                VersionTypeEnum.BETA -> mainView.context.getString(R.string.zh_profile_mods_information_release_type_beta)
                VersionTypeEnum.ALPHA -> mainView.context.getString(R.string.zh_profile_mods_information_release_type_alpha)
            }
            return text
        }
    }
}
