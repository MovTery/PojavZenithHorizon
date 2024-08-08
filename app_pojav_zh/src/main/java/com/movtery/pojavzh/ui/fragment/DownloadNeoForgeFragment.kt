package com.movtery.pojavzh.ui.fragment

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.mod.modloader.BaseModVersionListAdapter
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeDownloadTask
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.addAutoInstallArgs
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgedForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.formatGameVersion
import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import com.movtery.pojavzh.utils.MCVersionComparator.Companion.versionCompare
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import java.io.File
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadNeoForgeFragment : ModListFragment(), ModloaderDownloadListener {
    companion object {
        const val TAG: String = "DownloadNeoForgeFragment"
    }

    private val modloaderListenerProxy = ModloaderListenerProxy()

    override fun init() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, R.drawable.ic_neoforge))
        setNameText("NeoForge")
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
                processModDetails(loadVersionList())
            } catch (e: Exception) {
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
            }
        }
    }

    @Throws(Exception::class)
    fun loadVersionList(): List<String?> {
        val versions: MutableList<String?> = ArrayList()
        versions.addAll(downloadNeoForgedForgeVersions())
        versions.addAll(downloadNeoForgeVersions())

        versions.reverse()

        return versions
    }

    private fun processModDetails(neoForgeVersions: List<String?>?) {
        if (neoForgeVersions == null) {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("neoForgeVersions is Empty!")
            }
            return
        }
        val currentTask = currentTask

        val mNeoForgeVersions: MutableMap<String, MutableList<String?>> = HashMap()
        neoForgeVersions.forEach(Consumer<String?> { neoForgeVersion: String? ->
            if (currentTask!!.isCancelled) return@Consumer
            //查找并分组Minecraft版本与NeoForge版本
            val gameVersion: String

            val isOldVersion = neoForgeVersion!!.contains("1.20.1")
            gameVersion = if (isOldVersion) {
                "1.20.1"
            } else if (neoForgeVersion == "47.1.82") {
                return@Consumer
            } else { //1.20.2+
                formatGameVersion(neoForgeVersion)
            }
            mNeoForgeVersions.computeIfAbsent(gameVersion) { ArrayList() }
                .add(neoForgeVersion)
        })

        if (currentTask!!.isCancelled) return

        val mData: MutableList<ModListItemBean> = ArrayList()
        mNeoForgeVersions.entries
            .sortedWith { entry1, entry2 ->
                versionCompare(entry1.key, entry2.key)
            }
            .forEach { entry: Map.Entry<String, List<String?>> ->
                if (currentTask.isCancelled) return@forEach
                val adapter = BaseModVersionListAdapter(modloaderListenerProxy, this, R.drawable.ic_neoforge, entry.value)

                adapter.setOnItemClickListener { version: Any? ->
                    Thread(NeoForgeDownloadTask(modloaderListenerProxy, (version as String?)!!)).start()
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
            addAutoInstallArgs(modInstallerStartIntent, downloadedFile)
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
