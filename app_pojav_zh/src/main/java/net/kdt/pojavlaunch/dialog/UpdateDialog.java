package net.kdt.pojavlaunch.dialog;


import static net.kdt.pojavlaunch.PojavZHTools.markdownToHtml;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.UpdateLauncher;

public class UpdateDialog extends Dialog {
    private final String versionName;
    private final String tagName;
    private final String createdTime;
    private final String fileSize;
    private final String description;

    public UpdateDialog(@NonNull Context context, UpdateInformation updateInformation) {
        super(context);
        this.versionName = updateInformation.versionName;
        this.tagName = updateInformation.tagName;
        this.createdTime = updateInformation.createdTime;
        this.fileSize = updateInformation.fileSize;
        this.description = updateInformation.description;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_update);
        init();
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        TextView mVersionName = findViewById(R.id.zh_update_version_name);
        TextView mCreatedTime = findViewById(R.id.zh_update_time);
        TextView mFileSize = findViewById(R.id.zh_update_file_size);
        WebView mDescription = findViewById(R.id.zh_update_description);

        String version = getContext().getString(R.string.zh_update_dialog_version) + this.versionName;
        String time = getContext().getString(R.string.zh_update_dialog_time) + this.createdTime;
        String size = getContext().getString(R.string.zh_update_dialog_file_size) + this.fileSize;

        mVersionName.setText(version);
        mCreatedTime.setText(time);
        mFileSize.setText(size);

        String descriptionHtml = markdownToHtml(this.description);

        mDescription.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String[] color = new String[2];
                boolean darkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
                color[0] = darkMode ? "#333333" : "#CFCFCF";
                color[1] = darkMode ? "#ffffff" : "#0E0E0E";

                String css = "body { background-color: " + color[0] + "; color: " + color[1] + "; }" +
                        "a, a:link, a:visited, a:hover, a:active {" +
                        "  color: " + color[1] + ";" +
                        "  text-decoration: none;" +
                        "  pointer-events: none;" + //禁止链接的交互性
                        "}";

                //JavaScript代码，用于将CSS样式添加到WebView中
                String js = "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "if (style.styleSheet){" +
                        "  style.styleSheet.cssText = '" + css.replace("'", "\\'") + "';" +
                        "} else {" +
                        "  style.appendChild(document.createTextNode('" + css.replace("'", "\\'") + "'));" +
                        "}" +
                        "parent.appendChild(style);";

                mDescription.evaluateJavascript(js, null);
            }
        });

        mDescription.getSettings().setJavaScriptEnabled(true);
        mDescription.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        Button mUpdateButton = findViewById(R.id.zh_update_update_button);
        Button mCancelButton = findViewById(R.id.zh_update_cancel_button);
        Button mIgnoreButton = findViewById(R.id.zh_update_ignore_button);

        mUpdateButton.setOnClickListener(view -> {
            this.dismiss();
            UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), tagName, fileSize);
            updateLauncher.start();
        });
        mCancelButton.setOnClickListener(view -> this.dismiss());
        mIgnoreButton.setOnClickListener(view -> {
            DEFAULT_PREF.edit().putString("ignoreUpdate", this.versionName).apply();
            this.dismiss();
        });
    }

    public static class UpdateInformation {
        public String versionName;
        public String tagName;
        public String createdTime;
        public String fileSize;
        public String description;
        public void information(@NonNull String versionName, @NonNull String tagName, @NonNull String createdTime, @NonNull String fileSize, @NonNull String description) {
            this.versionName = versionName;
            this.tagName = tagName;
            this.createdTime = createdTime;
            this.fileSize = fileSize;
            this.description = description;
        }
    }
}
