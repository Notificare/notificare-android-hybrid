package re.notifica.hybrid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

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
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareBeacon;

public class MainActivity extends AppCompatActivity implements Notificare.OnNotificareReadyListener, BeaconRangingListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected static final String TAG = MainActivity.class.getSimpleName();
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manageFragments("main");

        builder = new AlertDialog.Builder(this);

        Notificare.shared().addNotificareReadyListener(this);
        getSupportActionBar().setShowHideAnimationEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notificare.shared().removeNotificareReadyListener(this);
    }

    @Override
    public void onNotificareReady(NotificareApplicationInfo notificareApplicationInfo) {

        if (AppBaseApplication.getNotificationsEnabled()) {
            int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
            ShortcutBadger.applyCount(this.getApplicationContext(), badgeCount);
        }

        if (!Notificare.shared().hasLocationPermissionGranted()) {
            Log.i(TAG, "permission not granted");
            if (Notificare.shared().didRequestLocationPermission()) {
                if (Notificare.shared().shouldShowRequestPermissionRationale(this)) {
                    // Here we should show a dialog explaining location updates
                    builder.setMessage(R.string.alert_location_permission_rationale)
                            .setTitle(R.string.app_name)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_location_permission_rationale_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Notificare.shared().requestLocationPermission(MainActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                                }
                            })
                            .create()
                            .show();
                }
            } else {
                Notificare.shared().requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.i(TAG, "permission granted");
            Notificare.shared().enableLocationUpdates();
            AppBaseApplication.setLocationEnabled(true);
            if (BuildConfig.ENABLE_BEACONS) {
                Notificare.shared().enableBeacons(60000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (Notificare.shared().checkRequestLocationPermissionResult(permissions, grantResults)) {
                    Log.i(TAG, "permission granted");
                    Notificare.shared().enableLocationUpdates();
                    AppBaseApplication.setLocationEnabled(true);
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(60000);
                    }
                }
                break;
        }
    }

    @Override
    public void onRangingBeacons(List<NotificareBeacon> list) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {

            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void manageFragments(String tag){

        if (tag.equals("/inbox")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new InboxFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("/settings")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new SettingsFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("/regions")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new RegionsFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("/signup")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new SignUpFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("/lostpass")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new LostPassFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("/profile")) {

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

        } else if (tag.equals("/analytics")) {

            final EditText input = new EditText(this);
            input.setHint(R.string.hint_event_name);

            builder.setMessage(R.string.analytics_text)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setView(input)
                    .setCancelable(true)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Notificare.shared().getEventLogger().logCustomEvent(input.getText().toString());

                        }
                    });
            builder.create();
            builder.show();


        } else if (tag.equals("/membercard")) {

            Log.i("SERIAL",AppBaseApplication.getMemberCardSerial());

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

        } else if (tag.equals("/storage")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new StorageFragment())
                    .addToBackStack(tag)
                    .commit();

        } else {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MainFragment())
                    .commit();
        }
    }

    public void createMemberCard(String name, String email){

        JSONObject payload = null;
        JSONArray primaryFields = null;
        JSONArray secondaryFields = null;
        try {
            payload = new JSONObject(AppBaseApplication.getMemberCardTemplate());
            payload.put("passbook", payload.getString("_id"));

            String url = "http://gravatar.com/avatar/" + md5(email.trim().toLowerCase()) + "?s=512";
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

    public static final String md5(final String s) {
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

}
