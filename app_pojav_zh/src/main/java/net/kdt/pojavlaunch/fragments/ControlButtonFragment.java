package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.CustomControlsActivity.BUNDLE_CONTROL_PATH;
import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.customcontrols.ControlInfoData;
import com.movtery.customcontrols.ControlsListView;
import com.movtery.customcontrols.EditControlData;
import com.movtery.utils.PasteFile;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.EditControlInfoDialog;
import net.kdt.pojavlaunch.dialog.FilesDialog;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class ControlButtonFragment extends Fragment {
    public static final String TAG = "ControlButtonFragment";
    public static final String BUNDLE_SELECT_CONTROL = "bundle_select_control";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddControlButton, mImportControlButton, mPasteButton, mRefreshButton;
    private ImageButton mHelpButton;
    private ControlsListView controlsListView;
    private boolean mSelectControl = false;

    public ControlButtonFragment() {
        super(R.layout.fragment_control_manager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runOnUiThread(() -> {
            if (controlsListView != null) {
                controlsListView.refresh();
            }
        });
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("json"),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, new File(Tools.CTRLMAP_PATH).getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                controlsListView.refresh();
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

        controlsListView.listAtPath(controlPath());

        controlsListView.setFileSelectedListener(new FileSelectedListener() {
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
            }
        });

        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
        mPasteButton.setOnClickListener(v -> PasteFile.pasteFile(requireActivity(), new File(Tools.CTRLMAP_PATH), null, () -> runOnUiThread(() -> {
            mPasteButton.setVisibility(View.GONE);
            controlsListView.refresh();
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

                controlsListView.refresh();
            });
            editControlInfoDialog.show();
        });
        mRefreshButton.setOnClickListener(v -> controlsListView.refresh());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_control_button_title));
            builder.setMessage(getString(R.string.zh_help_control_button_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private String removeLockPath(String path) {
        return path.replace(Tools.CTRLMAP_PATH, ".");
    }

    private void showDialog(File file) {
        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, true);

        if (file.isDirectory()) {
            filesButton.messageText = getString(R.string.zh_file_folder_message);
        } else {
            filesButton.messageText = getString(R.string.zh_file_message);
        }
        filesButton.moreButtonText = getString(R.string.global_load);

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> controlsListView.refresh()), file);

        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));

        filesDialog.setMoreButtonClick(v -> {
            Intent intent = new Intent(requireContext(), CustomControlsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_CONTROL_PATH, file.getAbsolutePath());
            intent.putExtras(bundle);

            startActivity(intent);
            filesDialog.dismiss();
        }); //加载
        filesDialog.show();
    }

    private File controlPath() {
        File ctrlPath = new File(Tools.CTRLMAP_PATH);
        if (!ctrlPath.exists()) ctrlPath.mkdirs();
        return ctrlPath;
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mSelectControl = bundle.getBoolean(BUNDLE_SELECT_CONTROL, mSelectControl);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_controls_return_button);
        mImportControlButton = view.findViewById(R.id.zh_controls_import_control_button);
        mAddControlButton = view.findViewById(R.id.zh_controls_create_new_button);
        mPasteButton = view.findViewById(R.id.zh_controls_paste_button);
        mRefreshButton = view.findViewById(R.id.zh_controls_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_controls_help_button);
        controlsListView = view.findViewById(R.id.zh_controls_list);

        mPasteButton.setVisibility(PasteFile.PASTE_TYPE != null ? View.VISIBLE : View.GONE);
    }
}

