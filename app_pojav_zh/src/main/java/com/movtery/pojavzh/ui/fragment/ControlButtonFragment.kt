package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.dialog.EditControlInfoDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlsListViewCreator
import com.movtery.pojavzh.ui.subassembly.customcontrols.EditControlData.createNewControlFile
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import com.movtery.pojavzh.ui.subassembly.view.SearchView
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils.setVisibilityAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground
import com.movtery.pojavzh.utils.file.PasteFile
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File

class ControlButtonFragment : FragmentWithAnim(R.layout.fragment_control_manager) {
    companion object {
        const val TAG: String = "ControlButtonFragment"
        const val BUNDLE_SELECT_CONTROL: String = "bundle_select_control"
    }

    private var openDocumentLauncher: ActivityResultLauncher<Any>? = null
    private var mControlLayout: View? = null
    private var mOperateLayout: View? = null
    private var mOperateView: View? = null
    private var mReturnButton: ImageButton? = null
    private var mAddControlButton: ImageButton? = null
    private var mImportControlButton: ImageButton? = null
    private var mPasteButton: ImageButton? = null
    private var mSearchSummonButton: ImageButton? = null
    private var mRefreshButton: ImageButton? = null
    private var mNothingTip: TextView? = null
    private var mSearchView: SearchView? = null
    private var controlsListViewCreator: ControlsListViewCreator? = null
    private var mSelectControl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension("json")) { result: Uri? ->
            result?.let {
                Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()

                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireContext(), result, File(Tools.CTRLMAP_PATH).absolutePath)
                    Tools.runOnUiThread {
                        Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show()
                        controlsListViewCreator!!.refresh()
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        parseBundle()

        controlsListViewCreator!!.setFileSelectedListener(object : FileSelectedListener() {
            override fun onFileSelected(file: File?, path: String?) {
                if (mSelectControl) {
                    ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path))
                    Tools.removeCurrentFragment(requireActivity())
                } else {
                    showDialog(file)
                }
            }

            override fun onItemLongClick(file: File?, path: String?) {
                TipDialog.Builder(requireContext())
                    .setTitle(R.string.default_control)
                    .setMessage(R.string.zh_controls_set_default_message)
                    .setConfirmClickListener {
                        val absolutePath = file!!.absolutePath
                        LauncherPreferences.DEFAULT_PREF.edit()
                            .putString("defaultCtrl", absolutePath).apply()
                        LauncherPreferences.PREF_DEFAULTCTRL_PATH = absolutePath
                    }.buildDialog()
            }
        })

        controlsListViewCreator!!.setRefreshListener {
            val itemCount = controlsListViewCreator!!.itemCount
            val show = itemCount == 0
            setVisibilityAnim(mNothingTip!!, show)
        }

        mReturnButton!!.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        mPasteButton!!.setOnClickListener {
            PasteFile.getInstance().pasteFiles(requireActivity(), File(Tools.CTRLMAP_PATH), null) {
                Tools.runOnUiThread {
                    mPasteButton!!.visibility = View.GONE
                    controlsListViewCreator!!.refresh()
                }
            }
        }
        mImportControlButton!!.setOnClickListener {
            val suffix = ".json"
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show()
            openDocumentLauncher!!.launch(suffix)
        } //限制.json文件
        mAddControlButton!!.setOnClickListener {
            val editControlInfoDialog = EditControlInfoDialog(requireContext(), true, null, ControlInfoData())
            editControlInfoDialog.setTitle(getString(R.string.zh_controls_create_new))
            editControlInfoDialog.setOnConfirmClickListener { fileName: String, controlInfoData: ControlInfoData? ->
                val file = File(File(Tools.CTRLMAP_PATH).absolutePath, "$fileName.json")
                if (file.exists()) { //检查文件是否已经存在
                    editControlInfoDialog.fileNameEditBox.error =
                        getString(R.string.zh_file_rename_exitis)
                    return@setOnConfirmClickListener
                }

                //创建布局文件
                createNewControlFile(requireContext(), file, controlInfoData)

                controlsListViewCreator!!.refresh()
                editControlInfoDialog.dismiss()
            }
            editControlInfoDialog.show()
        }
        mSearchSummonButton!!.setOnClickListener { mSearchView!!.setVisibility() }
        mRefreshButton!!.setOnClickListener { controlsListViewCreator!!.refresh() }

        controlsListViewCreator!!.listAtPath()

        slideInAnim(this)
    }

    private fun removeLockPath(path: String?): String {
        return path!!.replace(Tools.CTRLMAP_PATH, ".")
    }

    private fun showDialog(file: File?) {
        val filesButton = FilesButton()
        filesButton.setButtonVisibility(true, true, !file!!.isDirectory, true, true, true)

        if (file.isDirectory) {
            filesButton.setMessageText(getString(R.string.zh_file_folder_message))
        } else {
            filesButton.setMessageText(getString(R.string.zh_file_message))
        }
        filesButton.setMoreButtonText(getString(R.string.global_load))

        val filesDialog = FilesDialog(requireContext(), filesButton,
            { Tools.runOnUiThread { controlsListViewCreator!!.refresh() } },
            file
        )

        filesDialog.setCopyButtonClick { mPasteButton!!.visibility = View.VISIBLE }

        filesDialog.setMoreButtonClick {
            val intent = Intent(requireContext(), CustomControlsActivity::class.java)
            val bundle = Bundle()
            bundle.putString(CustomControlsActivity.BUNDLE_CONTROL_PATH, file.absolutePath)
            intent.putExtras(bundle)

            startActivity(intent)
            filesDialog.dismiss()
        } //加载
        filesDialog.show()
    }

    private fun parseBundle() {
        val bundle = arguments ?: return
        mSelectControl = bundle.getBoolean(BUNDLE_SELECT_CONTROL, mSelectControl)
    }

    private fun bindViews(view: View) {
        mControlLayout = view.findViewById(R.id.control_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        mOperateView = view.findViewById(R.id.operate_view)

        mReturnButton = view.findViewById(R.id.zh_return_button)
        mImportControlButton = view.findViewById(R.id.zh_add_file_button)
        mAddControlButton = view.findViewById(R.id.zh_create_folder_button)
        mPasteButton = view.findViewById(R.id.zh_paste_button)
        mRefreshButton = view.findViewById(R.id.zh_refresh_button)
        mSearchSummonButton = view.findViewById(R.id.zh_search_button)
        mNothingTip = view.findViewById(R.id.zh_controls_nothing)

        mImportControlButton!!.setContentDescription(getString(R.string.zh_controls_import_control))
        mAddControlButton!!.setContentDescription(getString(R.string.zh_controls_create_new))

        controlsListViewCreator =
            ControlsListViewCreator(requireContext(), view.findViewById(R.id.zh_controls_list))

        mSearchView = SearchView(view, view.findViewById(R.id.zh_search_view))
        mSearchView!!.setAsynchronousUpdatesListener(object : SearchView.SearchAsynchronousUpdatesListener {
            override fun onSearch(searchCount: TextView?, string: String?, caseSensitive: Boolean) {
                controlsListViewCreator!!.searchControls(searchCount, string, caseSensitive)
            }
        })
        mSearchView!!.setShowSearchResultsListener(object : SearchView.ShowSearchResultsListener {
            override fun onSearch(show: Boolean) {
                controlsListViewCreator!!.setShowSearchResultsOnly(show)
            }
        })

        mPasteButton!!.setVisibility(if (PasteFile.getInstance().pasteType != null) View.VISIBLE else View.GONE)

        ZHTools.setTooltipText(mReturnButton, mReturnButton!!.contentDescription)
        ZHTools.setTooltipText(mImportControlButton, mImportControlButton!!.contentDescription)
        ZHTools.setTooltipText(mAddControlButton, mAddControlButton!!.contentDescription)
        ZHTools.setTooltipText(mPasteButton, mPasteButton!!.contentDescription)
        ZHTools.setTooltipText(mSearchSummonButton, mSearchSummonButton!!.contentDescription)
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton!!.contentDescription)
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mControlLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))

        yoYos.add(setViewAnim(mOperateView!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mControlLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}

