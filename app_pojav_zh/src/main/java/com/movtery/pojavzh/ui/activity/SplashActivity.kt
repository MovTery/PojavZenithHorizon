package com.movtery.pojavzh.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.pojavzh.feature.background.BackgroundManager.setBackgroundImage
import com.movtery.pojavzh.feature.background.BackgroundType
import com.movtery.pojavzh.feature.unpack.Components
import com.movtery.pojavzh.feature.unpack.Jre
import com.movtery.pojavzh.feature.unpack.UnpackComponentsTask
import com.movtery.pojavzh.feature.unpack.UnpackJreTask
import com.movtery.pojavzh.feature.unpack.UnpackSingleFilesTask
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.MissingStorageActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var isStarted: Boolean = false
    private lateinit var binding: ActivitySplashBinding
    private lateinit var installableAdapter: InstallableAdapter
    private val items: MutableList<InstallableItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItems()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val splashText = findViewById<TextView>(R.id.splash_text)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SplashActivity)
            adapter = installableAdapter
        }

        binding.startButton.apply {
            setOnClickListener {
                if (isStarted) return@setOnClickListener
                isStarted = true
                splashText.setText(R.string.splash_screen_installing)
                installableAdapter.startAllTasks()
            }
            isClickable = false
        }

        if (!Tools.checkStorageRoot()) {
            finish()
            startActivity(Intent(this, MissingStorageActivity::class.java))
            return
        } else {
            setBackgroundImage(this, BackgroundType.MAIN_MENU, findViewById(R.id.background_view))
            checkEnd()
        }
    }

    private fun initItems() {
        Components.entries.forEach {
            val unpackComponentsTask = UnpackComponentsTask(this, it)
            if (!unpackComponentsTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.component,
                        it.summary?.let { it1 -> getString(it1) },
                        unpackComponentsTask
                    )
                )
            }
        }
        Jre.entries.forEach {
            val unpackJreTask = UnpackJreTask(this, it)
            if (!unpackJreTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.jreName,
                        getString(it.summary),
                        unpackJreTask
                    )
                )
            }
        }
        items.sort()
        installableAdapter = InstallableAdapter(items) {
            toMain()
        }
    }
    
    private fun checkEnd() {
        installableAdapter.checkAllTask()
        PojavApplication.sExecutorService.execute {
            UnpackSingleFilesTask(this).run()
        }

        binding.startButton.isClickable = true
    }

    private fun toMain() {
        finish()
        startActivity(Intent(this, LauncherActivity::class.java))
    }
}