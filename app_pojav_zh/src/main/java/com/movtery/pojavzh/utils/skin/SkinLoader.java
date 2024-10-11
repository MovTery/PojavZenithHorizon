package com.movtery.pojavzh.utils.skin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.movtery.pojavzh.feature.accounts.AccountUtils;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SkinLoader {
    public static Drawable getAvatarDrawable(Context context, MinecraftAccount account, int size) throws IOException {
        if (account.isMicrosoft || AccountUtils.isOtherLoginAccount(account)) {
            File skin = new File(PathAndUrlManager.DIR_USER_SKIN, account.username + ".png");
            if (skin.exists()) {
                try (InputStream is = Files.newInputStream(skin.toPath())) {
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    return new BitmapDrawable(context.getResources(), getAvatar(bitmap, size));
                }
            }
        }
        return getDefaultAvatar(context, size);
    }

    private static Drawable getDefaultAvatar(Context context, int size) throws IOException {
        InputStream is = context.getAssets().open("steve.png");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return new BitmapDrawable(context.getResources(), getAvatar(bitmap, size));
    }

    //使用了源代码：https://github.com/MovTery/FoldCraftLauncher/blob/main/FCL/src/main/java/com/tungsten/fcl/game/TexturesLoader.java#L318
    public static Bitmap getAvatar(Bitmap skin, int size) {
        int faceOffset = (int) Math.round(size / 18.0);
        Bitmap faceBitmap = Bitmap.createBitmap(skin, 8, 8, 8, 8, (Matrix) null, false);
        Bitmap hatBitmap = Bitmap.createBitmap(skin, 40, 8, 8, 8, (Matrix) null, false);
        Bitmap avatar = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(avatar);
        Matrix matrix;
        float faceScale = ((size - 2 * faceOffset) / 8f);
        float hatScale = (size / 8f);
        matrix = new Matrix();
        matrix.postScale(faceScale, faceScale);
        Bitmap newFaceBitmap = Bitmap.createBitmap(faceBitmap, 0, 0 , 8, 8, matrix, false);
        matrix = new Matrix();
        matrix.postScale(hatScale, hatScale);
        Bitmap newHatBitmap = Bitmap.createBitmap(hatBitmap, 0, 0, 8, 8, matrix, false);
        canvas.drawBitmap(newFaceBitmap, faceOffset, faceOffset, new Paint(Paint.ANTI_ALIAS_FLAG));
        canvas.drawBitmap(newHatBitmap, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));
        return avatar;
    }
}
