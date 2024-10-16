package com.movtery.pojavzh.ui.fragment

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.feature.mod.modloader.ModVersionListAdapter
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
        setLink("https://forums.minecraftforge.net/")
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
                val forgeVersions = ForgeUtils.downloadForgeVersions(force)
                processModDetails(forgeVersions)
            }.getOrElse { e ->
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadForge", Tools.printToString(e))
            }
        }
    }

    private fun processModDetails(forgeVersions: List<String>?) {
        forgeVersions ?: run {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("forgeVersions is Empty!")
            }
            return
        }

        val mForgeVersions: MutableMap<String, MutableList<String?>> = HashMap()
        forgeVersions.forEach(Consumer { forgeVersion: String ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            //查找并分组Minecraft版本与Forge版本
            val dashIndex = forgeVersion.indexOf("-")
            val gameVersion = forgeVersion.substring(0, dashIndex)
            mForgeVersions.computeIfAbsent(gameVersion) { ArrayList() }
                .add(forgeVersion)
        })

        currentTask?.apply { if (isCancelled) return }

        val mData: MutableList<ModListItemBean> = ArrayList()
        mForgeVersions.entries
            .sortedWith { entry1, entry2 -> -VersionNumber.compare(entry1.key, entry2.key) }
            .forEach { entry: Map.Entry<String, List<String?>> ->
                currentTask?.apply { if (isCancelled) return }

                //为整理好的Forge版本设置Adapter
                val adapter = ModVersionListAdapter(modloaderListenerProxy, this, R.drawable.ic_anvil, entry.value)
                adapter.setOnItemClickListener { version: Any? ->
                    Thread(ForgeDownloadTask(modloaderListenerProxy, version as String?)).start()
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
            ForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile, true)
            SelectRuntimeDialog(fragmentActivity!!).apply {
                setListener { jreName: String? ->
                    modloaderListenerProxy.detachListener()
                    modInstallerStartIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName)
                    dismiss()
                    Tools.backToMainMenu(fragmentActivity!!)
                    fragmentActivity?.startActivity(modInstallerStartIntent)
                }
                setTitleText(R.string.create_profile_forge)
                show()
            }
        }
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            modloaderListenerProxy.detachListener()
            Tools.dialog(fragmentActivity!!, fragmentActivity!!.getString(R.string.generic_error), fragmentActivity!!.getString(R.string.mod_no_installer, "Forge"))
        }
    }

    override fun onDownloadError(e: Exception) {
        Tools.runOnUiThread {
            modloaderListenerProxy.detachListener()
            Tools.showError(fragmentActivity!!, e)
        }
    }
}
