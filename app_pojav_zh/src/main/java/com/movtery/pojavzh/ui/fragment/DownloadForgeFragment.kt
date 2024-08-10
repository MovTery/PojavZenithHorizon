package com.movtery.pojavzh.ui.fragment

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.mod.modloader.BaseModVersionListAdapter
import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask
import net.kdt.pojavlaunch.modloaders.ForgeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import org.jackhuang.hmcl.util.versioning.VersionNumber
import java.io.File
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadForgeFragment : ModListFragment(), ModloaderDownloadListener {
    companion object {
        const val TAG: String = "DownloadForgeFragment"
    }

    private val modloaderListenerProxy = ModloaderListenerProxy()

    override fun init() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, R.drawable.ic_anvil))
        setNameText("Forge")
        setReleaseCheckBoxGone() //隐藏“仅展示正式版”选择框，在这里没有用处
        super.init()
    }

    override fun refresh(): Future<*> {
        return PojavApplication.sExecutorService.submit {
            try {
                Tools.runOnUiThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val forgeVersions = ForgeUtils.downloadForgeVersions()
                processModDetails(forgeVersions)
            } catch (e: Exception) {
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
            }
        }
    }

    private fun processModDetails(forgeVersions: List<String>?) {
        if (forgeVersions == null) {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("forgeVersions is Empty!")
            }
            return
        }
        val currentTask = currentTask

        val mForgeVersions: MutableMap<String, MutableList<String?>> = HashMap()
        forgeVersions.forEach(Consumer { forgeVersion: String ->
            if (currentTask!!.isCancelled) return@Consumer

            //查找并分组Minecraft版本与Forge版本
            val dashIndex = forgeVersion.indexOf("-")
            val gameVersion = forgeVersion.substring(0, dashIndex)
            mForgeVersions.computeIfAbsent(gameVersion) { ArrayList() }
                .add(forgeVersion)
        })

        if (currentTask!!.isCancelled) return

        val mData: MutableList<ModListItemBean> = ArrayList()
        mForgeVersions.entries
            .sortedWith { entry1, entry2 -> -VersionNumber.compare(entry1.key, entry2.key) }
            .forEach { entry: Map.Entry<String, List<String?>> ->
                if (currentTask.isCancelled) return

                //为整理好的Forge版本设置Adapter
                val adapter = BaseModVersionListAdapter(modloaderListenerProxy, this, R.drawable.ic_anvil, entry.value)
                adapter.setOnItemClickListener { version: Any? ->
                    Thread(ForgeDownloadTask(modloaderListenerProxy, version as String?)).start()
                }
                mData.add(ModListItemBean("Minecraft " + entry.key, adapter))
            }

        if (currentTask.isCancelled) return

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
            } catch (ignored: Exception) {
            }

            componentProcessing(false)
            recyclerView?.scheduleLayoutAnimation()
        }
    }

    override fun onDownloadFinished(downloadedFile: File) {
        Tools.runOnUiThread {
            val modInstallerStartIntent = Intent(fragmentActivity!!, JavaGUILauncherActivity::class.java)
            ForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile, true)
            val selectRuntimeDialog = SelectRuntimeDialog(fragmentActivity!!)
            selectRuntimeDialog.setListener { jreName: String? ->
                modloaderListenerProxy.detachListener()
                modInstallerStartIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName)
                selectRuntimeDialog.dismiss()
                Tools.backToMainMenu(fragmentActivity!!)
                fragmentActivity?.startActivity(modInstallerStartIntent)
            }
            selectRuntimeDialog.show()
        }
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            modloaderListenerProxy.detachListener()
            Tools.dialog(fragmentActivity!!, fragmentActivity!!.getString(R.string.global_error), fragmentActivity!!.getString(R.string.forge_dl_no_installer))
        }
    }

    override fun onDownloadError(e: Exception) {
        Tools.runOnUiThread {
            modloaderListenerProxy.detachListener()
            Tools.showError(fragmentActivity!!, e)
        }
    }
}
