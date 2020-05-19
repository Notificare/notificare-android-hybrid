package re.notifica.demo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;

//import org.altbeacon.beacon.logging.LogManager;
//import org.altbeacon.beacon.logging.Loggers;
import org.json.JSONException;
import org.json.JSONObject;

import re.notifica.Notificare;
import re.notifica.model.NotificareTimeOfDayRange;
import re.notifica.support.NotificareSupport;


public class AppBaseApplication extends Application {

    private static final String TAG = AppBaseApplication.class.getSimpleName();
    private static final String PREFS_NAME = "hybrid_preferences";
    private static final String PREF_KEY_ONBOARDING_STATUS = "onboarding_status";
    private static final String PREF_KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String PREF_KEY_LOCATION_ENABLED = "location_enabled";
    private static final String PREF_KEY_DND_ENABLED = "dnd_enabled";
    private static final String PREF_KEY_DND_RANGE = "dnd_range";
    private static final String PREF_KEY_CONFIG = "config";
    private static final String PREF_KEY_CUSTOMJS = "custom_js";
    private static final String PREF_KEY_MEMBERCARD_SERIAL = "member_card_serial";
    private static final String PREF_KEY_MEMBERCARD_TEMPLATE = "member_card_template";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // Allow debug logging only if build is debug
        Notificare.shared().setDebugLogging(BuildConfig.DEBUG);
        if (BuildConfig.ENABLE_STRICT_MODE && BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.enableDefaults();
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build());
        }

        // Enable beacons debugging
        if (BuildConfig.ENABLE_BEACONS_DEBUG) {
            Log.i(TAG, "enabling debugging for beacons");
            //LogManager.setLogger(Loggers.verboseLogger());
        }

        // REQUIRED
        // Launch Notificare
        Notificare.shared().launch(this);

        // REQUIRED
        // Create a default notification channel
        Notificare.shared().createDefaultChannel();

        // OPTIONAL
        // Add a separate notification channel for passbook relevance notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel passbookChannel = new NotificationChannel("passbook", "Passbook Channel", NotificationManager.IMPORTANCE_DEFAULT);
                passbookChannel.setDescription("This is for passbook notifications");
                notificationManager.createNotificationChannel(passbookChannel);
                Notificare.shared().setPassbookChannel(passbookChannel.getId());

                NotificationChannel importantChannel = new NotificationChannel("important", "Important Messages", NotificationManager.IMPORTANCE_HIGH);
                importantChannel.setDescription("This is for important notifications");
                notificationManager.createNotificationChannel(importantChannel);
            }
        }

        // REQUIRED
        Notificare.shared().setIntentReceiver(AppReceiver.class);
        //Notificare.shared().setUserPreferencesResource(R.xml.preferences);

        // RECOMMENDED
        Notificare.shared().setSmallIcon(R.drawable.ic_stat_notify_msg);

        // Orientation change in NotificationActivity is disallowed by default
        Notificare.shared().setAllowOrientationChange(true);

        // OPTIONAL, false by default
        Notificare.shared().setPassbookRelevanceOngoing(true);
        Notificare.shared().setPassbookLegacyMode(false);

        // Crash logs are enabled by default.
        // However, you opt-out by using the following instruction
        //Notificare.shared().setCrashLogs(false);

        // The SDK supports a single optional placeholder, %s.
        // If the placeholder is provided, it will be replaced by the pass' description, if any.
        //Notificare.shared().setRelevanceText("Notificare demo: %s");
        //Notificare.shared().setRelevanceIcon(R.drawable.notificare_passbook_style);


        // This app uses NotificareSupport lib, initialize it
        NotificareSupport.shared().launch(this);

        NotificareSupport.shared().getTypefaceCache().putFromAssets("ProximaNovaRegular", "fonts/ProximaNovaRegular.otf");
        NotificareSupport.shared().getTypefaceCache().putFromAssets("ProximaNovaBold", "fonts/ProximaNovaBold.otf");
        NotificareSupport.shared().getTypefaceCache().putFromAssets("ProximaNovaThin", "fonts/ProximaNovaThin.otf");

        // Internet connection configs
        NotificareSupport.shared().setInternetConnectionActivityTheme(R.style.AppTheme);
        NotificareSupport.shared().setInternetConnectionActivityLayout(R.layout.activity_no_internet);

        registerActivityLifecycleCallbacks(NotificareSupport.shared());
    }

    public static Context getAppContext() {
        return Notificare.shared().getApplicationContext();
    }

    public static boolean getOnboardingStatus() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_ONBOARDING_STATUS, false);
    }

    public static void setOnboardingStatus(boolean status) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_ONBOARDING_STATUS, status);
        editor.apply();
    }

    public static boolean getDndEnabled() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_DND_ENABLED, false);
    }

    public static void setDndEnabled(boolean enabled) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PREF_KEY_DND_ENABLED, enabled);
        editor.apply();
    }

    public static NotificareTimeOfDayRange getDndRange() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String dndString = sharedPreferences.getString(PREF_KEY_DND_RANGE, null);
        if (dndString != null) {
            try {
                return new NotificareTimeOfDayRange(new JSONObject(dndString));
            } catch (JSONException e) {
                Log.e(TAG, "Error unboxing dnd range", e);
            }
        }

        return null;
    }

    public static void setDndRange(NotificareTimeOfDayRange notificareTimeOfDayRange) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (notificareTimeOfDayRange == null) {
            editor.putString(PREF_KEY_DND_RANGE, null);
        } else {
            try {
                editor.putString(PREF_KEY_DND_RANGE, notificareTimeOfDayRange.toJSONObject().toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error boxing dnd range", e);
            }
        }
        editor.apply();
    }

    public static void resetDnd() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(PREF_KEY_DND_ENABLED);
        editor.remove(PREF_KEY_DND_RANGE);
        editor.apply();
    }

    public static String getConfigJSONString() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_CONFIG, "");
    }

    public static void setConfigJSONString(String config) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_CONFIG, config);
        editor.apply();
    }

    public static String getCustomJSString() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_CUSTOMJS, "");
    }

    public static void setCustomJSString(String customJS) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_CUSTOMJS, customJS);
        editor.apply();
    }

    public static String getMemberCardSerial() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_MEMBERCARD_SERIAL, "");
    }

    public static void setMemberCardSerial(String serial) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_MEMBERCARD_SERIAL, serial);
        editor.apply();
    }

    public static String getMemberCardTemplate() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_MEMBERCARD_TEMPLATE, "");
    }

    public static void setMemberCardTemplate(String template) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_MEMBERCARD_TEMPLATE, template);
        editor.apply();
    }

}
