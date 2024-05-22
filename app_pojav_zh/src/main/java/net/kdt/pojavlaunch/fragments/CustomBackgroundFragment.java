package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.DIR_BACKGROUND;
import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.PojavZHTools.isImage;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.movtery.background.BackgroundManager;
import com.movtery.background.BackgroundType;
import com.movtery.filelist.FileIcon;
import com.movtery.filelist.FileRecyclerView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CustomBackgroundFragment extends Fragment {

    public static final String TAG = "CustomBackgroundFragment";
    private final Map<BackgroundType, String> backgroundMap = new HashMap<>();
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mResetButton, mRefreshButton;
    private TabLayout mTabLayout;
    private FileRecyclerView mFileRecyclerView;
    private BackgroundType backgroundType;

    public CustomBackgroundFragment() {
        super(R.layout.fragment_custom_background);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, mFileRecyclerView.getFullPath().getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                mFileRecyclerView.listFileAt(backgroundPath());
                            });
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        initBackgroundMap();
        bindTabs();

        mFileRecyclerView.lockPathAt(backgroundPath());
        mFileRecyclerView.setShowFiles(true);
        mFileRecyclerView.setShowFolders(false);

        mFileRecyclerView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                refreshType(mTabLayout.getSelectedTabPosition());

                String fileName = file.getName();

                boolean image = isImage(file);
                FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                filesButton.setButtonVisibility(false, false, true, true, true, image); //默认虚拟鼠标不支持分享、重命名、删除操作

                String message;
                if (image) { //如果选中的不是一个图片，那么将显示默认的文件选择提示信息
                    message = getString(R.string.zh_custom_background_dialog_message, getCurrentStatusName());
                } else {
                    message = getString(R.string.zh_file_message);
                }

                filesButton.messageText = message;
                filesButton.moreButtonText = getString(R.string.global_select);

                FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileRecyclerView.refreshPath()), file);
                filesDialog.setMoreButtonClick(v -> {
                    backgroundMap.put(backgroundType, fileName);
                    BackgroundManager.saveProperties(backgroundMap);

                    Toast.makeText(requireContext(), getString(R.string.zh_custom_background_selected, getCurrentStatusName()) + fileName, Toast.LENGTH_SHORT).show();
                    filesDialog.dismiss();
                });
                filesDialog.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
            }
        });

        mResetButton.setOnClickListener(v -> {
            refreshType(mTabLayout.getSelectedTabPosition());

            backgroundMap.put(backgroundType, "null");
            BackgroundManager.saveProperties(backgroundMap);

            Toast.makeText(requireContext(), getString(R.string.zh_custom_background_reset, getCurrentStatusName()), Toast.LENGTH_SHORT).show();
        });

        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));
        mRefreshButton.setOnClickListener(v -> {
            refreshType(mTabLayout.getSelectedTabPosition());
            mFileRecyclerView.listFileAt(backgroundPath());
        });
    }

    private void initBackgroundMap() {
        Properties properties = BackgroundManager.getProperties();
        backgroundMap.put(BackgroundType.MAIN_MENU, (String) properties.get(BackgroundType.MAIN_MENU.name()));
        backgroundMap.put(BackgroundType.SETTINGS, (String) properties.get(BackgroundType.SETTINGS.name()));
        backgroundMap.put(BackgroundType.CUSTOM_CONTROLS, (String) properties.get(BackgroundType.CUSTOM_CONTROLS.name()));
        backgroundMap.put(BackgroundType.IN_GAME, (String) properties.get(BackgroundType.IN_GAME.name()));
    }

    private File backgroundPath() {
        if (!DIR_BACKGROUND.exists()) DIR_BACKGROUND.mkdirs();
        return DIR_BACKGROUND;
    }

    private String getCurrentStatusName() {
        switch (this.backgroundType) {
            case MAIN_MENU:
                return getString(R.string.zh_custom_background_main_menu);
            case SETTINGS:
                return getString(R.string.zh_custom_background_settings);
            case CUSTOM_CONTROLS:
                return getString(R.string.zh_custom_background_controls);
            case IN_GAME:
                return getString(R.string.zh_custom_background_in_game);
            default:
                return getString(R.string.zh_unknown);
        }
    }

    private void refreshType(int index) {
        switch (index) {
            case 1:
                this.backgroundType = BackgroundType.SETTINGS;
                break;
            case 2:
                this.backgroundType = BackgroundType.CUSTOM_CONTROLS;
                break;
            case 3:
                this.backgroundType = BackgroundType.IN_GAME;
                break;
            case 0:
            default:
                this.backgroundType = BackgroundType.MAIN_MENU;
                break;
        }
    }

    private void bindViews(@NonNull View view) {
        mTabLayout = view.findViewById(R.id.zh_custom_background_tab);

        mReturnButton = view.findViewById(R.id.zh_custom_background_return_button);
        mAddFileButton = view.findViewById(R.id.zh_custom_background_add_file_button);
        mResetButton = view.findViewById(R.id.zh_custom_background_reset_button);
        mRefreshButton = view.findViewById(R.id.zh_custom_background_refresh_button);
        mFileRecyclerView = view.findViewById(R.id.zh_custom_background);

        mFileRecyclerView.setFileIcon(FileIcon.FILE);
    }

    private void bindTabs() {
        TabLayout.Tab mainMenu = mTabLayout.newTab();
        TabLayout.Tab settings = mTabLayout.newTab();
        TabLayout.Tab controls = mTabLayout.newTab();
        TabLayout.Tab inGame = mTabLayout.newTab();

        mainMenu.setText(getResources().getText(R.string.zh_custom_background_main_menu));
        settings.setText(getResources().getText(R.string.zh_custom_background_settings));
        controls.setText(getResources().getText(R.string.zh_custom_background_controls));
        inGame.setText(getResources().getText(R.string.zh_custom_background_in_game));

        mTabLayout.addTab(mainMenu);
        mTabLayout.addTab(settings);
        mTabLayout.addTab(controls);
        mTabLayout.addTab(inGame);

        mTabLayout.selectTab(mainMenu);
    }
}
