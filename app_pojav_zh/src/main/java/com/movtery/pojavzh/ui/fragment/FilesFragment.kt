package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerView
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import com.movtery.pojavzh.ui.subassembly.view.SearchView
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.setVisibilityAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import com.movtery.pojavzh.utils.file.PasteFile
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import java.io.File
import java.util.function.Consumer

class FilesFragment : FragmentWithAnim(R.layout.fragment_files) {
    companion object {
        const val TAG: String = "FilesFragment"
        const val BUNDLE_LOCK_PATH: String = "bundle_lock_path"
        const val BUNDLE_LIST_PATH: String = "bundle_list_path"
        const val BUNDLE_SHOW_FILE: String = "show_file"
        const val BUNDLE_SHOW_FOLDER: String = "show_folder"
        const val BUNDLE_QUICK_ACCESS_PATHS: String = "quick_access_paths"
        const val BUNDLE_MULTI_SELECT_MODE: String = "multi_select_mode"
        const val BUNDLE_SELECT_FOLDER_MODE: String = "select_folder_mode"
        const val BUNDLE_REMOVE_LOCK_PATH: String = "remove_lock_path"
    }

    private var openDocumentLauncher: ActivityResultLauncher<Any?>? = null
    private var mShowFiles = false
    private var mShowFolders = false
    private var mQuickAccessPaths = false
    private var mMultiSelectMode = false
    private var mSelectFolderMode = false
    private var mRemoveLockPath = false
    private var mReturnButton: ImageButton? = null
    private var mAddFileButton: ImageButton? = null
    private var mCreateFolderButton: ImageButton? = null
    private var mPasteButton: ImageButton? = null
    private var mSearchSummonButton: ImageButton? = null
    private var mRefreshButton: ImageButton? = null
    private var mNothingTip: TextView? = null
    private var mSearchView: SearchView? = null
    private var mMultiSelectCheck: CheckBox? = null
    private var mSelectAllCheck: CheckBox? = null
    private var mFilesLayout: View? = null
    private var mOperateLayout: View? = null
    private var mOperateView: View? = null
    private var mExternalStorage: View? = null
    private var mSoftwarePrivate: View? = null
    private var mFileRecyclerView: FileRecyclerView? = null
    private var mFilePathView: TextView? = null
    private var mLockPath: String? = null
    private var mListPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension(null)) { result: Uri? ->
            result?.let {
                Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireContext(), result, mFileRecyclerView!!.fullPath.absolutePath)
                    Tools.runOnUiThread {
                        Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show()
                        mFileRecyclerView?.refreshPath()
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parseBundle()
        bindViews(view)

        mFileRecyclerView?.setShowFiles(mShowFiles)
        mFileRecyclerView?.setShowFolders(mShowFolders)
        mFileRecyclerView?.setTitleListener { title: String? -> mFilePathView?.text = removeLockPath(title!!) }

        mFilePathView?.setOnClickListener {
            val builder = EditTextDialog.Builder(requireContext())
            builder.setTitle(R.string.zh_file_jump_to_path)
            builder.setEditText(mFileRecyclerView!!.fullPath.absolutePath)
            builder.setConfirmListener { editBox: EditText ->
                val path = editBox.text.toString()
                if (path.isEmpty()) {
                    editBox.error = getString(R.string.global_error_field_empty)
                    return@setConfirmListener false
                }

                val file = File(path)
                //检查路径是否符合要求：最少为最顶部路径、路径是一个文件夹、这个路径存在
                if (!path.contains(mLockPath!!) || !file.isDirectory || !file.exists()) {
                    editBox.error = getString(R.string.zh_file_does_not_exist)
                    return@setConfirmListener false
                }

                mFileRecyclerView?.listFileAt(file)
                true
            }
            builder.buildDialog()
        }

        mFileRecyclerView?.setFileSelectedListener(object : FileSelectedListener() {
            override fun onFileSelected(file: File?, path: String?) {
                showDialog(file)
            }

            override fun onItemLongClick(file: File?, path: String?) {
                if (file!!.isDirectory) showDialog(file)
            }
        })

        mFileRecyclerView?.setOnMultiSelectListener { itemBeans: List<FileItemBean> ->
            if (itemBeans.isNotEmpty()) {
                PojavApplication.sExecutorService.execute {
                    //取出全部文件
                    val selectedFiles: MutableList<File> = ArrayList()
                    itemBeans.forEach(Consumer { value: FileItemBean ->
                        val file = value.file
                        if (file != null) selectedFiles.add(file)
                    })
                    val filesButton = FilesButton()
                    filesButton.setButtonVisibility(true, true, false, false, true, false)
                    filesButton.setDialogText(
                        getString(R.string.zh_file_multi_select_mode_title),
                        getString(R.string.zh_file_multi_select_mode_message, itemBeans.size), null
                    )
                    Tools.runOnUiThread {
                        val filesDialog = FilesDialog(requireContext(), filesButton, {
                            Tools.runOnUiThread {
                                closeMultiSelect()
                                mFileRecyclerView?.refreshPath()
                            }
                        }, mFileRecyclerView!!.fullPath, selectedFiles)
                        filesDialog.setCopyButtonClick { mPasteButton?.visibility = View.VISIBLE }
                        filesDialog.show()
                    }
                }
            }
        }
        mFileRecyclerView?.setRefreshListener {
            val itemCount = mFileRecyclerView!!.itemCount
            val show = itemCount <= 1
            setVisibilityAnim(mNothingTip!!, show)
        }
        mExternalStorage?.setOnClickListener {
            closeMultiSelect()
            mFileRecyclerView?.listFileAt(Environment.getExternalStorageDirectory())
        }
        mSoftwarePrivate?.setOnClickListener {
            closeMultiSelect()
            mFileRecyclerView?.listFileAt(requireContext().getExternalFilesDir(null))
        }
        val adapter = mFileRecyclerView!!.adapter
        mMultiSelectCheck?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mSelectAllCheck?.isChecked = false
            mSelectAllCheck?.visibility = if (isChecked) View.VISIBLE else View.GONE
            adapter.setMultiSelectMode(isChecked)
        }
        mSelectAllCheck?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            adapter.selectAllFiles(
                isChecked
            )
        }

        mReturnButton?.setOnClickListener {
            if (!mSelectFolderMode) {
                closeMultiSelect()
                Tools.removeCurrentFragment(requireActivity())
            } else {
                ExtraCore.setValue(
                    ExtraConstants.FILE_SELECTOR,
                    removeLockPath(mFileRecyclerView!!.fullPath.absolutePath)
                )
                Tools.removeCurrentFragment(requireActivity())
            }
        }
        mAddFileButton?.setOnClickListener {
            closeMultiSelect()
            openDocumentLauncher?.launch(null)
        } //不限制文件类型
        mCreateFolderButton?.setOnClickListener {
            closeMultiSelect()
            EditTextDialog.Builder(requireContext())
                .setTitle(R.string.folder_dialog_insert_name)
                .setConfirmListener { editBox: EditText ->
                    val name = editBox.text.toString().replace("/", "")
                    if (name.isEmpty()) {
                        editBox.error = getString(R.string.zh_file_rename_empty)
                        return@setConfirmListener false
                    }

                    val folder = File(mFileRecyclerView!!.fullPath, name)

                    if (folder.exists()) {
                        editBox.error = getString(R.string.zh_file_rename_exitis)
                        return@setConfirmListener false
                    }

                    val success = folder.mkdir()
                    if (success) {
                        mFileRecyclerView?.listFileAt(File(mFileRecyclerView!!.fullPath, name))
                    } else {
                        mFileRecyclerView?.refreshPath()
                    }
                    true
                }.buildDialog()
        }
        mPasteButton?.setOnClickListener {
            PasteFile.getInstance()
                .pasteFiles(requireActivity(), mFileRecyclerView!!.fullPath, null) {
                    Tools.runOnUiThread {
                        closeMultiSelect()
                        mPasteButton?.visibility = View.GONE
                        mFileRecyclerView?.refreshPath()
                    }
                }
        }
        mSearchSummonButton?.setOnClickListener {
            closeMultiSelect()
            mSearchView?.setVisibility()
        }
        mRefreshButton?.setOnClickListener {
            closeMultiSelect()
            mFileRecyclerView?.refreshPath()
        }

        mFileRecyclerView?.apply list@{
            mListPath?.let {
                lockAndListAt(File(mLockPath!!), File(mListPath!!))
                return@list
            }
            lockAndListAt(File(mLockPath!!), File(mLockPath!!))
        }

        slideInAnim(this)
    }

    private fun closeMultiSelect() {
        //点击其它控件时关闭多选模式
        mMultiSelectCheck?.isChecked = false
        mSelectAllCheck?.visibility = View.GONE
    }

    private fun showDialog(file: File?) {
        val filesButton = FilesButton()
        filesButton.setButtonVisibility(true, true, !file!!.isDirectory, true, true, false)
        val message = if (file.isDirectory) {
            getString(R.string.zh_file_folder_message)
        } else {
            getString(R.string.zh_file_message)
        }
        filesButton.setMessageText(message)

        val filesDialog = FilesDialog(requireContext(), filesButton,
            { Tools.runOnUiThread { mFileRecyclerView?.refreshPath() } },
            mFileRecyclerView!!.fullPath, file)
        filesDialog.setCopyButtonClick { mPasteButton?.visibility = View.VISIBLE }
        filesDialog.show()
    }

    private fun removeLockPath(path: String): String {
        var string = path
        if (mRemoveLockPath) {
            string = path.replace(mLockPath!!, ".")
        }
        return string
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun bindViews(view: View) {
        mFilesLayout = view.findViewById(R.id.files_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        mOperateView = view.findViewById(R.id.operate_view)

        mReturnButton = view.findViewById(R.id.zh_return_button)
        mAddFileButton = view.findViewById(R.id.zh_add_file_button)
        mCreateFolderButton = view.findViewById(R.id.zh_create_folder_button)
        mPasteButton = view.findViewById(R.id.zh_paste_button)
        mRefreshButton = view.findViewById(R.id.zh_refresh_button)
        mSearchSummonButton = view.findViewById(R.id.zh_search_button)
        mFileRecyclerView = view.findViewById(R.id.zh_files)
        mFilePathView = view.findViewById(R.id.zh_files_current_path)
        mExternalStorage = view.findViewById(R.id.zh_files_external_storage)
        mSoftwarePrivate = view.findViewById(R.id.zh_files_software_private)
        mMultiSelectCheck = view.findViewById(R.id.zh_file_multi_select_files)
        mSelectAllCheck = view.findViewById(R.id.zh_file_select_all)
        mNothingTip = view.findViewById(R.id.zh_files_nothing)

        mSearchView = SearchView(view, view.findViewById(R.id.zh_search_view))
        mSearchView?.setSearchListener(object : SearchView.SearchListener {
            override fun onSearch(string: String?, caseSensitive: Boolean): Int {
                return mFileRecyclerView!!.searchFiles(string, caseSensitive)
            }
        })
        mSearchView?.setShowSearchResultsListener(object : SearchView.ShowSearchResultsListener {
            override fun onSearch(show: Boolean) {
                mFileRecyclerView?.setShowSearchResultsOnly(show)
            }
        })

        if (!mQuickAccessPaths) {
            mExternalStorage?.visibility = View.GONE
            mSoftwarePrivate?.visibility = View.GONE
        }
        if (mSelectFolderMode || !mMultiSelectMode) {
            mMultiSelectCheck?.visibility = View.GONE
            mSelectAllCheck?.visibility = View.GONE
        }

        if (mSelectFolderMode) {
            mAddFileButton?.visibility = View.GONE
            mReturnButton?.contentDescription = getString(R.string.folder_fragment_select)
            mReturnButton?.setImageDrawable(resources.getDrawable(R.drawable.ic_check, requireActivity().theme))
        }

        mPasteButton?.setVisibility(if (PasteFile.getInstance().pasteType != null) View.VISIBLE else View.GONE)

        ZHTools.setTooltipText(mReturnButton, mReturnButton?.contentDescription)
        ZHTools.setTooltipText(mAddFileButton, mAddFileButton?.contentDescription)
        ZHTools.setTooltipText(mCreateFolderButton, mCreateFolderButton?.contentDescription)
        ZHTools.setTooltipText(mPasteButton, mPasteButton?.contentDescription)
        ZHTools.setTooltipText(mSearchSummonButton, mSearchSummonButton?.contentDescription)
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton?.contentDescription)
    }

    private fun parseBundle() {
        val bundle = arguments ?: return
        mLockPath = bundle.getString(BUNDLE_LOCK_PATH, Environment.getExternalStorageDirectory().absolutePath)
        mListPath = bundle.getString(BUNDLE_LIST_PATH, null)
        mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILE, true)
        mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDER, true)
        mQuickAccessPaths = bundle.getBoolean(BUNDLE_QUICK_ACCESS_PATHS, true)
        mMultiSelectMode = bundle.getBoolean(BUNDLE_MULTI_SELECT_MODE, true)
        mSelectFolderMode = bundle.getBoolean(BUNDLE_SELECT_FOLDER_MODE, false)
        mRemoveLockPath = bundle.getBoolean(BUNDLE_REMOVE_LOCK_PATH, true)
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mFilesLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))
        yoYos.add(setViewAnim(mOperateView!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mFilesLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}

