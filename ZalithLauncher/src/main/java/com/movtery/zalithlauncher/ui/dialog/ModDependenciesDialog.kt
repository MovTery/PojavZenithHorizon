package com.movtery.zalithlauncher.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.zalithlauncher.feature.download.ModDependenciesAdapter
import com.movtery.zalithlauncher.feature.download.item.DependenciesInfoItem
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.DialogModDependenciesBinding

class ModDependenciesDialog(
    parentFragment: Fragment,
    infoItem: InfoItem,
    mData: List<DependenciesInfoItem>,
    install: () -> Unit
) :
    FullScreenDialog(parentFragment.requireContext()) {
    private val binding = DialogModDependenciesBinding.inflate(layoutInflater)

    init {
        this.setCancelable(false)
        this.setContentView(binding.root)
        init(parentFragment, infoItem, mData.toMutableList(), install)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
            )
            setGravity(Gravity.CENTER)

            //隐藏状态栏
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun init(
        parentFragment: Fragment,
        infoItem: InfoItem,
        mData: MutableList<DependenciesInfoItem>,
        install: () -> Unit
    ) {
        val context = parentFragment.requireContext()

        binding.titleView.text = context.getString(R.string.download_install_dependencies, infoItem.title)
        binding.downloadButton.text = context.getString(R.string.download_install, infoItem.title)

        mData.sort()
        val adapter = ModDependenciesAdapter(parentFragment, infoItem, mData)
        adapter.setOnItemCLickListener { this.dismiss() }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards))
        binding.recyclerView.adapter = adapter

        binding.closeButton.setOnClickListener { this.dismiss() }
        binding.downloadButton.setOnClickListener {
            install()
            this.dismiss()
        }
    }
}
