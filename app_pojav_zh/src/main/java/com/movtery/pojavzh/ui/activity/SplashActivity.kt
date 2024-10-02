package com.movtery.pojavzh.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.background.BackgroundManager.setBackgroundImage
import com.movtery.pojavzh.feature.background.BackgroundType
import com.movtery.pojavzh.feature.unpack.Components
import com.movtery.pojavzh.feature.unpack.Jre
import com.movtery.pojavzh.feature.unpack.UnpackComponentsTask
import com.movtery.pojavzh.feature.unpack.UnpackJreTask
import com.movtery.pojavzh.feature.unpack.UnpackSingleFilesTask
import com.movtery.pojavzh.ui.view.AnimButton
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.MissingStorageActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var isStarted: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var startButton: AnimButton
    private lateinit var adapter: InstallableAdapter
    private val items: MutableList<InstallableItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initItems()

        setContentView(R.layout.activity_splash)

        val splashText = findViewById<TextView>(R.id.splash_text)
        startButton = findViewById(R.id.start_button)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        startButton.apply {
            setOnClickListener {
                if (isStarted) return@setOnClickListener
                isStarted = true
                splashText.setText(R.string.splash_screen_installing)
                adapter.startAllTasks()
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
        adapter = InstallableAdapter(items) {
            toMain()
        }
    }
    
    private fun checkEnd() {
        adapter.checkAllTask()
        PojavApplication.sExecutorService.execute {
            UnpackSingleFilesTask(this).run()
        }

        startButton.isClickable = true
    }

    private fun toMain() {
        finish()
        startActivity(Intent(this, LauncherActivity::class.java))
    }
}