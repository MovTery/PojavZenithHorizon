package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.pojavzh.event.EventDispatcher;
import com.movtery.pojavzh.event.EventType;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.greenrobot.eventbus.EventBus;

public class PojavProfile {
	private static final String PROFILE_PREF = "pojav_profile";
	private static final String PROFILE_PREF_FILE = "file";

	public static SharedPreferences getPrefs(Context ctx) {
		return ctx.getSharedPreferences(PROFILE_PREF, Context.MODE_PRIVATE);
	}

    public static MinecraftAccount getCurrentProfileContent(@NonNull Context ctx, @Nullable String profileName) {
        return MinecraftAccount.load(profileName == null ? getCurrentProfileName(ctx) : profileName);
    }

    public static String getCurrentProfileName(Context ctx) {
        String name = getPrefs(ctx).getString(PROFILE_PREF_FILE, "");
        // A dirty fix
        if (name.startsWith(PathAndUrlManager.DIR_ACCOUNT_NEW) && name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5).replace(PathAndUrlManager.DIR_ACCOUNT_NEW, "").replace(".json", "");
            setCurrentProfile(ctx, name);
        }
        return name;
    }

	public static void setCurrentProfile(@NonNull Context ctx, @Nullable Object obj) {
		SharedPreferences.Editor pref = getPrefs(ctx).edit();

		try {
			if (obj instanceof String) {
				String acc = (String) obj;
				pref.putString(PROFILE_PREF_FILE, acc);
				//MinecraftAccount.clearTempAccount();
			} else if (obj == null) {
				pref.putString(PROFILE_PREF_FILE, "");
			} else {
				throw new IllegalArgumentException("Profile must be String.class or null");
			}
		} finally {
			pref.apply();
			EventBus.getDefault().post(new EventDispatcher(EventType.ACCOUNT_UPDATE));
		}
	}
}
