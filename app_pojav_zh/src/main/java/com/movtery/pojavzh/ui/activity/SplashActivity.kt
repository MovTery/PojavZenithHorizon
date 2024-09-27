package com.movtery.pojavzh.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.background.BackgroundManager
import com.movtery.pojavzh.feature.background.BackgroundType
import com.movtery.pojavzh.feature.unpack.Components
import com.movtery.pojavzh.feature.unpack.Jre
import com.movtery.pojavzh.feature.unpack.UnpackComponentsTask
import com.movtery.pojavzh.feature.unpack.UnpackJreTask
import com.movtery.pojavzh.feature.unpack.UnpackSingleFilesTask
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.ui.view.AnimButton
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.MissingStorageActivity
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    companion object {
        const val REQUEST_CODE_PERMISSIONS: Int = 0
    }
    private var isStarted: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var startButton: AnimButton
    private lateinit var adapter: InstallableAdapter
    private val items: MutableList<InstallableItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initItems()

        setContentView(R.layout.activity_splash)

        BackgroundManager.getInstance()?.setBackgroundImage(
            BackgroundType.MAIN_MENU,
            findViewById(R.id.background_view)
        )

        startButton = findViewById(R.id.start_button)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        startButton.apply {
            setOnClickListener {
                if (isStarted) return@setOnClickListener
                isStarted = true
                adapter.startAllTasks()
            }
            isClickable = false
        }

        checkPermissions()
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

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handlePermissionsForAndroid11AndAbove()
        } else {
            handlePermissionsForAndroid10AndBelow()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            checkPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && hasAllPermissionsGranted(
                grantResults
            )
        ) {
            checkEnd()
        } else {
            checkPermissions()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun handlePermissionsForAndroid11AndAbove() {
        if (!Environment.isExternalStorageManager()) {
            showDialog {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
            }
        } else {
            checkEnd()
        }
    }

    private fun handlePermissionsForAndroid10AndBelow() {
        if (!hasStoragePermissions()) {
            showDialog {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_PERMISSIONS
                )
            }
        } else {
            checkEnd()
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun showDialog(requestPermissions: RequestPermissions) {
        TipDialog.Builder(this)
            .setMessage(getString(R.string.zh_permissions_manage_external_storage))
            .setConfirmClickListener { requestPermissions.onRequest() }
            .setCancelClickListener { this.finish() }
            .setCancelable(false)

            .buildDialog()
    }

    private fun interface RequestPermissions {
        fun onRequest()
    }
    
    private fun checkEnd() {
        if (!Tools.checkStorageRoot()) {
            finish()
            startActivity(Intent(this, MissingStorageActivity::class.java))
            return
        }
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