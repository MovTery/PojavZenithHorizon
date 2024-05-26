package net.kdt.pojavlaunch;

import static com.movtery.utils.PojavZHTools.getLatestFile;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.movtery.ui.dialog.ExitDialog;
import com.movtery.utils.PojavZHTools;

import java.io.File;

@Keep
public class ExitActivity extends AppCompatActivity {

    @SuppressLint("StringFormatInvalid") //invalid on some translations but valid on most, cant fix that atm
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int code = -1;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            code = extras.getInt("code",-1);
        }

        File crashReportFile = getLatestFile(new File(PojavZHTools.getGameDirPath(getCurrentProfile().gameDir), "crash-reports"), 2);

        ExitDialog exitDialog = new ExitDialog(this, code, crashReportFile, new File(Tools.DIR_GAME_HOME, "latestlog.txt"));
        exitDialog.setOnDismissListener(dialog -> ExitActivity.this.finish());
        exitDialog.show();
    }

    public static void showExitMessage(Context ctx, int code) {
        Intent i = new Intent(ctx,ExitActivity.class);
        i.putExtra("code",code);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

}
