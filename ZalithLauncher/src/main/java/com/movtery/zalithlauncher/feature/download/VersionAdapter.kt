package com.movtery.zalithlauncher.feature.download

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.movtery.anim.animations.Animations
import com.movtery.zalithlauncher.feature.download.enums.VersionType
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.item.ModLikeVersionItem
import com.movtery.zalithlauncher.feature.download.item.ModVersionItem
import com.movtery.zalithlauncher.feature.download.item.VersionItem
import com.movtery.zalithlauncher.feature.download.platform.AbstractPlatformHelper
import com.movtery.zalithlauncher.ui.dialog.EditTextDialog
import com.movtery.zalithlauncher.ui.dialog.ModDependenciesDialog
import com.movtery.zalithlauncher.utils.NumberWithUnits.Companion.formatNumberWithUnit
import com.movtery.zalithlauncher.utils.ZHTools
import com.movtery.zalithlauncher.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.zalithlauncher.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.ItemModVersionBinding
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import java.io.File
import java.util.Locale
import java.util.StringJoiner
import java.util.TimeZone

class VersionAdapter(
    private val parentFragment: Fragment,
    private val infoItem: InfoItem,
    private val platformHelper: AbstractPlatformHelper,
    private val mData: List<VersionItem>?,
    private val targetFile: File?
) : RecyclerView.Adapter<VersionAdapter.InnerHolder>(), TaskCountListener {
    private var mTasksRunning = false

    init {
        ProgressKeeper.addTaskCountListener(this)
        mData?.sortedWith { o1, o2 ->  //按照日期进行一波排序
            o1.uploadDate.compareTo(o2.uploadDate)
        }
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
        private val mContext: Context = itemView.context

        fun setData(versionItem: VersionItem) {
            binding.downloadImageview.setImageResource(getDownloadType(versionItem.versionType))

            binding.titleTextview.text = versionItem.title

            val downloadCountText = formatNumberWithUnit(versionItem.downloadCount, ZHTools.isEnglish(mContext))
            val dateText = StringUtils.formatDate(versionItem.uploadDate, Locale.getDefault(),
                TimeZone.getDefault())

            binding.tagsLayout.removeAllViews()
            binding.tagsLayout.addView(getTagTextView(R.string.download_info_downloads, downloadCountText))
            binding.tagsLayout.addView(getTagTextView(R.string.download_info_date, dateText))

            if (versionItem is ModLikeVersionItem) {
                val sj = StringJoiner(", ")
                for (modloader in versionItem.modloaders) {
                    sj.add(modloader.loaderName)
                }
                val modloaderText = if (sj.length() > 0) sj.toString()
                else mContext.getString(R.string.generic_unknown)
                binding.tagsLayout.addView(getTagTextView(R.string.download_info_modloader, modloaderText))
            }
            binding.tagsLayout.addView(getTagTextView(getDownloadTypeText(versionItem.versionType)))

            itemView.setOnClickListener { _: View? ->
                if (mTasksRunning) {
                    setViewAnim(itemView, Animations.Shake)
                    Toast.makeText(mContext, mContext.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (versionItem is ModVersionItem && versionItem.dependencies.isNotEmpty()) {
                    ModDependenciesDialog(parentFragment, infoItem, versionItem.dependencies) {
                        preInstall(versionItem)
                    }.show()
                } else {
                    preInstall(versionItem)
                }
            }
        }

        private fun preInstall(versionItem: VersionItem) {
            targetFile?.let {
                val file = File(versionItem.fileName)

                EditTextDialog.Builder(mContext)
                    .setTitle(R.string.download_install_custom_name)
                    .setEditText(file.nameWithoutExtension)
                    .setConfirmListener { editText: EditText ->
                        val string = editText.text.toString()
                        if (string.contains("/")) {
                            editText.error = mContext.getString(
                                R.string.generic_input_invalid_character,
                                "/"
                            )
                            return@setConfirmListener false
                        }

                        val installFile = File(it, "${string}.${file.extension}")
                        println(installFile)
                        startInstall(versionItem, installFile)
                        true
                    }.buildDialog()
                return
            }

            startInstall(versionItem, null)
        }

        private fun startInstall(versionItem: VersionItem, targetFile: File?) {
            platformHelper.install(mContext, infoItem, versionItem, targetFile)
        }

        private fun getDownloadType(versionType: VersionType): Int {
            return when (versionType) {
                VersionType.BETA -> R.drawable.ic_download_beta
                VersionType.ALPHA -> R.drawable.ic_download_alpha
                VersionType.RELEASE -> R.drawable.ic_download_release
            }
        }

        private fun getDownloadTypeText(versionType: VersionType): String {
            val text = when (versionType) {
                VersionType.RELEASE -> mContext.getString(R.string.version_release)
                VersionType.BETA -> mContext.getString(R.string.download_info_release_type_beta)
                VersionType.ALPHA -> mContext.getString(R.string.download_info_release_type_alpha)
            }
            return text
        }

        private fun getTagTextView(string: Int, value: String): TextView {
            return getTagTextView(StringUtils.insertSpace(mContext.getString(string), value))
        }

        private fun getTagTextView(value: String): TextView {
            val textView = TextView(mContext)
            textView.text = value
            val layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, Tools.dpToPx(10f).toInt(), 0)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Tools.dpToPx(9F))
            textView.layoutParams = layoutParams
            return textView
        }
    }
}
