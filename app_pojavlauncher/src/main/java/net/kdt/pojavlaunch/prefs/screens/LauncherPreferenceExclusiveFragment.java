package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION_RATE;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        // 获取值
        int rate = PREF_ANIMATION_RATE;

        CustomSeekBarPreference seek = requirePreference("animationRate", CustomSeekBarPreference.class);
        seek.setMin(0);
        seek.setMax(1000);
        seek.setValue(rate);
        seek.setSuffix(" ms");

        seek.setOnPreferenceChangeListener((preference, newValue) -> {
            Animation cutInto = AnimationUtils.loadAnimation(requireContext(), R.anim.cut_into);
            Animation cutOut = AnimationUtils.loadAnimation(requireContext(), R.anim.cut_out);

            cutInto.setDuration((Integer) newValue);
            cutOut.setDuration((Integer) newValue);
            return true;
        });
    }
}
