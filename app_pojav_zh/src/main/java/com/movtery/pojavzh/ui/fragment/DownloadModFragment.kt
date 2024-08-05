package com.movtery.pojavzh.ui.fragment

import android.graphics.Bitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies.SelectedMod
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionAdapter
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import com.movtery.pojavzh.ui.subassembly.viewmodel.ModApiViewModel
import com.movtery.pojavzh.ui.subassembly.viewmodel.RecyclerViewModel
import com.movtery.pojavzh.utils.MCVersionComparator.versionCompare
import com.movtery.pojavzh.utils.MCVersionRegex.RELEASE_REGEX
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ImageReceiver
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem
import java.util.Collections
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadModFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadModFragment"
    }

    private val mIconCache = ModIconCache()
    private var mParentUIRecyclerView: RecyclerView? = null
    private var mModItem: ModItem? = null
    private var mModApi: ModpackApi? = null
    private var mImageReceiver: ImageReceiver? = null
    private var mIsModpack = false
    private var mModsPath: String? = null

    override fun init() {
        parseViewModel()
        super.init()
    }

    override fun refresh(): Future<*> {
        return PojavApplication.sExecutorService.submit {
            try {
                Tools.runOnUiThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val mModDetail = mModApi!!.getModDetails(mModItem)
                processModDetails(mModDetail)
            } catch (e: Exception) {
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        mParentUIRecyclerView?.isEnabled = true
        super.onDestroy()
    }

    private fun processModDetails(mModDetail: ModDetail) {
        val currentTask = currentTask
        val pattern = RELEASE_REGEX

        val releaseCheckBoxChecked = releaseCheckBox!!.isChecked
        val mModVersionsByMinecraftVersion: MutableMap<String, MutableList<ModVersionItem>> = HashMap()

        mModDetail.modVersionItems.forEach(Consumer { modVersionItem: ModVersionItem ->
            if (currentTask!!.isCancelled) return@Consumer

            val versionId = modVersionItem.versionId
            for (mcVersion in versionId) {
                if (currentTask.isCancelled) return@Consumer

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

        if (currentTask!!.isCancelled) return

        val mData: MutableList<ModListItemBean> = ArrayList()
        mModVersionsByMinecraftVersion.entries
            .sortedWith { entry1, entry2 ->
                versionCompare(entry1.key, entry2.key)
            }
            .forEach { entry: Map.Entry<String, List<ModVersionItem>> ->
                if (currentTask.isCancelled) return@forEach

                mData.add(ModListItemBean("Minecraft " + entry.key,
                    ModVersionAdapter(SelectedMod(this@DownloadModFragment,
                        mModItem!!.title, mModApi, mIsModpack, mModsPath), mModDetail, entry.value)
                    )
                )
            }

        if (currentTask.isCancelled) return

        Tools.runOnUiThread {
            val modVersionView = recyclerView
            try {
                var mModAdapter = modVersionView!!.adapter as ModListAdapter?
                if (mModAdapter == null) {
                    mModAdapter = ModListAdapter(this, mData)
                    modVersionView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    modVersionView.adapter = mModAdapter
                } else {
                    mModAdapter.updateData(mData)
                }
            } catch (ignored: Exception) {
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

        setNameText(mModItem!!.title)

        mImageReceiver = ImageReceiver { bm: Bitmap ->
            mImageReceiver = null
            val drawable = RoundedBitmapDrawableFactory.create(resources, bm)
            drawable.cornerRadius = resources.getDimension(R.dimen._1sdp) / 250 * bm.height
            setIcon(drawable)
        }
        mIconCache.getImage(mImageReceiver, mModItem!!.iconCacheTag, mModItem!!.imageUrl)
    }
}
