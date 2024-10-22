package com.movtery.zalithlauncher.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.zalithlauncher.event.single.DownloadItemClickEvent
import com.movtery.zalithlauncher.event.value.InDownloadFragmentEvent
import com.movtery.zalithlauncher.feature.download.Filters
import com.movtery.zalithlauncher.feature.download.InfoAdapter
import com.movtery.zalithlauncher.feature.download.enums.Category
import com.movtery.zalithlauncher.feature.download.enums.Classify
import com.movtery.zalithlauncher.feature.download.enums.ModLoader
import com.movtery.zalithlauncher.feature.download.enums.Platform
import com.movtery.zalithlauncher.feature.download.enums.Sort
import com.movtery.zalithlauncher.feature.download.utils.CategoryUtils
import com.movtery.zalithlauncher.feature.download.utils.ModLoaderUtils
import com.movtery.zalithlauncher.feature.download.utils.SortUtils
import com.movtery.zalithlauncher.ui.dialog.SelectVersionDialog
import com.movtery.zalithlauncher.ui.subassembly.adapter.ObjectSpinnerAdapter
import com.movtery.zalithlauncher.ui.subassembly.versionlist.VersionSelectedListener
import com.movtery.zalithlauncher.utils.ZHTools
import com.movtery.zalithlauncher.utils.anim.AnimUtils.Companion.setVisibilityAnim
import com.skydoves.powerspinner.PowerSpinnerView
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentDownloadSearchBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DownloadFragment : FragmentWithAnim(R.layout.fragment_download_search), InfoAdapter.SearchResultCallback {
    companion object {
        const val TAG = "DownloadFragment"
        const val BUNDLE_CLASSIFY_TYPE = "bundle_classify_type"
    }

    private lateinit var binding: FragmentDownloadSearchBinding
    private lateinit var mPlatformAdapter: ObjectSpinnerAdapter<Platform>
    private lateinit var mSortAdapter: ObjectSpinnerAdapter<Sort>
    private lateinit var mCategoryAdapter: ObjectSpinnerAdapter<Category>
    private lateinit var mModLoaderAdapter: ObjectSpinnerAdapter<ModLoader>
    private var mCurrentPlatform: Platform = Platform.CURSEFORGE
    private var mCurrentClassify: Classify = Classify.MOD
    private val mFilters: Filters = Filters()

    private val mAdapterMap: Map<Int, InfoAdapter> = mapOf(
        0 to InfoAdapter(this, 0, this),
        1 to InfoAdapter(this, 1, this),
        2 to InfoAdapter(this, 2, this),
        3 to InfoAdapter(this, 3, this)
    )
    private var mCurrentAdapterIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadSearchBinding.inflate(inflater, container, false)

        mPlatformAdapter = ObjectSpinnerAdapter(binding.platformSpinner) { platform -> platform.pName }
        mSortAdapter = ObjectSpinnerAdapter(binding.sortSpinner) { sort -> getString(sort.resNameID) }
        mCategoryAdapter = ObjectSpinnerAdapter(binding.categorySpinner) { category -> getString(category.resNameID) }
        mModLoaderAdapter = ObjectSpinnerAdapter(binding.modloaderSpinner) { modloader ->
            if (modloader == ModLoader.ALL) getString(R.string.generic_all)
            else modloader.loaderName
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun changeClassify(classify: Classify) {
            if (mCurrentClassify == classify) return
            mCurrentClassify = classify
            refreshClassifies()
        }
        binding.apply {
            classifyTab.observeIndexChange { _, toIndex, _, _ ->
                when (toIndex) {
                    0 -> changeClassify(Classify.MOD)
                    1 -> changeClassify(Classify.MODPACK)
                    2 -> changeClassify(Classify.RESOURCE_PACK)
                    3 -> changeClassify(Classify.WORLD)
                }
            }

            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                layoutAnimation = LayoutAnimationController(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)
                )
                addOnScrollListener(object : OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val lm = layoutManager as LinearLayoutManager
                        val lastPosition = lm.findLastVisibleItemPosition()
                        setVisibilityAnim(backToTop, lastPosition >= 12)
                    }
                })
            }

            backToTop.setOnClickListener { recyclerView.smoothScrollToPosition(0) }

            searchView.setOnClickListener { search() }
            nameEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    mFilters.name = s?.toString() ?: ""
                }
            })

            // 打开版本选择弹窗
            mcVersionButton.setOnClickListener {
                val selectVersionDialog = SelectVersionDialog(requireContext())
                selectVersionDialog.setOnVersionSelectedListener(object : VersionSelectedListener() {
                    override fun onVersionSelected(version: String?) {
                        selectedMcVersionView.text = version
                        mFilters.mcVersion = version
                        selectVersionDialog.dismiss()
                    }
                })
                selectVersionDialog.show()
            }
        }

        // 初始化 Spinner
        mPlatformAdapter.setItems(listOf(Platform.CURSEFORGE, Platform.MODRINTH))
        mSortAdapter.setItems(SortUtils.getSortList())
        mModLoaderAdapter.setItems(ModLoaderUtils.getModLoaderList())

        binding.apply {
            platformSpinner.setSpinnerAdapter(mPlatformAdapter)
            platformSpinner.selectItemByIndex(0)
            setSpinnerListener<Platform>(platformSpinner) {
                mCurrentPlatform = it
                search()
            }

            sortSpinner.setSpinnerAdapter(mSortAdapter)
            sortSpinner.selectItemByIndex(0)
            setSpinnerListener<Sort>(sortSpinner) { mFilters.sort = it }

            categorySpinner.setSpinnerAdapter(mCategoryAdapter)
            setSpinnerListener<Category>(binding.categorySpinner) { mFilters.category = it }

            modloaderSpinner.setSpinnerAdapter(mModLoaderAdapter)
            modloaderSpinner.selectItemByIndex(0)
            setSpinnerListener<ModLoader>(modloaderSpinner) {
                mFilters.modloader = it.takeIf { loader -> loader != ModLoader.ALL }
            }

            reset.setOnClickListener {
                nameEdit.setText("")
                platformSpinner.selectItemByIndex(0)
                sortSpinner.selectItemByIndex(0)
                categorySpinner.selectItemByIndex(0)
                modloaderSpinner.selectItemByIndex(0)
                binding.selectedMcVersionView.text = null
                mFilters.mcVersion = null
            }

            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        }

        parseBundle()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(InDownloadFragmentEvent(true))
    }

    override fun onStop() {
        closeSpinner()
        super.onStop()
        EventBus.getDefault().unregister(this)
        EventBus.getDefault().post(InDownloadFragmentEvent(false))
    }

    @Subscribe
    fun lock(event: DownloadItemClickEvent.Lock) {
        binding.recyclerView.isEnabled = false
    }

    @Subscribe
    fun unLock(event: DownloadItemClickEvent.UnLock) {
        binding.recyclerView.isEnabled = true
    }

    private fun closeSpinner() {
        binding.platformSpinner.dismiss()
        binding.sortSpinner.dismiss()
        binding.categorySpinner.dismiss()
        binding.modloaderSpinner.dismiss()
    }

    private fun <E> setSpinnerListener(spinnerView: PowerSpinnerView, func: (E) -> Unit) {
        spinnerView.setOnSpinnerItemSelectedListener<E> { _, _, _, newItem -> func(newItem) }
    }

    private fun refreshClassifies() {
        binding.recyclerView.scrollToPosition(0)
        setStatusText(false)

        fun refreshCategorySpinner(list: List<Category>, showModLoader: Boolean) {
            if (mCategoryAdapter.itemCount > 0 && binding.categorySpinner.selectedIndex != 0) {
                //防止上一个类别的数量比这次的少，索引越界
                binding.categorySpinner.selectItemByIndex(0)
            }
            mCategoryAdapter.setItems(list)
            binding.categorySpinner.selectItemByIndex(0)
            showModLoader(showModLoader)
        }

        when (mCurrentClassify) {
            Classify.ALL -> {
                mCurrentClassify = Classify.MOD
                refreshClassifies()
                return
            }
            Classify.MOD -> {
                refreshCategorySpinner(CategoryUtils.getModCategory(), true)
                setAdapter(0)
            }
            Classify.MODPACK -> {
                refreshCategorySpinner(CategoryUtils.getModPackCategory(), true)
                setAdapter(1)
            }
            Classify.RESOURCE_PACK -> {
                refreshCategorySpinner(CategoryUtils.getResourcePackCategory(), false)
                setAdapter(2)
            }
            Classify.WORLD -> {
                refreshCategorySpinner(CategoryUtils.getWorldCategory(), false)
                setAdapter(3)
            }
        }
        mCurrentPlatform.helper.currentClassify = mCurrentClassify
    }

    private fun setAdapter(adapterIndex: Int) {
        mCurrentAdapterIndex = adapterIndex

        binding.recyclerView.adapter = mAdapterMap[adapterIndex]
        setRecyclerView(true)

        val adapter = mAdapterMap[adapterIndex]!!
        val requestSearch = adapter.itemCount == 0 || adapter.checkPlatform(mCurrentPlatform)
        adapter.setPlatform(mCurrentPlatform)

        if (requestSearch) search()
        else setLoadingLayout(false)
    }

    private fun showModLoader(show: Boolean) {
        binding.apply {
            modloaderTextview.visibility = if (show) View.VISIBLE else View.GONE
            modloaderSpinner.visibility = if (show) View.VISIBLE else View.GONE
            if (show) {
                modloaderSpinner.setSpinnerAdapter(mModLoaderAdapter)
                modloaderSpinner.selectItemByIndex(0)
            } else {
                mFilters.modloader = null
            }
        }
    }

    override fun onSearchFinished(index: Int) {
        if (index != mCurrentAdapterIndex) return
        binding.apply {
            setStatusText(false)
            setLoadingLayout(false)
            setRecyclerView(true)
        }
    }

    override fun onSearchError(index: Int, error: Int) {
        if (index != mCurrentAdapterIndex) return
        binding.apply {
            statusText.text = when (error) {
                InfoAdapter.SearchResultCallback.ERROR_INTERNAL -> getString(R.string.download_search_failed)
                InfoAdapter.SearchResultCallback.ERROR_PLATFORM_NOT_SUPPORTED -> getString(R.string.download_search_platform_not_supported)
                else -> getString(R.string.download_search_no_result)
            }
        }
        setLoadingLayout(false)
        setRecyclerView(false)
        setStatusText(true)
    }

    private fun setStatusText(shouldShow: Boolean) {
        setVisibilityAnim(binding.statusText, shouldShow)
    }

    private fun setLoadingLayout(shouldShow: Boolean) {
        setVisibilityAnim(binding.loadingLayout, shouldShow)
    }

    private fun setRecyclerView(shouldShow: Boolean) {
        binding.apply {
            recyclerView.visibility = if (shouldShow) View.VISIBLE else View.GONE
            if (shouldShow) recyclerView.scheduleLayoutAnimation()
        }
    }

    fun search() {
        mCurrentPlatform.helper.currentClassify = mCurrentClassify
        mCurrentPlatform.helper.filters = mFilters
        setStatusText(false)
        setRecyclerView(false)
        setLoadingLayout(true)
        binding.apply {
            recyclerView.scrollToPosition(0)
        }
        mAdapterMap[mCurrentAdapterIndex]?.apply {
            setPlatform(mCurrentPlatform)
            performSearchQuery()
        }
    }

    private fun parseBundle() {
        val type = arguments?.getInt(BUNDLE_CLASSIFY_TYPE) ?: 0
        binding.classifyTab.onPageSelected(type)
        refreshClassifies()
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(classifyLayout, Animations.BounceInRight))
                .apply(AnimPlayer.Entry(itemsLayout, Animations.BounceInDown))
                .apply(AnimPlayer.Entry(operateLayout, Animations.BounceInLeft))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(classifyLayout, Animations.FadeOutLeft))
                .apply(AnimPlayer.Entry(itemsLayout, Animations.FadeOutUp))
                .apply(AnimPlayer.Entry(operateLayout, Animations.FadeOutRight))
        }
    }
}
