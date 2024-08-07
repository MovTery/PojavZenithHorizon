package net.kdt.pojavlaunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.UnpackJRE;

import net.kdt.pojavlaunch.tasks.AsyncAssetManager;

public class TestStorageActivity extends BaseActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handlePermissionsForAndroid11AndAbove();
        } else {
            handlePermissionsForAndroid10AndBelow();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && hasAllPermissionsGranted(grantResults)) {
            exit();
        } else {
            checkPermissions();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void handlePermissionsForAndroid11AndAbove() {
        if (!Environment.isExternalStorageManager()) {
            showDialog(() -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_PERMISSIONS);
            });
        } else {
            exit();
        }
    }

    private void handlePermissionsForAndroid10AndBelow() {
        if (!hasStoragePermissions()) {
            showDialog(() -> ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSIONS));
        } else {
            exit();
        }
    }

    private boolean hasStoragePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showDialog(RequestPermissions requestPermissions) {
        new TipDialog.Builder(this)
                .setMessage(getString(R.string.zh_permissions_manage_external_storage))
                .setConfirmClickListener(requestPermissions::onRequest)
                .setCancelClickListener(this::finish)
                .setCancelable(false)

                .buildDialog();
    }

    private interface RequestPermissions {
        void onRequest();
    }

    private void exit() {
        if (!Tools.checkStorageRoot()) {
            startActivity(new Intent(this, MissingStorageActivity.class));
            return;
        }
        //Only run them once we get a definitive green light to use storage
        AsyncAssetManager.unpackComponents(this);
        AsyncAssetManager.unpackSingleFiles(this);
        UnpackJRE.unpackAllJre(getAssets()); //解压JRE8、JRE17、JRE21

        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }
}
