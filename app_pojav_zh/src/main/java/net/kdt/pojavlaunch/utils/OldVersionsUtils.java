package net.kdt.pojavlaunch.utils;

import com.movtery.pojavzh.event.sticky.LIBGLESValueEvent;
import com.movtery.pojavzh.feature.log.Logging;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.util.Date;

/** Class here to help with various stuff to help run lower versions smoothly */
public class OldVersionsUtils {
    /** Lower minecraft versions fare better with opengl 1
     * @param version The version about to be launched
     */
    public static void selectOpenGlVersion(JMinecraftVersionList.Version version){
        // 1309989600 is 2011-07-07  2011-07-07T22:00:00+00:00
        String creationTime = version.time;
        if(!Tools.isValidString(creationTime)){
            EventBus.getDefault().postSticky(new LIBGLESValueEvent("2"));
            return;
        }

        try {
           Date creationDate = DateUtils.parseReleaseDate(creationTime);
            if(creationDate == null) {
                Logging.e("GL_SELECT", "Failed to parse version date");
                EventBus.getDefault().postSticky(new LIBGLESValueEvent("2"));
                return;
            }
            String openGlVersion =  DateUtils.dateBefore(creationDate, 2011, 6, 8) ? "1" : "2";
            Logging.i("GL_SELECT", openGlVersion);
            EventBus.getDefault().postSticky(new LIBGLESValueEvent(openGlVersion));
        }catch (ParseException exception){
            Logging.e("GL_SELECT", exception.toString());
            EventBus.getDefault().postSticky(new LIBGLESValueEvent("2"));
        }
    }
}
