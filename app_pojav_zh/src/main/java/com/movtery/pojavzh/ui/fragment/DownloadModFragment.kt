package com.movtery.pojavzh.ui.fragment

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies.SelectedMod
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionAdapter
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import com.movtery.pojavzh.ui.subassembly.viewmodel.ModApiViewModel
import com.movtery.pojavzh.ui.subassembly.viewmodel.RecyclerViewModel
import com.movtery.pojavzh.utils.MCVersionRegex.Companion.RELEASE_REGEX
import com.movtery.pojavzh.utils.image.ImageUtils
import com.movtery.pojavzh.utils.image.UrlImageCallback
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem
import org.jackhuang.hmcl.util.versioning.VersionNumber
import java.util.Collections
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadModFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadModFragment"
    }

    private var mParentUIRecyclerView: RecyclerView? = null
    private var mModItem: ModItem? = null
    private var mModApi: ModpackApi? = null
    private var mIsModpack = false
    private var mModsPath: String? = null
    private var linkGetSubmit: Future<*>? = null

    override fun init() {
        parseViewModel()
        linkGetSubmit = PojavApplication.sExecutorService.submit {
            runCatching {
                val webUrl = mModApi!!.getWebUrl(mModItem)
                fragmentActivity?.runOnUiThread { setLink(webUrl) }
            }.getOrElse { e ->
                Logging.e("DownloadModFragment", "Failed to retrieve the website link, ${Tools.printToString(e)}")
            }
        }
        super.init()
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    override fun onDestroy() {
        mParentUIRecyclerView?.isEnabled = true
        linkGetSubmit?.apply {
            if (!isCancelled && !isDone) cancel(true)
        }
        super.onDestroy()
    }

    private fun refresh(force: Boolean): Future<*> {
        return PojavApplication.sExecutorService.submit {
            runCatching {
                Tools.runOnUiThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val mModDetail = mModApi!!.getModDetails(mModItem, force)
                processModDetails(mModDetail)
            }.getOrElse { e ->
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadModFragment", Tools.printToString(e))
            }
        }
    }

    private fun processModDetails(mModDetail: ModDetail) {
        val pattern = RELEASE_REGEX

        val releaseCheckBoxChecked = releaseCheckBox!!.isChecked
        val mModVersionsByMinecraftVersion: MutableMap<String, MutableList<ModVersionItem>> = HashMap()

        mModDetail.modVersionItems.forEach(Consumer { modVersionItem: ModVersionItem ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            val versionId = modVersionItem.versionId
            for (mcVersion in versionId) {
                currentTask?.apply { if (isCancelled) return@Consumer }

                if (releaseCheckBoxChecked) {
                    val matcher = pattern.matcher(mcVersion)
                    if (!matcher.matches()) {
                        //如果不是正式版本，将继续检测下一项
                        continue
                    }
                }

                mModVersionsByMinecraftVersion.computeIfAbsent(mcVersion) { Collections.synchronizedList(ArrayList()) }
                    .add(modVersionItem) //将Mod 版本数据加入到相应的版本号分组中
            }
        })

        currentTask?.apply { if (isCancelled) return }

        val mData: MutableList<ModListItemBean> = ArrayList()
        mModVersionsByMinecraftVersion.entries
            .sortedWith { entry1, entry2 -> -VersionNumber.compare(entry1.key, entry2.key) }
            .forEach { entry: Map.Entry<String, List<ModVersionItem>> ->
                currentTask?.apply { if (isCancelled) return }

                mData.add(ModListItemBean("Minecraft " + entry.key,
                    ModVersionAdapter(SelectedMod(this@DownloadModFragment,
                        mModItem!!.title, mModApi, mIsModpack, mModsPath), mModDetail, entry.value)
                    )
                )
            }

        currentTask?.apply { if (isCancelled) return }

        Tools.runOnUiThread {
            val modVersionView = recyclerView
            runCatching {
                var mModAdapter = modVersionView!!.adapter as ModListAdapter?
                mModAdapter ?: run {
                    mModAdapter = ModListAdapter(this, mData)
                    modVersionView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    modVersionView.adapter = mModAdapter
                    return@runCatching
                }
                mModAdapter?.updateData(mData)
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            modVersionView?.scheduleLayoutAnimation()
        }
    }

    private fun parseViewModel() {
        val viewModel = ViewModelProvider(fragmentActivity!!)[ModApiViewModel::class.java]
        val recyclerViewModel = ViewModelProvider(fragmentActivity!!)[RecyclerViewModel::class.java]
        mModApi = viewModel.modApi
        mModItem = viewModel.modItem
        mIsModpack = viewModel.isModpack
        mModsPath = viewModel.modsPath
        mParentUIRecyclerView = recyclerViewModel.view

        mModItem?.let { item ->
            setNameText(item.subTitle ?: item.title)
            setSubTitleText(item.subTitle?.let { item.title })
        }

        mModItem?.imageUrl?.apply {
            ImageUtils.loadDrawableFromUrl(fragmentActivity!!, this, object : UrlImageCallback {
                override fun onImageLoaded(drawable: Drawable?, url: String) {
                    setIcon(drawable)
                }

                override fun onImageCleared(placeholder: Drawable?, url: String) {
                    setIcon(placeholder)
                }
            })
        }
    }
}
