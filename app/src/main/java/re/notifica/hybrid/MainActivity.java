package re.notifica.hybrid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.beacon.BeaconRangingListener;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareAsset;
import re.notifica.model.NotificareBeacon;
import re.notifica.model.NotificareProduct;

public class MainActivity extends AppCompatActivity implements Notificare.OnNotificareReadyListener, BeaconRangingListener, InboxFragment.OnFragmentInteractionListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected static final String TAG = MainActivity.class.getSimpleName();
    private AlertDialog.Builder builder;
    public List<Circle> circlesList;
    public GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manageFragments("main");

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

        if (tag.equals("inbox")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new InboxFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("settings")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new SettingsFragment())
                    .addToBackStack(tag)
                    .commit();

        } else if (tag.equals("regions")) {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new RegionsFragment())
                    .addToBackStack(tag)
                    .commit();

//            getSupportActionBar().show();
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setShowHideAnimationEnabled(false);
//            getSupportActionBar().setTitle(R.string.title_regions);


        } else {

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit)
                    .replace(R.id.content_frame, new MainFragment())
                    .addToBackStack(tag)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i(TAG, uri.toString());
    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        map = googleMap;
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        googleMap.getUiSettings().setCompassEnabled(true);
//        googleMap.getUiSettings().setRotateGesturesEnabled(true);
//        googleMap.getUiSettings().setScrollGesturesEnabled(true);
//        googleMap.getUiSettings().setTiltGesturesEnabled(true);
//        googleMap.getUiSettings().setZoomGesturesEnabled(true);
//        googleMap.getUiSettings().setZoomControlsEnabled(true);
//    }
//
//    public void loadLocations(){
//
//        // Updates the location and zoom of the MapView
//        //float zoom = map.getCameraPosition().zoom;
//        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(, zoom));
//
//        Log.i("HERERERERE", "BLA");
//        circlesList = new ArrayList<Circle>();
//
//        Notificare.shared().doCloudRequest("GET", "region", null, null, new NotificareCallback<JSONObject>() {
//            @Override
//            public void onSuccess(JSONObject jsonObject) {
//
//                JSONArray regions = null;
//                try {
//                    regions = jsonObject.getJSONArray("regions");
//                    for (int i = 0; i < regions.length(); i++) {
//
//                        JSONObject region = (JSONObject)regions.get(i);
//                        Circle circle;
//                        circle = map.addCircle(new CircleOptions()
//                                .center(new LatLng((double)region.getJSONObject("geometry").getJSONArray("coordinates").get(1), (double)region.getJSONObject("geometry").getJSONArray("coordinates").get(0)))
//                                .radius(region.getDouble("distance"))
//                                .fillColor(R.color.colorPrimary)
//                                .strokeColor(0)
//                                .strokeWidth(0));
//
//                        circlesList.add(circle);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//
//
//            }
//
//            @Override
//            public void onError(NotificareError notificareError) {
//
//            }
//        });
//    }
}
