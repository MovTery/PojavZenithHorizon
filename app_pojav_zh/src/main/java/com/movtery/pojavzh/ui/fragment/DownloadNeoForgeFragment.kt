package com.movtery.pojavzh.ui.fragment

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.feature.mod.modloader.ModVersionListAdapter
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeDownloadTask
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.addAutoInstallArgs
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgedForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.formatGameVersion
import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import org.jackhuang.hmcl.util.versioning.VersionNumber
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
        setLink("https://neoforged.net/")
        setReleaseCheckBoxGone() //隐藏“仅展示正式版”选择框，在这里没有用处
        super.init()
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    private fun refresh(force: Boolean): Future<*> {
        return PojavApplication.sExecutorService.submit {
            runCatching {
                Tools.runOnUiThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                processModDetails(loadVersionList(force))
            }.getOrElse { e ->
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadNeoForgeFragment", Tools.printToString(e))
            }
        }
    }

    @Throws(Exception::class)
    fun loadVersionList(force: Boolean): List<String?> {
        val versions: MutableList<String?> = ArrayList()
        versions.addAll(downloadNeoForgedForgeVersions(force))
        versions.addAll(downloadNeoForgeVersions(force))

        versions.reverse()

        return versions
    }

    private fun processModDetails(neoForgeVersions: List<String?>?) {
        neoForgeVersions ?: run {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("neoForgeVersions is Empty!")
            }
            return
        }

        val mNeoForgeVersions: MutableMap<String, MutableList<String?>> = HashMap()
        neoForgeVersions.forEach(Consumer<String?> { neoForgeVersion: String? ->
            currentTask?.apply { if (isCancelled) return@Consumer }
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

        currentTask?.apply { if (isCancelled) return }

        val mData: MutableList<ModListItemBean> = ArrayList()
        mNeoForgeVersions.entries
            .sortedWith { entry1, entry2 -> -VersionNumber.compare(entry1.key, entry2.key) }
            .forEach { entry: Map.Entry<String, List<String?>> ->
                currentTask?.apply { if (isCancelled) return }
                val adapter = ModVersionListAdapter(modloaderListenerProxy, this, R.drawable.ic_neoforge, entry.value)

                adapter.setOnItemClickListener { version: Any? ->
                    Thread(NeoForgeDownloadTask(modloaderListenerProxy, (version as String?)!!)).start()
                }
                mData.add(ModListItemBean("Minecraft " + entry.key, adapter))
            }

        currentTask?.apply { if (isCancelled) return }

        Tools.runOnUiThread {
            val recyclerView = recyclerView
            runCatching {
                var mModAdapter = recyclerView.adapter as ModListAdapter?
                mModAdapter ?: run {
                    mModAdapter = ModListAdapter(this, mData)
                    recyclerView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    recyclerView.adapter = mModAdapter
                    return@runCatching
                }
                mModAdapter?.updateData(mData)
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()
        }
    }

    override fun onDownloadFinished(downloadedFile: File) {
        Tools.runOnUiThread {
            val modInstallerStartIntent = Intent(fragmentActivity!!, JavaGUILauncherActivity::class.java)
            addAutoInstallArgs(modInstallerStartIntent, downloadedFile)
            SelectRuntimeDialog(fragmentActivity!!).apply {
                setListener { jreName: String? ->
                    modloaderListenerProxy.detachListener()
                    modInstallerStartIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName)
                    dismiss()
                    Tools.backToMainMenu(fragmentActivity!!)
                    fragmentActivity?.startActivity(modInstallerStartIntent)
                }
                setTitleText(R.string.zh_modloader_dl_install_neoforge)
                show()
            }
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
