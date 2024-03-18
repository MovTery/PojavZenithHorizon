package net.kdt.pojavlaunch;

import android.content.*;
import android.content.pm.ActivityInfo;
import android.os.*;
import androidx.appcompat.app.*;
import net.kdt.pojavlaunch.utils.*;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_FORCE_LANDSCAPE;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_IGNORE_NOTCH;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PREF_FORCE_LANDSCAPE) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        LocaleUtils.setLocale(this);
        Tools.setFullscreen(this, setFullscreen());
        Tools.updateWindowSize(this);
    }

    /** @return Whether the activity should be set as a fullscreen one */
    public boolean setFullscreen(){
        return true;
    }


    @Override
    public void startActivity(Intent i) {
        super.startActivity(i);
        //new Throwable("StartActivity").printStackTrace();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Tools.checkStorageRoot(this)) {
            startActivity(new Intent(this, MissingStorageActivity.class));
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Tools.setFullscreen(this, setFullscreen());
        Tools.ignoreNotch(PREF_IGNORE_NOTCH,this);
    }
}
