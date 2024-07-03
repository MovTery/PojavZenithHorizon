package net.kdt.pojavlaunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.movtery.pojavzh.ui.dialog.CropperDialog;

import net.kdt.pojavlaunch.R;

public class CropperUtils {
    public static ActivityResultLauncher<?> registerCropper(Fragment fragment, final CropperListener cropperListener) {
        return fragment.registerForActivityResult(new ActivityResultContracts.OpenDocument(), (result)->{
            Context context = fragment.getContext();
            if(context == null) return;
            if (result == null) {
                Toast.makeText(context, R.string.cropper_select_cancelled, Toast.LENGTH_SHORT).show();
                return;
            }
            openCropperDialog(context, result, cropperListener);
        });
    }

    private static void openCropperDialog(Context context, Uri selectedUri,
                                          final CropperListener cropperListener) {
        new CropperDialog(context, selectedUri, cropperListener).show();
    }

    @SuppressWarnings("unchecked")
    public static void startCropper(ActivityResultLauncher<?> resultLauncher) {
        ActivityResultLauncher<String[]> realResultLauncher =
                (ActivityResultLauncher<String[]>) resultLauncher;
        realResultLauncher.launch(new String[]{"image/*"});
    }

    public interface CropperListener {
        void onCropped(Bitmap contentBitmap);
        void onFailed(Exception exception);
    }
}
