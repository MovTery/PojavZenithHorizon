package com.movtery.pojavzh.ui.fragment

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.mod.modloader.BaseModVersionListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import com.movtery.pojavzh.utils.MCVersionComparator
import com.movtery.pojavzh.utils.MCVersionRegex
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.FabricVersion
import net.kdt.pojavlaunch.modloaders.FabriclikeDownloadTask
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import java.io.File
import java.util.concurrent.Future

abstract class DownloadFabricLikeFragment(val utils: FabriclikeUtils, val icon: Int) : ModListFragment(), ModloaderDownloadListener {
    private val modloaderListenerProxy = ModloaderListenerProxy()

    override fun init() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, icon))
        setNameText(utils.name)
        super.init()
    }

    override fun refresh(): Future<*> {
        return PojavApplication.sExecutorService.submit {
            try {
                Tools.runOnUiThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val gameVersions = utils.downloadGameVersions()
                processInfo(gameVersions)
            } catch (e: Exception) {
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
            }
        }
    }

    private fun processInfo(gameVersions: Array<FabricVersion>) {
        if (gameVersions.isEmpty()) {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("gameVersions is Empty!")
            }
            return
        }

        val releaseCheckBoxChecked = releaseCheckBox!!.isChecked
        val pattern = MCVersionRegex.RELEASE_REGEX

        val mFabricVersions: MutableMap<String, List<FabricVersion>> = HashMap()
        val loaderVersions: Array<FabricVersion>? = utils.downloadLoaderVersions()
        gameVersions.forEach {
            if (currentTask!!.isCancelled) return
            val version = it.version

            if (releaseCheckBoxChecked) {
                val matcher = pattern.matcher(version)
                if (!matcher.matches()) {
                    //如果不是正式版本，将继续检测下一项
                    return@forEach
                }
            }

            mFabricVersions[version] = loaderVersions!!.toList()
        }

        if (currentTask!!.isCancelled) return

        val mData: MutableList<ModListItemBean> = ArrayList()
        mFabricVersions.entries
            .sortedWith { entry1, entry2 ->
                MCVersionComparator.versionCompare(entry1.key, entry2.key)
            }
            .forEach { (gameVersion, loaderVersions) ->
                if (currentTask!!.isCancelled) return

                //为整理好的Fabric版本设置Adapter
                val adapter = BaseModVersionListAdapter(modloaderListenerProxy, this, icon, loaderVersions)
                adapter.setOnItemClickListener { version ->
                    val fabricVersion = version as FabricVersion
                    Thread(
                        FabriclikeDownloadTask(modloaderListenerProxy, utils,
                            gameVersion, fabricVersion.version, true)
                    ).start()
                }

                mData.add(ModListItemBean("Minecraft $gameVersion", adapter))
            }

        if (currentTask!!.isCancelled) return

        Tools.runOnUiThread {
            val recyclerView = recyclerView
            try {
                var mModAdapter = recyclerView!!.adapter as ModListAdapter?
                if (mModAdapter == null) {
                    mModAdapter = ModListAdapter(this, mData)
                    recyclerView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    recyclerView.adapter = mModAdapter
                } else {
                    mModAdapter.updateData(mData)
                }
            } catch (ignored: java.lang.Exception) {
            }

            componentProcessing(false)
            recyclerView?.scheduleLayoutAnimation()
        }
    }

    override fun onDownloadFinished(downloadedFile: File?) {
        Tools.runOnUiThread {
            modloaderListenerProxy.detachListener()
            Tools.backToMainMenu(fragmentActivity!!)
        }
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            val context = fragmentActivity!!
            modloaderListenerProxy.detachListener()
            Tools.dialog(context, context.getString(R.string.global_error),
                context.getString(R.string.fabric_dl_cant_read_meta, utils.name)
            )
        }
    }

    override fun onDownloadError(e: java.lang.Exception?) {
        Tools.runOnUiThread {
            val context = fragmentActivity!!
            modloaderListenerProxy.detachListener()
            Tools.showError(context, e)
        }
    }
}