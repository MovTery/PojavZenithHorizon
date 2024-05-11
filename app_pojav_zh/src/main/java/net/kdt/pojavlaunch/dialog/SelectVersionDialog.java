package net.kdt.pojavlaunch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.movtery.versionlist.VersionListView;
import com.movtery.versionlist.VersionSelectedListener;
import com.movtery.versionlist.VersionType;

import net.kdt.pojavlaunch.R;

public class SelectVersionDialog extends Dialog {
    private static SelectVersionDialog selectVersionDialog;
    private VersionType versionType;

    public static SelectVersionDialog getSelectVersionDialog(@NonNull Context context) {
        if (selectVersionDialog == null) selectVersionDialog = new SelectVersionDialog(context);
        return selectVersionDialog;
    }

    private VersionListView versionListView;

    public static void open(@NonNull Context context, VersionSelectedListener versionSelectedListener) {
        getSelectVersionDialog(context).getVersionListView().setVersionSelectedListener(versionSelectedListener);
        getSelectVersionDialog(context).show();
    }

    private SelectVersionDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setContentView(R.layout.dialog_select_version);
        init();
    }

    private void init() {
        Button releaseButton = findViewById(R.id.zh_version_release);
        Button snapshotButton = findViewById(R.id.zh_version_snapshot);
        Button betaButton = findViewById(R.id.zh_version_beta);
        Button alphaButton = findViewById(R.id.zh_version_alpha);
        Button cancelButton = findViewById(R.id.zh_version_cancel);

        versionListView = findViewById(R.id.zh_version);

        versionType = VersionType.RELEASE;
        refresh();

        releaseButton.setOnClickListener(v -> {
            versionType = VersionType.RELEASE;
            refresh();
        });
        snapshotButton.setOnClickListener(v -> {
            versionType = VersionType.SNAPSHOT;
            refresh();
        });
        betaButton.setOnClickListener(v -> {
            versionType = VersionType.BETA;
            refresh();
        });
        alphaButton.setOnClickListener(v -> {
            versionType = VersionType.ALPHA;
            refresh();
        });

        cancelButton.setOnClickListener(v -> this.dismiss());

        versionListView.setVersionSelectedListener(new VersionSelectedListener() {
            @Override
            public void onVersionSelected(String version) {

            }
        });
    }

    private VersionListView getVersionListView() {
        return versionListView;
    }

    private void refresh() {
        versionListView.setVersionType(versionType);
    }
}
