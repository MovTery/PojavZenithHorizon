package com.movtery.pojavzh.ui.fragment

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.feature.mod.modloader.ModVersionListAdapter
import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog
import com.movtery.pojavzh.ui.subassembly.modlist.ModListAdapter
import com.movtery.pojavzh.ui.subassembly.modlist.ModListFragment
import com.movtery.pojavzh.ui.subassembly.modlist.ModListItemBean
import com.movtery.pojavzh.utils.MCVersionRegex
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.FabricVersion
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import org.jackhuang.hmcl.util.versioning.VersionNumber
import java.io.File
import java.util.concurrent.Future

abstract class DownloadFabricLikeFragment(val utils: FabriclikeUtils, val icon: Int) : ModListFragment(), ModloaderDownloadListener {
    private val modloaderListenerProxy = ModloaderListenerProxy()
    private var selectedGameVersion: String = ""
    private var selectedLoaderVersion: String = ""

    override fun init() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, icon))
        setNameText(utils.name)
        setLink(utils.webUrl)
        super.init()
    }

    override fun initRefresh(): Future<*>? {
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
                val gameVersions = utils.downloadGameVersions(force)
                processInfo(gameVersions, force)
            }.getOrElse { e ->
                Tools.runOnUiThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                e("DownloadFabricLike", Tools.printToString(e))
            }
        }
    }

    private fun processInfo(gameVersions: Array<FabricVersion>, force: Boolean) {
        if (gameVersions.isEmpty()) {
            Tools.runOnUiThread {
                componentProcessing(false)
                setFailedToLoad("gameVersions is Empty!")
            }
            return
        }

        val releaseCheckBoxChecked = releaseCheckBox.isChecked
        val pattern = MCVersionRegex.RELEASE_REGEX

        val mFabricVersions: MutableMap<String, List<FabricVersion>> = HashMap()
        val loaderVersions: Array<FabricVersion>? = utils.downloadLoaderVersions(force)
        gameVersions.forEach {
            currentTask?.apply { if (isCancelled) return }
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

        currentTask?.apply { if (isCancelled) return }

        val mData: MutableList<ModListItemBean> = ArrayList()
        mFabricVersions.entries
            .sortedWith { entry1, entry2 -> -VersionNumber.compare(entry1.key, entry2.key) }
            .forEach { (gameVersion, loaderVersions) ->
                currentTask?.apply { if (isCancelled) return }

                //为整理好的Fabric版本设置Adapter
                val adapter = ModVersionListAdapter(modloaderListenerProxy, this, icon, loaderVersions)
                adapter.setOnItemClickListener { version ->
                    selectedGameVersion = gameVersion
                    val loaderVersion = (version as FabricVersion).version
                    selectedLoaderVersion = loaderVersion
                    Thread(utils.getDownloadTask(modloaderListenerProxy, gameVersion, loaderVersion)).start()
                }

                mData.add(ModListItemBean("Minecraft $gameVersion", adapter))
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
                e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()
        }
    }

    override fun onDownloadFinished(downloadedFile: File?) {
        Tools.runOnUiThread {
            downloadedFile?.apply {
                val modInstallerStartIntent = Intent(fragmentActivity!!, JavaGUILauncherActivity::class.java)
                FabriclikeUtils.addAutoInstallArgs(modInstallerStartIntent, utils, selectedGameVersion, selectedLoaderVersion, this)
                SelectRuntimeDialog(fragmentActivity!!).apply {
                    setListener { jreName: String? ->
                        modloaderListenerProxy.detachListener()
                        modInstallerStartIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName)
                        dismiss()
                        Tools.backToMainMenu(fragmentActivity!!)
                        fragmentActivity?.startActivity(modInstallerStartIntent)
                    }
                    setTitleText(R.string.create_profile_fabric)
                    show()
                }
                return@runOnUiThread
            }
            Tools.backToMainMenu(fragmentActivity!!)
        }
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            val context = fragmentActivity!!
            modloaderListenerProxy.detachListener()
            Tools.dialog(context, context.getString(R.string.generic_error),
                context.getString(R.string.mod_fabric_cant_read_meta, utils.name)
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