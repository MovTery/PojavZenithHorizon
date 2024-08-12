package com.movtery.pojavzh.ui.dialog;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.log.Logging;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.imgcropper.BitmapCropBehaviour;
import net.kdt.pojavlaunch.imgcropper.CropperBehaviour;
import net.kdt.pojavlaunch.imgcropper.CropperView;
import net.kdt.pojavlaunch.imgcropper.RegionDecoderCropBehaviour;
import net.kdt.pojavlaunch.utils.CropperUtils;

import java.io.IOException;
import java.io.InputStream;

public class CropperDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final Context context;
    private final Uri selectedUri;
    private final CropperUtils.CropperListener cropperListener;
    private CropperView cropImageView;
    private View finishProgressBar;

    public CropperDialog(@NonNull Context context, Uri selectedUri, final CropperUtils.CropperListener cropperListener) {
        super(context);
        this.context = context;
        this.selectedUri = selectedUri;
        this.cropperListener = cropperListener;

        setCancelable(false);
        setContentView(R.layout.dialog_cropper);

        init();
        DraggableDialog.initDialog(this);
    }

    private void init() {
        cropImageView = findViewById(R.id.crop_dialog_view);
        finishProgressBar = findViewById(R.id.crop_dialog_progressbar);
        ImageView mCloseButton = findViewById(R.id.zh_crop_dialog_close);
        ImageView mConfirmButton = findViewById(R.id.zh_crop_dialog_confirm);

        bindValues();

        mCloseButton.setOnClickListener(v -> dismiss());
        mConfirmButton.setOnClickListener(v -> {
            dismiss();
            cropperListener.onCropped(cropImageView.crop((int) Tools.dpToPx(70)));
        });
    }

    private void bindValues() {
        ToggleButton horizontalLock = findViewById(R.id.crop_dialog_hlock);
        ToggleButton verticalLock = findViewById(R.id.crop_dialog_vlock);
        Button reset = findViewById(R.id.crop_dialog_reset);

        horizontalLock.setOnClickListener(v -> cropImageView.horizontalLock = horizontalLock.isChecked());
        verticalLock.setOnClickListener(v -> cropImageView.verticalLock = verticalLock.isChecked());
        reset.setOnClickListener(v -> cropImageView.resetTransforms());
    }

    @Override
    public void show() {
        super.show();
        ContentResolver contentResolver = context.getContentResolver();

        PojavApplication.sExecutorService.execute(() -> {
            CropperBehaviour cropperBehaviour = null;
            try {
                cropperBehaviour = createBehaviour(cropImageView, contentResolver, selectedUri);
            } catch (Exception e) {
                cropperListener.onFailed(e);
            }
            CropperBehaviour finalBehaviour = cropperBehaviour;
            Tools.runOnUiThread(() -> finishSetup(finishProgressBar, cropImageView, finalBehaviour));
        });
    }

    private CropperBehaviour createBehaviour(CropperView cropImageView, ContentResolver contentResolver, Uri selectedUri) throws Exception {
        try (InputStream inputStream = contentResolver.openInputStream(selectedUri)) {
            if (inputStream == null) return null;
            try {
                BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                RegionDecoderCropBehaviour cropBehaviour = new RegionDecoderCropBehaviour(cropImageView);
                cropBehaviour.setRegionDecoder(regionDecoder);
                return cropBehaviour;
            } catch (IOException e) {
                // Catch IOE here to detect the case when BitmapRegionDecoder does not support this image format.
                // If it does not, we will just have to load the bitmap in full resolution using BitmapFactory.
                Logging.w("CropperUtils", "Failed to load image into BitmapRegionDecoder", e);
            }
        }
        // We can safely re-open the stream here as ACTION_OPEN_DOCUMENT grants us long-term access
        // to the file that we have picked.
        try (InputStream inputStream = contentResolver.openInputStream(selectedUri)) {
            if (inputStream == null) return null;
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            BitmapCropBehaviour cropBehaviour = new BitmapCropBehaviour(cropImageView);
            cropBehaviour.setBitmap(originalBitmap);
            return cropBehaviour;
        }
    }

    private void finishSetup(View progressBar, CropperView cropImageView, CropperBehaviour cropperBehaviour) {
        if (cropperBehaviour == null) {
            dismiss();
            return;
        }
        progressBar.setVisibility(View.GONE);
        cropImageView.setCropperBehaviour(cropperBehaviour);
        cropperBehaviour.applyImage();
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
