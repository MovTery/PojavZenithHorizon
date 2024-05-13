package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.DIR_CUSTOM_MOUSE;
import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.PojavZHTools.isImage;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.SpacesItemDecoration;
import com.movtery.custommouse.MouseItemBean;
import com.movtery.custommouse.MouseListAdapter;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomMouseFragment extends Fragment {
    public static final String TAG = "CustomMouseFragment";
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mRefreshButton;
    private ImageButton mHelpButton;
    private ImageView mMouseView;
    private final List<MouseItemBean> mData = new ArrayList<>();
    private RecyclerView mMouseListView;
    private MouseListAdapter mMouseListAdapter;

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
                                refreshAdapter();
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mMouseListView.setLayoutManager(layoutManager);
        mMouseListView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, 12));
        mMouseListAdapter = new MouseListAdapter(mData);
        mMouseListView.setAdapter(mMouseListAdapter);

        mMouseListAdapter.setOnItemClickListener(file -> {
            String fileName = file.getName();
            boolean isDefaultMouse = fileName.equals("default_mouse.png");

            FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
            filesButton.setButtonVisibility(!isDefaultMouse, !isDefaultMouse, !isDefaultMouse, isImage(file)); //默认虚拟鼠标不支持分享、重命名、删除操作

            //如果选中的虚拟鼠标是默认的虚拟鼠标，那么将加上额外的提醒
            String message = getString(R.string.zh_file_message);
            if (isDefaultMouse)
                message += "\n" + getString(R.string.zh_custom_mouse_message_default);

            filesButton.messageText = message;
            filesButton.moreButtonText = getString(R.string.global_select);

            FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, this::refreshAdapter, file);
            filesDialog.setMoreButtonClick(v -> {
                DEFAULT_PREF.edit().putString("custom_mouse", fileName).apply();
                refreshIcon();
                Toast.makeText(requireContext(), getString(R.string.zh_custom_mouse_added) + fileName, Toast.LENGTH_SHORT).show();
                filesDialog.dismiss();
            });
            filesDialog.show();
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        mRefreshButton.setOnClickListener(v -> refreshAdapter());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mouse_title));
            builder.setMessage(getString(R.string.zh_help_mouse_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private void loadData() {
        if (!mData.isEmpty()) mData.clear();

        File[] files = mousePath().listFiles();
        if (files != null) {
            for (File file : files) {
                if (isImage(file)) {
                    MouseItemBean mouseItemBean = new MouseItemBean();
                    mouseItemBean.name = file.getName();
                    mouseItemBean.mouseIcon = Drawable.createFromPath(file.getAbsolutePath());
                    mData.add(mouseItemBean);
                }
            }
        }

        //默认显示当前选中的鼠标
        refreshIcon();
    }

    private File mousePath() {
        File path = new File(DIR_CUSTOM_MOUSE);
        if (!path.exists()) path.mkdirs();
        return path;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        runOnUiThread(() -> {
            loadData();
            mMouseListAdapter.notifyDataSetChanged();
        });
    }

    private void refreshIcon() {
        PojavApplication.sExecutorService.execute(() -> runOnUiThread(() -> mMouseView.setImageDrawable(PojavZHTools.customMouse(requireContext()))));
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_custom_mouse_return_button);
        mAddFileButton = view.findViewById(R.id.zh_custom_mouse_add_button);
        mRefreshButton = view.findViewById(R.id.zh_custom_mouse_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_custom_mouse_help_button);
        mMouseListView = view.findViewById(R.id.zh_custom_mouse);
        mMouseView = view.findViewById(R.id.zh_custom_mouse_icon);
    }
}
