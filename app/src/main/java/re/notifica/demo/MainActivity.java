package re.notifica.demo;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.beacon.BeaconRangingListener;
import re.notifica.billing.BillingManager;
import re.notifica.billing.BillingResult;
import re.notifica.billing.Purchase;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareBeacon;
import re.notifica.model.NotificareInboxItem;
import re.notifica.model.NotificareNotification;
import re.notifica.model.NotificareScannable;
import re.notifica.support.NotificareSupport;
import re.notifica.support.v7.app.ActionBarBaseActivity;

public class MainActivity extends ActionBarBaseActivity implements Notificare.OnNotificareReadyListener, Notificare.OnBillingReadyListener, Notificare.OnNotificareNotificationListener, BillingManager.OnRefreshFinishedListener, BillingManager.OnPurchaseFinishedListener, BeaconRangingListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SCANNABLE_REQUEST_CODE = 9001;
    protected static final String TAG = MainActivity.class.getSimpleName();
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        manageFragments("");

        builder = new AlertDialog.Builder(this);

        Notificare.shared().addNotificareReadyListener(this);

        Log.i(TAG, "Intent: " + getIntent().getData());
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "new intent: " + intent.getData());
        handleIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNABLE_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    NotificareScannable scannable = Notificare.shared().extractScannableFromActivityResult(data);
                    if (scannable != null) {
                        if (scannable.getNotification() != null) {
                            Notificare.shared().openNotification(this, scannable.getNotification());
                        } else {
                            Log.i(TAG, "scannable with type " + scannable.getType());
                        }
                    } else {
                        Toast.makeText(this, "scannable not found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "scan did not return any results", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == CommonStatusCodes.CANCELED) {
                Toast.makeText(this, "scan was canceled", Toast.LENGTH_LONG).show();
            } else {
                Log.w(TAG, "error result: " + resultCode);
            }
        } else if (!(Notificare.shared().getBillingManager() != null && Notificare.shared().getBillingManager().handleActivityResult(requestCode, resultCode, data))) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Notificare.shared().fetchScannable(data.toString(), new NotificareCallback<NotificareScannable>() {

                @Override
                public void onSuccess(NotificareScannable notificareScannable) {
                    if (notificareScannable != null) {
                        if (notificareScannable.getNotification() != null) {
                            Notificare.shared().openNotification(MainActivity.this, notificareScannable.getNotification());
                        } else {
                            Toast.makeText(MainActivity.this, "scannable found", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onError(NotificareError notificareError) {
                    Toast.makeText(MainActivity.this, "scannable not found", Toast.LENGTH_LONG).show();
                }
            });
        } else if (data != null) {
            Log.d(TAG, "uri is " + data.toString() + ", path is " + data.getPath());
            String base = data.getPath();
            if (data.getQuery() != null) {
                base = base.concat("?").concat(data.getQuery());
            }
            manageFragments(base);
        } else if (intent.hasCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
            manageFragments("/settings");
        } else {
            handleNotificationOpenedIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().addRangingListener(this);
        }
        Notificare.shared().addNotificareNotificationListener(this);
        Notificare.shared().addBillingReadyListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().removeRangingListener(this);
        }
        Notificare.shared().removeNotificareNotificationListener(this);
        Notificare.shared().removeBillingReadyListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notificare.shared().removeNotificareReadyListener(this);
    }

    @Override
    public void onNotificareReady(NotificareApplicationInfo notificareApplicationInfo) {

        if (Notificare.shared().isNotificationsEnabled()) {
            int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
            ShortcutBadger.applyCount(this.getApplicationContext(), badgeCount);
        }
        askBackgroundLocationPermission();
    }

    @Override
    public void onNotificareNotification(NotificareNotification notificareNotification, NotificareInboxItem notificareInboxItem, Boolean shouldPresent) {
        Log.i(TAG, "foreground notification received");
        if (shouldPresent) {
            Log.i(TAG, "should present incoming notification");
            Snackbar notificationSnackbar = Snackbar.make(findViewById(R.id.content_frame), notificareNotification.getMessage(), Snackbar.LENGTH_INDEFINITE);
            notificationSnackbar.setAction(R.string.open, view -> {
                if (notificareInboxItem != null && Notificare.shared().getInboxManager() != null) {
                    Notificare.shared().openInboxItem(this, notificareInboxItem);
                } else {
                    Notificare.shared().openNotification(this, notificareNotification);
                }
            });
            notificationSnackbar.show();
        }
    }

    @Override
    public void onBillingReady() {
        Notificare.shared().getBillingManager().refresh(this);
    }

    @Override
    public void onRefreshFinished() {
        Log.i(TAG, "billing refresh finished");
    }

    @Override
    public void onRefreshFailed(NotificareError notificareError) {
        Log.e(TAG, "billing refresh failed: " + notificareError.getLocalizedMessage());
    }

    @Override
    public void onPurchaseFinished(BillingResult billingResult, Purchase purchase) {
        Log.i(TAG, "purchase finished: " + billingResult.getMessage());
    }

    public void askForegroundLocationPermission() {
        if (!Notificare.shared().hasForegroundLocationPermissionGranted()) {
            Log.i(TAG, "permission not granted");
            if (Notificare.shared().didRequestLocationPermission()) {
                if (Notificare.shared().shouldShowForegroundRequestPermissionRationale(this)) {
                    // Here we should show a dialog explaining location updates
                    builder.setMessage(R.string.alert_location_permission_rationale)
                            .setTitle(R.string.app_name)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_location_permission_rationale_ok, (dialog, id) -> Notificare.shared().requestLocationPermission(MainActivity.this, LOCATION_PERMISSION_REQUEST_CODE))
                            .create()
                            .show();
                }
            } else {
                Notificare.shared().requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.i(TAG, "foreground location permission granted, we can update location");
            Notificare.shared().enableLocationUpdates();
            if (BuildConfig.ENABLE_BEACONS) {
                Notificare.shared().enableBeacons(30000);
            }
        }
    }

    public void askBackgroundLocationPermission() {
        if (!Notificare.shared().hasLocationPermissionGranted()) {
            Log.i(TAG, "permission not granted");
            if (Notificare.shared().didRequestLocationPermission()) {
                if (Notificare.shared().shouldShowRequestPermissionRationale(this)) {
                    // Here we should show a dialog explaining location updates
                    builder.setMessage(R.string.alert_location_permission_rationale)
                            .setTitle(R.string.app_name)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_location_permission_rationale_ok, (dialog, id) -> Notificare.shared().requestLocationPermission(MainActivity.this, LOCATION_PERMISSION_REQUEST_CODE))
                            .create()
                            .show();
                }
            } else {
                Notificare.shared().requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.i(TAG, "background location permission granted, we can update location");
            Notificare.shared().enableLocationUpdates();
            if (BuildConfig.ENABLE_BEACONS) {
                Notificare.shared().enableBeacons(30000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (Notificare.shared().checkRequestLocationPermissionResult(permissions, grantResults)) {
                    Log.i(TAG, "background location permission granted");
                    Notificare.shared().enableLocationUpdates();
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(30000);
                    }
                } else if (Notificare.shared().checkRequestForegroundLocationPermissionResult(permissions, grantResults)) {
                    Log.i(TAG, "foreground locations permission granted");
                    Notificare.shared().enableLocationUpdates();
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(30000);
                    }
                }
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {

            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void manageFragments(String tag){

        Log.d(TAG, "open fragment for tag: " + tag);

        switch (tag) {
            case "/inbox":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new InboxFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/settings":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new SettingsFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/regions":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new RegionsFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/signup":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new SignUpFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/lostpass":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new LostPassFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/profile":

                if (Notificare.shared().isLoggedIn()) {

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fragment_enter,
                                    R.anim.fragment_exit,
                                    R.anim.fragment_pop_enter,
                                    R.anim.fragment_pop_exit)
                            .replace(R.id.content_frame, new ProfileFragment())
                            .addToBackStack(tag)
                            .commit();
                } else {

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fragment_enter,
                                    R.anim.fragment_exit,
                                    R.anim.fragment_pop_enter,
                                    R.anim.fragment_pop_exit)
                            .replace(R.id.content_frame, new SignInFragment())
                            .addToBackStack(tag)
                            .commit();
                }

                break;
            case "/analytics":

                final EditText input = new EditText(this);
                input.setHint(R.string.hint_event_name);

                builder.setMessage(R.string.analytics_text)
                        .setTitle(R.string.app_name)
                        .setCancelable(false)
                        .setView(input)
                        .setCancelable(true)
                        .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                        })
                        .setPositiveButton(android.R.string.ok, (dialog, id) -> Notificare.shared().getEventLogger().logCustomEvent(input.getText().toString(), new NotificareCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Log.i(TAG, "Event sent");
                            }

                            @Override
                            public void onError(NotificareError notificareError) {
                                Log.e(TAG, "Error sending event");
                            }
                        }));
                builder.create();
                builder.show();


                break;
            case "/membercard":

                Log.i("SERIAL", AppBaseApplication.getMemberCardSerial());

                if (AppBaseApplication.getMemberCardSerial().isEmpty()) {

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fragment_enter,
                                    R.anim.fragment_exit,
                                    R.anim.fragment_pop_enter,
                                    R.anim.fragment_pop_exit)
                            .replace(R.id.content_frame, new SignInFragment())
                            .addToBackStack(tag)
                            .commit();

                } else {

                    Notificare.shared().getPassbookManager().open(AppBaseApplication.getMemberCardSerial());

                }

                break;
            case "/storage":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new StorageFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/beacons":

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,
                                R.anim.fragment_exit,
                                R.anim.fragment_pop_enter,
                                R.anim.fragment_pop_exit)
                        .replace(R.id.content_frame, new BeaconsFragment())
                        .addToBackStack(tag)
                        .commit();

                break;
            case "/scan":

                Notificare.shared().startScannableActivity(this, SCANNABLE_REQUEST_CODE);

                break;
            default:

                MainFragment fragment = new MainFragment();
                Bundle args = new Bundle();
                args.putString("url", tag);
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
                break;
        }
    }

    public void createMemberCard(String name, String email){

        JSONObject payload = null;
        JSONArray primaryFields = null;
        JSONArray secondaryFields = null;
        try {
            payload = new JSONObject(AppBaseApplication.getMemberCardTemplate());
            payload.put("passbook", payload.getString("_id"));

            String url = "https://gravatar.com/avatar/" + md5(email.trim().toLowerCase()) + "?s=512";
            payload.getJSONObject("data").put("thumbnail", url);

            primaryFields = payload.getJSONObject("data").getJSONArray("primaryFields");

            for (int i = 0; i < primaryFields.length(); i++) {
                JSONObject field = (JSONObject) primaryFields.get(i);
                if (field.getString("key").equals("name")) {
                    field.put("value", name);
                }
            }

            secondaryFields = payload.getJSONObject("data").getJSONArray("secondaryFields");
            for (int i = 0; i < secondaryFields.length(); i++) {
                JSONObject field = (JSONObject) secondaryFields.get(i);
                if (field.getString("key").equals("email")) {
                    field.put("value", email);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Notificare.shared().doCloudRequest("POST", "/api/pass", null, payload, new NotificareCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

                String serial = null;
                try {
                    serial = jsonObject.getJSONObject("pass").getString("serial");
                    AppBaseApplication.setMemberCardSerial(serial);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(NotificareError notificareError) {
                Log.i("PASS", notificareError.getMessage());
            }
        });
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRangingBeacons(final List<NotificareBeacon> list) {
        runOnUiThread(() -> Log.d("MAIN ACTIVITY", list.toString()));
    }
}
