package com.kdt.mcgui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.res.ResourcesCompat;


import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.authenticator.microsoft.PresentedException;
import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;

import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.feature.login.AuthResult;
import com.movtery.pojavzh.feature.login.OtherLoginApi;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.spse.extended_view.ExtendedTextView;

public class mcAccountSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {
    public mcAccountSpinner(@NonNull Context context) {
        this(context, null);
    }
    public mcAccountSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private final List<String> mAccountList = new ArrayList<>(2);
    private MinecraftAccount mSelectecAccount = null;
    private AccountsManager accountsManager;

    /* Display the head of the current profile, here just to allow bitmap recycling */
    private BitmapDrawable mHeadDrawable;

    /* Current animator to for the login bar, is swapped when changing step */
    private ObjectAnimator mLoginBarAnimator;
    private float mLoginBarWidth = -1;

    /* Paint used to display the bottom bar, to show the login progress. */
    private final Paint mLoginBarPaint = new Paint();

    /* When a login is performed in the background, we need to know where we are */
    private final static int MAX_LOGIN_STEP = 5;
    private int mLoginStep = 0;

    /* Login listeners */
    private final ProgressListener mProgressListener = step -> {
        // Animate the login bar, cosmetic purposes only
        mLoginStep = step;
        if(mLoginBarAnimator != null){
            mLoginBarAnimator.cancel();
            mLoginBarAnimator.setFloatValues( mLoginBarWidth, (getWidth()/MAX_LOGIN_STEP * mLoginStep));
        }else{
            mLoginBarAnimator = ObjectAnimator.ofFloat(this, "LoginBarWidth", mLoginBarWidth, (getWidth()/MAX_LOGIN_STEP * mLoginStep));
        }
        mLoginBarAnimator.start();
    };

    private final DoneListener mDoneListener = account -> {
        mLoginBarPaint.setColor(Color.TRANSPARENT);
        Toast.makeText(getContext(), R.string.main_login_done, Toast.LENGTH_SHORT).show();

        // Check if the account being added is not one that is already existing
        // Like login twice on the same mc account...
        for(String mcAccountName : mAccountList){
            if(mcAccountName.equals(account.username)) return;
        }

        mSelectecAccount = account;
        invalidate();
        mAccountList.add(account.username);
        reloadAccounts(false, mAccountList.size() -1);
        accountsManager.add(account);
    };

    private final ErrorListener mErrorListener = errorMessage -> {
        mLoginBarPaint.setColor(Color.RED);
        Context context = getContext();
        if(errorMessage instanceof PresentedException) {
            PresentedException exception = (PresentedException) errorMessage;
            Throwable cause = exception.getCause();
            if(cause == null) {
                Tools.dialog(context, context.getString(R.string.global_error), exception.toString(context));
            }else {
                Tools.showError(context, exception.toString(context), exception.getCause());
            }
        }else {
            Tools.showError(getContext(), errorMessage);
        }
        invalidate();
    };

    /* Triggered when we need to do microsoft login */
    private final ExtraListener<Uri> mMicrosoftLoginListener = (key, value) -> {
        mLoginBarPaint.setColor(getResources().getColor(R.color.background_bottom_bar, getContext().getTheme()));
        new MicrosoftBackgroundLogin(false, value.getQueryParameter("code")).performLogin(
                mProgressListener, mDoneListener, mErrorListener);
        return false;
    };

    /* Triggered when we need to perform mojang login */
    private final ExtraListener<String[]> mLocalLoginListener = (key, value) -> {
        if(value[1].isEmpty()){ // Test mode
            MinecraftAccount account = new MinecraftAccount();
            account.username = value[0];
            try {
                account.save();
            }catch (IOException e){
                Log.e("McAccountSpinner", "Failed to save the account : " + e);
            }

            mDoneListener.onLoginDone(account);
        }
        return false;
    };

    private final ExtraListener<MinecraftAccount> mOtherLoginListener = (key, value) -> {
        try {
            value.save();
        }catch (IOException e){
            Log.e("McAccountSpinner", "Failed to save the account : " + e);
        }
        mDoneListener.onLoginDone(value);
        return false;
    };

    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        // Set visual properties
        setBackgroundColor(getResources().getColor(R.color.background_bottom_bar, getContext().getTheme()));
        mLoginBarPaint.setColor(getResources().getColor(R.color.background_bottom_bar, getContext().getTheme()));
        mLoginBarPaint.setStrokeWidth(getResources().getDimensionPixelOffset(R.dimen._1sdp));

