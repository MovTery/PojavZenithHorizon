package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground;
import static net.kdt.pojavlaunch.CustomControlsActivity.BUNDLE_CONTROL_PATH;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.ui.dialog.EditControlInfoDialog;
import com.movtery.pojavzh.ui.dialog.FilesDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlsListViewCreator;
import com.movtery.pojavzh.ui.subassembly.customcontrols.EditControlData;
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener;
import com.movtery.pojavzh.ui.subassembly.view.SearchView;
import com.movtery.pojavzh.utils.anim.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.file.PasteFile;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControlButtonFragment extends FragmentWithAnim {
    public static final String TAG = "ControlButtonFragment";
    public static final String BUNDLE_SELECT_CONTROL = "bundle_select_control";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private View mControlLayout, mOperateLayout, mOperateView;
    private ImageButton mReturnButton, mAddControlButton, mImportControlButton, mPasteButton, mSearchSummonButton, mRefreshButton;
    private TextView mNothingTip;
    private SearchView mSearchView;
    private ControlsListViewCreator controlsListViewCreator;
    private boolean mSelectControl = false;

    public ControlButtonFragment() {
        super(R.layout.fragment_control_manager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("json"),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, new File(Tools.CTRLMAP_PATH).getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                controlsListViewCreator.refresh();
                            });
                        });
                    }
                }
        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        controlsListViewCreator.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                if (mSelectControl) {
                    ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path));
                    Tools.removeCurrentFragment(requireActivity());
                } else {
                    showDialog(file);
                }
            }

            @Override
            public void onItemLongClick(File file, String path) {
                new TipDialog.Builder(requireContext())
                        .setTitle(R.string.default_control)
                        .setMessage(R.string.zh_controls_set_default_message)
                        .setConfirmClickListener(() -> {
                            String absolutePath = file.getAbsolutePath();
                            LauncherPreferences.DEFAULT_PREF.edit().putString("defaultCtrl", absolutePath).apply();
                            LauncherPreferences.PREF_DEFAULTCTRL_PATH = absolutePath;
                        }).buildDialog();
            }
        });

        controlsListViewCreator.setRefreshListener(() -> {
            int itemCount = controlsListViewCreator.getItemCount();
            boolean show = itemCount == 0;
            AnimUtils.setVisibilityAnim(mNothingTip, show);
        });

        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mPasteButton.setOnClickListener(v -> PasteFile.getInstance().pasteFiles(requireActivity(), new File(Tools.CTRLMAP_PATH), null, () -> runOnUiThread(() -> {
            mPasteButton.setVisibility(View.GONE);
            controlsListViewCreator.refresh();
        })));
        mImportControlButton.setOnClickListener(v -> {
            String suffix = ".json";
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        }); //限制.json文件
        mAddControlButton.setOnClickListener(v -> {
            EditControlInfoDialog editControlInfoDialog = new EditControlInfoDialog(requireContext(), true, null, new ControlInfoData());
            editControlInfoDialog.setTitle(getString(R.string.zh_controls_create_new));
            editControlInfoDialog.setOnConfirmClickListener((fileName, controlInfoData) -> {
                File file = new File(new File(Tools.CTRLMAP_PATH).getAbsolutePath(), fileName + ".json");

                if (file.exists()) { //检查文件是否已经存在
                    editControlInfoDialog.getFileNameEditBox().setError(getString(R.string.zh_file_rename_exitis));
                    return;
                }

                //创建布局文件
                EditControlData.createNewControlFile(requireContext(), file, controlInfoData);

                controlsListViewCreator.refresh();

                editControlInfoDialog.dismiss();
            });
            editControlInfoDialog.show();
        });
        mSearchSummonButton.setOnClickListener(v -> mSearchView.setVisibility());
        mRefreshButton.setOnClickListener(v -> controlsListViewCreator.refresh());

        controlsListViewCreator.listAtPath();

        ViewAnimUtils.slideInAnim(this);
    }

    private String removeLockPath(String path) {
        return path.replace(Tools.CTRLMAP_PATH, ".");
    }

    private void showDialog(File file) {
        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, true);

        if (file.isDirectory()) {
            filesButton.setMessageText(getString(R.string.zh_file_folder_message));
        } else {
            filesButton.setMessageText(getString(R.string.zh_file_message));
        }
        filesButton.setMoreButtonText(getString(R.string.global_load));

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> controlsListViewCreator.refresh()), file);

        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));

        filesDialog.setMoreButtonClick(() -> {
            Intent intent = new Intent(requireContext(), CustomControlsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_CONTROL_PATH, file.getAbsolutePath());
            intent.putExtras(bundle);

            startActivity(intent);
            filesDialog.dismiss();
        }); //加载
        filesDialog.show();
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mSelectControl = bundle.getBoolean(BUNDLE_SELECT_CONTROL, mSelectControl);
    }

    private void bindViews(@NonNull View view) {
        mControlLayout = view.findViewById(R.id.control_layout);
        mOperateLayout = view.findViewById(R.id.operate_layout);

        mOperateView = view.findViewById(R.id.operate_view);

        mReturnButton = view.findViewById(R.id.zh_return_button);
        mImportControlButton = view.findViewById(R.id.zh_add_file_button);
        mAddControlButton = view.findViewById(R.id.zh_create_folder_button);
        mPasteButton = view.findViewById(R.id.zh_paste_button);
        mRefreshButton = view.findViewById(R.id.zh_refresh_button);
        mSearchSummonButton = view.findViewById(R.id.zh_search_button);
        mNothingTip = view.findViewById(R.id.zh_controls_nothing);

        mImportControlButton.setContentDescription(getString(R.string.zh_controls_import_control));
        mAddControlButton.setContentDescription(getString(R.string.zh_controls_create_new));

        controlsListViewCreator = new ControlsListViewCreator(requireContext(), view.findViewById(R.id.zh_controls_list));

        mSearchView = new SearchView(view, view.findViewById(R.id.zh_search_view));
        mSearchView.setAsynchronousUpdatesListener(controlsListViewCreator::searchControls);
        mSearchView.setShowSearchResultsListener(controlsListViewCreator::setShowSearchResultsOnly);

        mPasteButton.setVisibility(PasteFile.getInstance().getPasteType() != null ? View.VISIBLE : View.GONE);

        ZHTools.setTooltipText(mReturnButton, mReturnButton.getContentDescription());
        ZHTools.setTooltipText(mImportControlButton, mImportControlButton.getContentDescription());
        ZHTools.setTooltipText(mAddControlButton, mAddControlButton.getContentDescription());
        ZHTools.setTooltipText(mPasteButton, mPasteButton.getContentDescription());
        ZHTools.setTooltipText(mSearchSummonButton, mSearchSummonButton.getContentDescription());
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton.getContentDescription());
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mControlLayout, Techniques.BounceInDown));
        yoYos.add(ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.BounceInLeft));

        yoYos.add(ViewAnimUtils.setViewAnim(mOperateView, Techniques.FadeInLeft));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mControlLayout, Techniques.FadeOutUp));
        yoYos.add(ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.FadeOutRight));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }
}

