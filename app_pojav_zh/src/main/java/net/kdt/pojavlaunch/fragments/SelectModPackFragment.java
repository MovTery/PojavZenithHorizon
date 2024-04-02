package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class SelectModPackFragment extends Fragment {
    public static final String TAG = "SelectModPackFragment";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private File modPackFile;

    public SelectModPackFragment(){
        super(R.layout.fragment_select_modpack);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension(null),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                        //使用AsyncTask在后台线程中执行文件复制
                        new CopyFile().execute(result);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.zh_modpack_button_search_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, null);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.zh_modpack_button_local_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Toast.makeText(requireActivity(), getString(R.string.zh_select_modpack_local_tip), Toast.LENGTH_SHORT).show();
                openDocumentLauncher.launch(null);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.zh_modpack_help_button).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_modpack_title));
            builder.setMessage(getString(R.string.zh_help_modpack_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            modPackFile = copyFileInBackground(requireContext(), uris, Tools.DIR_CACHE.getAbsolutePath());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PojavZHTools.DIR_GAME_MODPACK = modPackFile.getAbsolutePath();
            ExtraCore.setValue(ExtraConstants.INSTALL_LOCAL_MODPACK, true);
        }
    }
}
