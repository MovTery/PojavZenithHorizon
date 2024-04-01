package net.kdt.pojavlaunch.dialog;


import static net.kdt.pojavlaunch.PojavZHTools.markdownToHtml;
import static net.kdt.pojavlaunch.PojavZHTools.updateLauncher;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.app.Dialog;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class UpdateDialog extends Dialog {
    private final String versionName;
    private final String createdTime;
    private final String description;

    public UpdateDialog(@NonNull Context context, UpdateInformation updateInformation) {
        super(context);
        this.versionName = updateInformation.versionName;
        this.createdTime = updateInformation.createdTime;
        this.description = updateInformation.description;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_update);
        init();
    }
    private void init() {
        TextView mVersionName = findViewById(R.id.zh_update_version_name);
        TextView mCreatedTime = findViewById(R.id.zh_update_time);
        WebView mDescription = findViewById(R.id.zh_update_description);

        String version = getContext().getString(R.string.zh_update_dialog_version) + this.versionName;
        String time = getContext().getString(R.string.zh_update_dialog_time) + this.createdTime;

        mVersionName.setText(version);
        mCreatedTime.setText(time);

        String descriptionHtml = markdownToHtml(this.description);

        mDescription.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String css = "body { background-color: transparent; color: #ffffff; }";

                //JavaScript代码，用于将CSS样式添加到WebView中
                String js = "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "if (style.styleSheet){" +
                        "  style.styleSheet.cssText = '" + css + "';" +
                        "} else {" +
                        "  style.appendChild(document.createTextNode('" + css + "'));" +
                        "}" +
                        "parent.appendChild(style);";

                mDescription.evaluateJavascript(js, null);
            }
        });

        mDescription.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        Button mUpdateButton = findViewById(R.id.zh_update_update_button);
        Button mCancelButton = findViewById(R.id.zh_update_cancel_button);
        Button mIgnoreButton = findViewById(R.id.zh_update_ignore_button);

        mUpdateButton.setOnClickListener(view -> {
            this.dismiss();
            updateLauncher(getContext());
        });
        mCancelButton.setOnClickListener(view -> this.dismiss());
        mIgnoreButton.setOnClickListener(view -> {
            DEFAULT_PREF.edit().putString("ignoreUpdate", this.versionName).apply();
            this.dismiss();
        });
    }

    public static class UpdateInformation {
        public String versionName;
        public String createdTime;
        public String description;
        public void information(@NonNull String versionName, @NonNull String createdTime, @NonNull String description) {
            this.versionName = versionName;
            this.createdTime = createdTime;
            this.description = description;
        }
    }
}
