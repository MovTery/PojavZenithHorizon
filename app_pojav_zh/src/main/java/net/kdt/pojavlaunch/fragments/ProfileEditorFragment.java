package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.event.sticky.FileSelectorEvent;
import com.movtery.pojavzh.event.sticky.RefreshVersionSpinnerEvent;
import com.movtery.pojavzh.event.sticky.VersionSelectorEvent;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.ControlButtonFragment;
import com.movtery.pojavzh.ui.fragment.FilesFragment;
import com.movtery.pojavzh.ui.fragment.VersionSelectorFragment;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.file.FileTools;
import com.skydoves.powerspinner.DefaultSpinnerAdapter;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.FragmentProfileEditorBinding;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.profiles.ProfileIconCache;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProfileEditorFragment extends FragmentWithAnim {
    public static final String TAG = "ProfileEditorFragment";
    public static final String DELETED_PROFILE = "deleted_profile";

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
                try (
                        InputStream inputStream = requireActivity().getContentResolver().openInputStream(result)
                ) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    onImageSelected(bitmap);
                } catch (Exception e) {
                    Logging.e("ProfileEditorFragment", Tools.printToString(e));
                }
            });

    private FragmentProfileEditorBinding binding;
    private String mProfileKey;
    private MinecraftProfile mTempProfile = null;
    private String mValueToConsume = "";

    private List<String> mRenderNames;

    public ProfileEditorFragment(){
        super(R.layout.fragment_profile_editor);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Paths, which can be changed
        FileSelectorEvent fileSelectorEvent = EventBus.getDefault().getStickyEvent(FileSelectorEvent.class);
        VersionSelectorEvent versionSelectorEvent = EventBus.getDefault().getStickyEvent(VersionSelectorEvent.class);

        if (mTempProfile != null) {
            if (fileSelectorEvent != null && fileSelectorEvent.getPath() != null) {
                String path = fileSelectorEvent.getPath();
                if (mValueToConsume.equals(FilesFragment.BUNDLE_SELECT_FOLDER_MODE)) {
                    mTempProfile.gameDir = path;
                } else {
                    mTempProfile.controlFile = path;
                }
            }

            //选择版本
            if (versionSelectorEvent != null && versionSelectorEvent.getVersion() != null) {
                String version = versionSelectorEvent.getVersion();
                mTempProfile.lastVersionId = version;
                binding.vprofEditorVersionSpinner.setText(version);
            }
        }

        if (fileSelectorEvent != null) EventBus.getDefault().removeStickyEvent(fileSelectorEvent);
        if (versionSelectorEvent != null) EventBus.getDefault().removeStickyEvent(versionSelectorEvent);

        binding = FragmentProfileEditorBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Set up behaviors
        binding.vprofEditorCancelButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        binding.vprofEditorSaveButton.setOnClickListener(v -> {
            ProfileIconCache.dropIcon(mProfileKey);
            save();
            Tools.backToMainMenu(requireActivity());
        });

        binding.vprofEditorPathButton.setOnClickListener(v -> {
            File dir = new File(PathAndUrlManager.DIR_GAME_DEFAULT);
            if (!dir.exists()) FileTools.mkdirs(dir);
            Bundle bundle = new Bundle();
            bundle.putBoolean(FilesFragment.BUNDLE_SELECT_FOLDER_MODE, true);
            bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILE, false);
            bundle.putBoolean(FilesFragment.BUNDLE_QUICK_ACCESS_PATHS, false);
            bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, ProfilePathManager.getCurrentPath());
            mValueToConsume = FilesFragment.BUNDLE_SELECT_FOLDER_MODE;

            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        });

        binding.vprofEditorCtrlButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ControlButtonFragment.BUNDLE_SELECT_CONTROL, true);
            mValueToConsume = ControlButtonFragment.BUNDLE_SELECT_CONTROL;

            ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, bundle);
        });

        // 切换至版本选择界面
        binding.vprofEditorVersionButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
                VersionSelectorFragment.class, VersionSelectorFragment.TAG, null));

        // Set up the icon change click listener
        binding.vprofEditorProfileLayout.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        loadValues(Objects.requireNonNull(AllSettings.Companion.getCurrentProfile()), view.getContext());
    }

    @Override
    public void onPause() {
        binding.vprofEditorSpinnerRuntime.dismiss();
        binding.vprofEditorProfileRenderer.dismiss();
        super.onPause();
    }

    private void loadValues(@NonNull String profile, @NonNull Context context){
        if(mTempProfile == null){
            mTempProfile = getProfile(profile);
        }
        binding.vprofEditorProfileIcon.setImageDrawable(
                ProfileIconCache.fetchIcon(getResources(), mProfileKey, mTempProfile.icon)
        );

        // Runtime spinner
        List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        List<String> runtimeNames = new ArrayList<>();
        runtimes.forEach(v -> runtimeNames.add(String.format("%s - %s", v.name, v.versionString == null ? getString(R.string.multirt_runtime_corrupt) : v.versionString)));
        runtimeNames.add(getString(R.string.generic_default));
        int jvmIndex = runtimeNames.size() - 1;
        if (mTempProfile.javaDir != null) {
            String selectedRuntime = mTempProfile.javaDir.substring(Tools.LAUNCHERPROFILES_RTPREFIX.length());
            int nindex = runtimes.indexOf(new Runtime(selectedRuntime));
            if (nindex != -1) jvmIndex = nindex;
        }
        DefaultSpinnerAdapter runtimeAdapter = new DefaultSpinnerAdapter(binding.vprofEditorSpinnerRuntime);
        runtimeAdapter.setItems(runtimeNames);
        binding.vprofEditorSpinnerRuntime.setSpinnerAdapter(runtimeAdapter);
        binding.vprofEditorSpinnerRuntime.selectItemByIndex(jvmIndex);
        binding.vprofEditorSpinnerRuntime.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> {
            if (i1 == runtimeNames.size() - 1) mTempProfile.javaDir = null;
            else {
                Runtime runtime = runtimes.get(i1);
                mTempProfile.javaDir = runtime.versionString == null ? null : Tools.LAUNCHERPROFILES_RTPREFIX + runtime.name;
            }
        });

        // Renderer spinner
        Tools.RenderersList renderersList = Tools.getCompatibleRenderers(context);
        mRenderNames = renderersList.rendererIds;
        List<String> renderList = new ArrayList<>(renderersList.rendererDisplayNames.length + 1);
        renderList.addAll(Arrays.asList(renderersList.rendererDisplayNames));
        renderList.add(context.getString(R.string.generic_default));
        int rendererIndex = renderList.size() - 1;
        if(mTempProfile.pojavRendererName != null) {
            int nindex = mRenderNames.indexOf(mTempProfile.pojavRendererName);
            if(nindex != -1) rendererIndex = nindex;
        }
        DefaultSpinnerAdapter rendererAdapter = new DefaultSpinnerAdapter(binding.vprofEditorProfileRenderer);
        rendererAdapter.setItems(renderList);
        binding.vprofEditorProfileRenderer.setSpinnerAdapter(rendererAdapter);
        binding.vprofEditorProfileRenderer.selectItemByIndex(rendererIndex);
        binding.vprofEditorProfileRenderer.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> {
            if(i1 == renderList.size() - 1) mTempProfile.pojavRendererName = null;
            else mTempProfile.pojavRendererName = mRenderNames.get(i1);
        });

        binding.vprofEditorVersionSpinner.setText(mTempProfile.lastVersionId);
        binding.vprofEditorJreArgs.setText(mTempProfile.javaArgs == null ? "" : mTempProfile.javaArgs);
        binding.vprofEditorProfileName.setText(mTempProfile.name);
        binding.vprofEditorPath.setText(mTempProfile.gameDir == null ? "" : mTempProfile.gameDir);
        binding.vprofEditorCtrlSpinner.setText(mTempProfile.controlFile == null ? "" : mTempProfile.controlFile);
    }

    private MinecraftProfile getProfile(@NonNull String profile){
        MinecraftProfile minecraftProfile;
        if(getArguments() == null) {
            // EDGE CASE: User leaves Pojav in background. Pojav gets terminated in the background.
            // Current selected fragment and its arguments are saved.
            // User returns to Pojav. Android restarts process and reinitializes fragment without
            // going to the main screen. mainProfileJson and profiles left uninitialized, which
            // results in a crash.
            // Reload the profiles to avoid this edge case.
            LauncherProfiles.load();
            MinecraftProfile originalProfile = LauncherProfiles.mainProfileJson.profiles.get(profile);
            // EDGE CASE: User edits the JSON, so the profile that was edited no longer exists.
            // Create a brand new profile as a fallback for this case.
            if(originalProfile != null) minecraftProfile = new MinecraftProfile(originalProfile);
            else minecraftProfile = MinecraftProfile.createTemplate();
            mProfileKey = profile;
        }else{
            minecraftProfile = MinecraftProfile.createTemplate();
            mProfileKey = LauncherProfiles.getFreeProfileKey();
        }
        return minecraftProfile;
    }

    private void save(){
        //First, check for potential issues in the inputs
        mTempProfile.lastVersionId = binding.vprofEditorVersionSpinner.getText().toString();
        mTempProfile.controlFile = binding.vprofEditorCtrlSpinner.getText().toString();
        mTempProfile.name = binding.vprofEditorProfileName.getText().toString();
        mTempProfile.javaArgs = binding.vprofEditorJreArgs.getText().toString();
        mTempProfile.gameDir = binding.vprofEditorPath.getText().toString();

        if(mTempProfile.controlFile.isEmpty()) mTempProfile.controlFile = null;
        if(mTempProfile.javaArgs.isEmpty()) mTempProfile.javaArgs = null;
        if(mTempProfile.gameDir.isEmpty()) mTempProfile.gameDir = null;

        LauncherProfiles.mainProfileJson.profiles.put(mProfileKey, mTempProfile);
        LauncherProfiles.write(ProfilePathManager.getCurrentProfile());
        EventBus.getDefault().postSticky(new RefreshVersionSpinnerEvent(mProfileKey));
    }

    private void onImageSelected(Bitmap bitmap) {
        Glide.with(requireContext())
                .load(bitmap)
                .into(binding.vprofEditorProfileIcon);

        Logging.i("ProfileEditorFragment", "The icon has been updated");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream, Base64.NO_WRAP)) {
            bitmap.compress(
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.R ?
                            // On Android < 30, there was no distinction between "lossy" and "lossless",
                            // and the type is picked by the quality parameter. We set the quality to 60.
                            // so it should be lossy,
                            Bitmap.CompressFormat.WEBP :
                            // On Android >= 30, we can explicitly specify that we want lossy compression
                            // with the visual quality of 60.
                            Bitmap.CompressFormat.WEBP_LOSSY,
                    60,
                    base64OutputStream
            );
            base64OutputStream.flush();
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            Tools.showErrorRemote(e);
            return;
        }
        String iconLine = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        mTempProfile.icon = "data:image/webp;base64," + iconLine;
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.editorLayout, Animations.BounceInDown))
                .apply(new AnimPlayer.Entry(binding.operateLayout, Animations.BounceInLeft))
                .apply(new AnimPlayer.Entry(binding.vprofEditorProfileLayout, Animations.Wobble));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.editorLayout, Animations.FadeOutUp))
                .apply(new AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight));
    }
}
