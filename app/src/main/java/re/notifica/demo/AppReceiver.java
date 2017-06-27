package re.notifica.demo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareNotification;
import re.notifica.push.gcm.DefaultIntentReceiver;


public class AppReceiver extends DefaultIntentReceiver {

    public static final String PREFS_NAME = "AppPrefsFile";
    private static final String TAG = AppReceiver.class.getSimpleName();

    @Override
    protected void generateNotification(String intentAction, int icon, @Nullable NotificareNotification notification, @Nullable String inboxItemId, Bundle extras, Bitmap picture, Bitmap largeIcon) {
        boolean canShowNotification = false;

        if (AppBaseApplication.getNotificationsEnabled()) {
            canShowNotification = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
                    ShortcutBadger.applyCount(Notificare.shared().getApplicationContext(), badgeCount);
                }
            }, 1000);
        }

        if (canShowNotification) {
            super.generateNotification(intentAction, icon, notification, inboxItemId, extras, picture, largeIcon);
        }
    }

    @Override
    public void onNotificationOpenRegistered(NotificareNotification notification, Boolean handled) {
        int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
        ShortcutBadger.applyCount(Notificare.shared().getApplicationContext(), badgeCount);
        Log.d(TAG, "Notification with type " + notification.getType() + " was opened, handled by SDK: " + handled);
    }

    @Override
    public void onUrlClicked(Uri urlClicked, Bundle extras) {
        Log.i(TAG, "URL was clicked: " + urlClicked);
        NotificareNotification notification = extras.getParcelable(Notificare.INTENT_EXTRA_NOTIFICATION);
        if (notification != null) {
            Log.i(TAG, "URL was clicked for \"" + notification.getMessage() + "\"");
        }
    }

    @Override
    public void onReady() {
        Notificare.shared().enableNotifications();
        if (BuildConfig.ENABLE_BILLING) {
            Notificare.shared().enableBilling();
        }
    }

    @Override
    public void onRegistrationFinished(String deviceId) {
        Log.d(TAG, "Device was registered with GCM as device " + deviceId);
        // Register as a device for a test userID

        Notificare.shared().registerDevice(deviceId, new NotificareCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (Notificare.shared().isLocationUpdatesEnabled()) {
                    Notificare.shared().enableLocationUpdates();
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(30000);
                    }
                }
                Notificare.shared().fetchDeviceTags(new NotificareCallback<List<String>>() {

                    @Override
                    public void onError(NotificareError arg0) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSuccess(List<String> arg0) {


                    }

                });
            }

            @Override
            public void onError(NotificareError error) {
                Log.e(TAG, "Error registering device", error);
            }

        });

    }

    @Override
    public void onActionReceived(Uri target) {
        Log.d(TAG, "Custom action was received: " + target.toString());
        // By default, pass the target as data URI to your main activity in a launch intent
        super.onActionReceived(target);
    }


}
