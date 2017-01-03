package re.notifica.hybrid;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import re.notifica.Notificare;
import re.notifica.model.NotificareTimeOfDayRange;
import re.notifica.support.NotificareSupport;


public class AppBaseApplication extends Application {

    private static final String TAG = AppBaseApplication.class.getSimpleName();
    private static final String PREFS_NAME = "hybrid_preferences";
    private static final String PREF_KEY_ONBOARDING_STATUS = "onboarding_status";
    private static final String PREF_KEY_INITIAL_TOPICS_SELECTION_STATUS = "initial_topics_selection_status";
    private static final String PREF_KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String PREF_KEY_DND_ENABLED = "dnd_enabled";
    private static final String PREF_KEY_DND_RANGE = "dnd_range";

    @Override
    public void onCreate() {
        super.onCreate();
        Notificare.shared().setDebugLogging(BuildConfig.DEBUG);
        //Notificare.shared().setUseLegacyGCM();
        Notificare.shared().launch(this);
        Notificare.shared().setIntentReceiver(AppReceiver.class);
        Notificare.shared().setUserPreferencesResource(R.xml.preferences);
        Notificare.shared().setSmallIcon(R.drawable.ic_stat_notify_msg);
        Notificare.shared().setAllowJavaScript(true);
        Notificare.shared().setAllowOrientationChange(false);
        Notificare.shared().setPassbookRelevanceOngoing(true);

        // Crash logs are enabled by default.
        // However, you opt-out by using the following instruction
        //Notificare.shared().setCrashLogs(false);

        // The SDK supports a single optional placeholder, %s.
        // If the placeholder is provided, it will be replaced by the pass' description, if any.
        //Notificare.shared().setRelevanceText("Notificare demo: %s");
        //Notificare.shared().setRelevanceIcon(R.drawable.notificare_passbook_style);

        NotificareSupport.shared().launch(this);

        // Internet connection configs
        NotificareSupport.shared().setInternetConnectionActivityTheme(R.style.AppTheme);
        NotificareSupport.shared().setInternetConnectionActivityLayout(R.layout.activity_no_internet);

        // Run internet connection manager
        NotificareSupport.shared().getInternetConnectionManager().registerNetworkStateReceiver();
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

    public static boolean getInitialTopicsSelectionStatus() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_INITIAL_TOPICS_SELECTION_STATUS, false);
    }

    public static void setInitialTopicsSelectionStatus(boolean status) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PREF_KEY_INITIAL_TOPICS_SELECTION_STATUS, status);
        editor.apply();
    }

    public static boolean getNotificationsEnabled() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(boolean enabled) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, enabled);
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
}
