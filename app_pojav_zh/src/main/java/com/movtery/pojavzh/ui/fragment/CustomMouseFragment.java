package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.ZHTools.DIR_CUSTOM_MOUSE;
import static com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground;
import static com.movtery.pojavzh.utils.image.ImageUtils.isImage;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.dialog.FilesDialog;
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon;
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean;
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomMouseFragment extends Fragment {
    public static final String TAG = "CustomMouseFragment";
    private final List<FileItemBean> mData = new ArrayList<>();
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private ImageButton mReturnButton, mAddFileButton, mRefreshButton;
    private ImageView mMouseView;
    private FileRecyclerViewCreator fileRecyclerViewCreator;

    public CustomMouseFragment() {
        super(R.layout.fragment_custom_mouse);
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
                            copyFileInBackground(requireContext(), result, mousePath().getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                loadData();
                            });
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        loadData();

        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        mRefreshButton.setOnClickListener(v -> loadData());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadData() {
        List<FileItemBean> fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(requireContext(), mousePath(), FileIcon.IMAGE, true, false);
        fileItemBeans.add(0, new FileItemBean(requireContext().getDrawable(R.drawable.ic_mouse_pointer), null, getString(R.string.zh_custom_mouse_default)));
        runOnUiThread(() -> {
            fileRecyclerViewCreator.loadData(fileItemBeans);
            //默认显示当前选中的鼠标
            refreshIcon();
        });
    }

    private File mousePath() {
        File path = new File(DIR_CUSTOM_MOUSE);
        if (!path.exists()) FileTools.mkdirs(path);
        return path;
    }

    private void refreshIcon() {
        PojavApplication.sExecutorService.execute(() -> runOnUiThread(() -> mMouseView.setImageDrawable(ZHTools.customMouse(requireContext()))));
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_return_button);
        mAddFileButton = view.findViewById(R.id.zh_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_refresh_button);

        mAddFileButton.setContentDescription(getString(R.string.zh_custom_mouse_add));
        view.findViewById(R.id.zh_search_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_paste_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_create_folder_button).setVisibility(View.GONE);

        mMouseView = view.findViewById(R.id.zh_custom_mouse_icon);

        ZHTools.setTooltipText(mReturnButton, mReturnButton.getContentDescription());
        ZHTools.setTooltipText(mAddFileButton, mAddFileButton.getContentDescription());
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton.getContentDescription());

        RecyclerView mMouseListView = view.findViewById(R.id.zh_custom_mouse);
        fileRecyclerViewCreator = new FileRecyclerViewCreator(requireContext(), mMouseListView, (position, fileItemBean) -> {
            File file = fileItemBean.getFile();
            String fileName = file == null ? null : file.getName();
            boolean isDefaultMouse = position == 0;

            FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
            filesButton.setButtonVisibility(false, false,
                    !isDefaultMouse, !isDefaultMouse, !isDefaultMouse,
                    (isDefaultMouse || isImage(file))); //默认虚拟鼠标不支持分享、重命名、删除操作

            //如果选中的虚拟鼠标是默认的虚拟鼠标，那么将加上额外的提醒
            String message = getString(R.string.zh_file_message);
            if (isDefaultMouse)
                message += "\n" + getString(R.string.zh_custom_mouse_message_default);

            filesButton.setMessageText(message);
            filesButton.setMoreButtonText(getString(R.string.global_select));

            FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, this::loadData, file);
            filesDialog.setMoreButtonClick(() -> {
                DEFAULT_PREF.edit().putString("custom_mouse", fileName).apply();
                refreshIcon();
                Toast.makeText(requireContext(),
                        StringUtils.insertSpace(getString(R.string.zh_custom_mouse_added), (fileName == null ? getString(R.string.zh_custom_mouse_default) : fileName)),
                        Toast.LENGTH_SHORT).show();
                filesDialog.dismiss();
            });
            filesDialog.show();
        }, null, mData);
    }
}
