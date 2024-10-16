package com.movtery.pojavzh.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.movtery.anim.AnimPlayer
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.anim.SlideAnimation
import net.kdt.pojavlaunch.R

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS: Int = 0
    }

    private var animPlayer: AnimPlayer = AnimPlayer()

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onStart() {
        super.onStart()
        playAnimation { slideIn(it) }
    }

    fun slideOut() {
        playAnimation { slideOut(it) }
    }

    private fun playAnimation(animationAction: (AnimPlayer) -> Unit) {
        if (AllSettings.animation) {
            animPlayer.clearEntries()
            animPlayer.apply {
                animationAction(this)
                start()
            }
        }
    }

    fun checkPermissions(title: Int = R.string.generic_warning, permissionGranted: PermissionGranted?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handlePermissionsForAndroid11AndAbove(title, permissionGranted)
        } else {
            handlePermissionsForAndroid10AndBelow(title, permissionGranted)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun handlePermissionsForAndroid11AndAbove(title: Int, permissionGranted: PermissionGranted?) {
        if (!Environment.isExternalStorageManager()) {
            showPermissionRequestDialog(title) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:" + requireActivity().packageName))
                startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
            }
        } else {
            permissionGranted?.granted()
        }
    }

    private fun handlePermissionsForAndroid10AndBelow(title: Int, permissionGranted: PermissionGranted?) {
        if (!hasStoragePermissions()) {
            showPermissionRequestDialog(title) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_PERMISSIONS
                )
            }
        } else {
            permissionGranted?.granted()
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRequestDialog(title: Int, requestPermissions: RequestPermissions) {
        TipDialog.Builder(requireActivity())
            .setTitle(title)
            .setMessage(getString(R.string.permissions_manage_external_storage))
            .setConfirmClickListener { requestPermissions.onRequest() }
            .setCancelable(false)
            .buildDialog()
    }

    fun interface RequestPermissions {
        fun onRequest()
    }

    fun interface PermissionGranted {
        fun granted()
    }
}
