package net.kdt.pojavlaunch.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.movtery.pojavzh.feature.log.Logging;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.utils.NotificationUtils;

/**
 * Lazy service which allows the process not to get killed.
 * Can be created from context, can be killed statically
 */
public class ProgressService extends Service implements TaskCountListener {

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * Simple wrapper to start the service
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, ProgressService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        Tools.buildNotificationChannel(getApplicationContext());
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        Intent killIntent = new Intent(getApplicationContext(), ProgressService.class);
        killIntent.putExtra("kill", true);
        PendingIntent pendingKillIntent = PendingIntent.getService(this, NotificationUtils.PENDINGINTENT_CODE_KILL_PROGRESS_SERVICE
                , killIntent, PendingIntent.FLAG_IMMUTABLE);
        mNotificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(getString(R.string.lazy_service_default_title))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notification_terminate), pendingKillIntent)
                .setSmallIcon(R.drawable.ic_pojav_full)
                .setNotificationSilent();
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("kill", false)) {
                stopSelf(); // otherwise Android tries to restart the service since it "crashed"
                Process.killProcess(Process.myPid());
                return START_NOT_STICKY;
            }
        }
        Logging.d("ProgressService", "Started!");
        mNotificationBuilder.setContentText(getString(R.string.progresslayout_tasks_in_progress, ProgressKeeper.getTaskCount()));
        startForeground(NotificationUtils.NOTIFICATION_ID_PROGRESS_SERVICE, mNotificationBuilder.build());
        if (ProgressKeeper.getTaskCount() < 1) stopSelf();
        else ProgressKeeper.addTaskCountListener(this, false);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ProgressKeeper.removeTaskCountListener(this);
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        Tools.MAIN_HANDLER.post(() -> {
            if (taskCount > 0) {
                mNotificationBuilder.setContentText(getString(R.string.progresslayout_tasks_in_progress, taskCount));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                notificationManagerCompat.notify(1, mNotificationBuilder.build());
            } else {
                stopSelf();
            }
        });
    }
}