        accountsManager = new AccountsManager();

        // Set behavior
        reloadAccounts(true, 0);
        setOnItemSelectedListener(this);

        ExtraCore.addExtraListener(ExtraConstants.MICROSOFT_LOGIN_TODO, mMicrosoftLoginListener);
        ExtraCore.addExtraListener(ZHExtraConstants.LOCAL_LOGIN_TODO, mLocalLoginListener);
        ExtraCore.addExtraListener(ZHExtraConstants.OTHER_LOGIN_TODO, mOtherLoginListener);
    }


    @Override
    public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){  // Add account button
            if(mAccountList.size() > 1){
                ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            }
            return;
        }

        pickAccount(position);
        if(mSelectecAccount != null)
            performLogin(mSelectecAccount);
    }

    @Override
    public final void onNothingSelected(AdapterView<?> parent) {}


    @Override
    protected void onDraw(Canvas canvas) {
        if(mLoginBarWidth == -1) mLoginBarWidth = getWidth(); // Initial draw

        float bottom = getHeight() - mLoginBarPaint.getStrokeWidth()/2;
        canvas.drawLine(0, bottom, mLoginBarWidth, bottom, mLoginBarPaint);
    }

    public void removeCurrentAccount() {
        int position = getSelectedItemPosition();
        if (position == 0) return;
        String userName = mAccountList.get(position);
        File accountFile = new File(Tools.DIR_ACCOUNT_NEW, userName + ".json");
        File userIconFile = new File(ZHTools.DIR_USER_ICON, userName + ".png");
        if (accountFile.exists()) FileUtils.deleteQuietly(accountFile);
        if (userIconFile.exists()) FileUtils.deleteQuietly(userIconFile);
        mAccountList.remove(position);

        reloadAccounts(false, 0);
        accountsManager.remove(userName);
    }

    @Keep
    public void setLoginBarWidth(float value){
        mLoginBarWidth = value;
        invalidate(); // Need to redraw each time this is changed
    }

    /** Allows checking whether we have an online account */
    public boolean isAccountOnline(){
        return mSelectecAccount != null && !mSelectecAccount.accessToken.equals("0");
    }

    public MinecraftAccount getSelectedAccount(){
        return mSelectecAccount;
    }

    public int getLoginState(){
        return mLoginStep;
    }

    public boolean isLoginDone(){
        return mLoginStep >= MAX_LOGIN_STEP;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setNoAccountBehavior(){
        // Set custom behavior when no account are present, to make it act as a button
        if(mAccountList.size() != 1){
            // Remove any touch listener
            setOnTouchListener(null);
            return;
        }

        // Make the spinner act like a button, since there is no item to really select
        setOnTouchListener((v, event) -> {
            if(event.getAction() != MotionEvent.ACTION_UP) return false;
            // The activity should intercept this and spawn another fragment
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return true;
        });
    }

    /**
     * Reload the spinner, from memory or from scratch. A default account can be selected
     * @param fromFiles Whether we use files as the source of truth
     * @param overridePosition Force the spinner to be at this position, if not 0
     */
    private void reloadAccounts(boolean fromFiles, int overridePosition) {
        if (fromFiles) {
            mAccountList.clear();
            mAccountList.add(getContext().getString(R.string.main_add_account));

            accountsManager.reload();
            for (MinecraftAccount minecraftAccount : AccountsManager.getAllAccount()) {
                mAccountList.add(minecraftAccount.username);
            }
        }

        String[] accountArray = mAccountList.toArray(new String[0]);
        AccountAdapter accountAdapter = new AccountAdapter(getContext(), R.layout.item_minecraft_account, accountArray);
        accountAdapter.setDropDownViewResource(R.layout.item_minecraft_account);
        setAdapter(accountAdapter);

        // Pick what's available, might just be the the add account "button"
        pickAccount(overridePosition == 0 ? -1 : overridePosition);
        if(mSelectecAccount != null)
            performLogin(mSelectecAccount);

        // Remove or add the behavior if needed
        setNoAccountBehavior();

    }

    private void performLogin(MinecraftAccount minecraftAccount) {
        if (minecraftAccount.isLocal()) return;
        if (!Objects.isNull(minecraftAccount.baseUrl) && !minecraftAccount.baseUrl.equals("0") && System.currentTimeMillis() > minecraftAccount.expiresAt) {
            OtherLoginApi.getINSTANCE().setBaseUrl(minecraftAccount.baseUrl);
            PojavApplication.sExecutorService.execute(() -> {
                try {
                    OtherLoginApi.getINSTANCE().login(getContext(), minecraftAccount.account, minecraftAccount.password, new OtherLoginApi.Listener() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            minecraftAccount.expiresAt = System.currentTimeMillis() + 30 * 60 * 1000;
                            minecraftAccount.accessToken = authResult.getAccessToken();
                            ((Activity) getContext()).runOnUiThread(() -> ExtraCore.setValue(ZHExtraConstants.OTHER_LOGIN_TODO, minecraftAccount));
                        }

                        @Override
                        public void onFailed(String error) {
                            mErrorListener.onLoginError(new Throwable(error));
                        }
                    });
                } catch (IOException e) {
                    mErrorListener.onLoginError(e);
                }
            });
            return;
        }

        mLoginBarPaint.setColor(getResources().getColor(R.color.background_bottom_bar, getContext().getTheme()));
        if (minecraftAccount.isMicrosoft) {
            if (System.currentTimeMillis() > minecraftAccount.expiresAt) {
                // Perform login only if needed
                new MicrosoftBackgroundLogin(true, minecraftAccount.msaRefreshToken)
                        .performLogin(mProgressListener, mDoneListener, mErrorListener);
            }
        }
    }

    /** Pick the selected account, the one in settings if 0 is passed */
    private void pickAccount(int position){
        MinecraftAccount selectedAccount;
        if(position != -1){
            PojavProfile.setCurrentProfile(getContext(), mAccountList.get(position));
            selectedAccount = PojavProfile.getCurrentProfileContent(getContext(), mAccountList.get(position));


            // WORKAROUND
            // Account file corrupted due to previous versions having improper encoding
            if (selectedAccount == null){
                removeCurrentAccount();
                pickAccount(-1);
                setSelection(0);
                return;
            }
            setSelection(position);
        }else {
            // Get the current profile, or the first available profile if the wanted one is unavailable
            selectedAccount = PojavProfile.getCurrentProfileContent(getContext(), null);
            int spinnerPosition = selectedAccount == null
                    ? mAccountList.size() <= 1 ? 0 : 1
                    : mAccountList.indexOf(selectedAccount.username);
            setSelection(spinnerPosition, false);
        }

        mSelectecAccount = selectedAccount;
        setImageFromSelectedAccount();
    }

    @Deprecated()
    /* Legacy behavior, update the head image manually for the selected account */
    private void setImageFromSelectedAccount(){
        BitmapDrawable oldBitmapDrawable = mHeadDrawable;

        if(mSelectecAccount != null){
            ExtendedTextView view = ((ExtendedTextView) getSelectedView());
            if(view != null){
                Bitmap bitmap = mSelectecAccount.getSkinFace();
                if(bitmap != null) {
                    mHeadDrawable = new BitmapDrawable(getResources(), bitmap);
                    view.setCompoundDrawables(mHeadDrawable, null, null, null);
                }else{
                    view.setCompoundDrawables(null, null, null, null);
                }
                view.postProcessDrawables();
            }
        }

        if(oldBitmapDrawable != null){
            oldBitmapDrawable.getBitmap().recycle();
        }
    }


    private static class AccountAdapter extends ArrayAdapter<String> {

        private final HashMap<String, Drawable> mImageCache = new HashMap<>();
        public AccountAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minecraft_account, parent, false);
            }
            ExtendedTextView textview = (ExtendedTextView) convertView;
            textview.setText(super.getItem(position));

            // Handle the "Add account section"
            if(position == 0) textview.setCompoundDrawables(ResourcesCompat.getDrawable(parent.getResources(), R.drawable.ic_add, null), null, null, null);
            else {
                String username = super.getItem(position);
                Drawable accountHead = mImageCache.get(username);
                if (accountHead == null){
                    accountHead = new BitmapDrawable(parent.getResources(), MinecraftAccount.getSkinFace(username));
                    mImageCache.put(username, accountHead);
                }
                textview.setCompoundDrawables(accountHead, null, null, null);
            }
            return convertView;
        }
    }

}