package re.notifica.demo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.app.DefaultIntentReceiver;
import re.notifica.model.NotificareDevice;
import re.notifica.model.NotificareNotification;
import re.notifica.model.NotificareRemoteMessage;
import re.notifica.model.NotificareSystemNotification;


public class AppReceiver extends DefaultIntentReceiver {

    private static final String TAG = AppReceiver.class.getSimpleName();

    @Override
    public void onNotificationReceived(NotificareRemoteMessage message) {
        super.onNotificationReceived(message);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
            Log.i(TAG, "unread count = " + badgeCount);
            ShortcutBadger.applyCount(Notificare.shared().getApplicationContext(), badgeCount);
        }, 1000);
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
        // Check if notifications are enabled, by default they are not.
        // Make sure to call enableNotifications() after on-boarding (or from your app settings view)
        if (Notificare.shared().isNotificationsEnabled()) {
            Notificare.shared().enableNotifications();
        }
        // Check if location updates are enabled, by default they are not.
        // Make sure to call enableLocationUpdates() after on-boarding (or from your app settings view)
        if (Notificare.shared().isLocationUpdatesEnabled()) {
            Notificare.shared().enableLocationUpdates();
            if (BuildConfig.ENABLE_BEACONS) {
                Notificare.shared().enableBeacons(30000);
            }
        }
        // Enable in-app billing
        if (BuildConfig.ENABLE_BILLING) {
            Notificare.shared().enableBilling();
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
    public void onDeviceRegistered(NotificareDevice device) {
        // Informational only, no need to take any action
        Log.i(TAG, "Registered device " + device);
    }

    @Override
    public void onActionReceived(Uri target) {
        Log.d(TAG, "Custom action was received: " + target.toString());
        // By default, pass the target as data URI to your main activity in a launch intent
        super.onActionReceived(target);
    }

    @Override
    public void onSystemNotificationReceived(NotificareSystemNotification notification) {
        Log.i(TAG, "system notification received with type " + notification.getType());
        if (notification.getExtra() != null) {
            for (String key : notification.getExtra().keySet()) {
                Log.i(TAG, key + ": " + notification.getExtra().get(key));
            }
        }
    }
}
