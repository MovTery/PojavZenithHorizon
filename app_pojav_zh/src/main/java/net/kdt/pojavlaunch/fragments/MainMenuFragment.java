package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.markdownToHtml;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ADVANCED_FEATURES;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.ShareLogDialog;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);
        Button mOpenInstanceDirButton = view.findViewById(R.id.zh_open_instance_dir_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mAboutButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), AboutFragment.class, AboutFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext(), new File(Tools.DIR_GAME_HOME + "/latestlog.txt"));
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, Tools.DIR_GAME_HOME);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenInstanceDirButton.setOnClickListener(v -> {
            String path = PojavZHTools.getGameDirPath(getCurrentProfile().gameDir).getAbsolutePath();
            File file = new File(path);
            if (!file.exists()) file.mkdirs(); //必须保证此路径存在

            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, path);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenMainDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
        mOpenInstanceDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);

        checkNewNotice(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    private void checkNewNotice(View view) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(PojavZHTools.URL_GITHUB_HOME + "notice.json")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    if (response.body() == null) return;
                    String responseBody = response.body().string(); //解析响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        int numbering = jsonObject.getInt("numbering");
                        if (numbering <= DEFAULT_PREF.getInt("ignoreNotice", 0)) return; //忽略此通知

                        View launcherNoticeView = view.findViewById(R.id.zh_menu_notice);
                        requireActivity().runOnUiThread(() -> launcherNoticeView.setVisibility(View.VISIBLE));

                        String language = PojavZHTools.getDefaultLanguage();
                        String rawTitle;
                        String rawSubstance;
                        if (language.equals("zh_cn")) {
                            rawTitle = jsonObject.getString("title_" + language);
                            rawSubstance = jsonObject.getString("substance_" + language);
                        } else {
                            rawTitle = jsonObject.getString("title_zh_tw");
                            rawSubstance = jsonObject.getString("substance_zh_tw");
                        }
                        String rawDate = jsonObject.getString("date");
                        String substance = markdownToHtml(rawSubstance);

                        requireActivity().runOnUiThread(() -> {
                            //初始化
                            TextView noticeTitleView = view.findViewById(R.id.zh_menu_notice_title);
                            TextView noticeDateView = view.findViewById(R.id.zh_menu_notice_date);
                            WebView noticeSubstanceWebView = view.findViewById(R.id.zh_menu_notice_substance);
                            Button noticeCloseButton = view.findViewById(R.id.zh_menu_notice_close_button);

                            if (!rawTitle.equals("null")) {
                                noticeTitleView.setText(rawTitle);
                            }

                            noticeDateView.setText(rawDate);
                            PojavZHTools.getWebViewAfterProcessing(noticeSubstanceWebView);
                            noticeSubstanceWebView.getSettings().setJavaScriptEnabled(true);
                            noticeSubstanceWebView.loadDataWithBaseURL(null, substance, "text/html", "UTF-8", null);
                            noticeCloseButton.setOnClickListener(view1 -> {
                                DEFAULT_PREF.edit().putInt("ignoreNotice", numbering).apply();
                                requireActivity().runOnUiThread(() -> launcherNoticeView.setVisibility(View.GONE));
                            });
                        });
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }
}
