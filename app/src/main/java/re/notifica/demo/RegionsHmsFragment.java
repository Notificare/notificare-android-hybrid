package re.notifica.demo;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.CircleOptions;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.Polygon;
import com.huawei.hms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegionsHmsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = RegionsHmsFragment.class.getSimpleName();
    public MapView mapView;
    public HuaweiMap map;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public List<Circle> circlesList;
    public List<Polygon> polygonsList;
    public List<Marker> markersList;
    public Marker userLocation;

    public RegionsHmsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_regions);
        }

        View rootView = inflater.inflate(R.layout.fragment_regions_hms, container, false);

        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        return rootView;
    }


    // Check for permission to access Location
    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }
//    // Asks for permission
//    private void askPermission() {
//        ActivityCompat.requestPermissions(
//                getActivity(),
//                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
//                REQUEST_CODE_ASK_PERMISSIONS
//        );
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        switch (requestCode) {
//            case REQUEST_CODE_ASK_PERMISSIONS: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted
//                    if (checkPermission()) {
//                        map.setMyLocationEnabled(true);
//
//                        AppBaseApplication.setLocationEnabled(true);
//
//                        Notificare.shared().enableLocationUpdates();
//
//                        if (BuildConfig.ENABLE_BEACONS) {
//                            Notificare.shared().enableBeacons(30000);
//                        }
//
//                        loadLocations();
//                    }
//
//
//                } else {
//                    // Permission denied
//
//                }
//                break;
//            }
//        }
//    }

    public void loadLocations() {

        if (Notificare.shared().hasForegroundLocationPermissionGranted()) {
            if (Notificare.shared().getLastKnownLocation() != null) {
                double lat = Notificare.shared().getLastKnownLocation().getLatitude();
                double lon = Notificare.shared().getLastKnownLocation().getLongitude();

                userLocation = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("user_location",64,64)))
                        .position(new LatLng(lat, lon))
                        .title("My Location"));

                // Updates the location and zoom of the MapView
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));

                circlesList = new ArrayList<>();
                polygonsList = new ArrayList<>();
                markersList = new ArrayList<>();

                Notificare.shared().doCloudRequest("GET", "/api/region", null, null, new NotificareCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {

                        JSONArray regions;
                        try {
                            regions = jsonObject.getJSONArray("regions");
                            for (int i = 0; i < regions.length(); i++) {

                                JSONObject region = (JSONObject) regions.get(i);

                                LatLng location = new LatLng(region.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1), region.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));

                                if (!region.isNull("advancedGeometry")) {

                                    JSONArray coordinates = region.getJSONObject("advancedGeometry").getJSONArray("coordinates").getJSONArray(0);

                                    PolygonOptions poly = new PolygonOptions();
                                    poly.fillColor(R.color.colorPrimary);
                                    poly.strokeColor(0);
                                    poly.strokeWidth(0);

                                    for (int j = 0; j < coordinates.length(); j++) {
                                        JSONArray c = coordinates.getJSONArray(j);
                                        poly.add(new LatLng(c.getDouble(1), c.getDouble(0)));
                                    }

                                    Polygon polygon = map.addPolygon(poly);

                                    polygonsList.add(polygon);

                                } else {
                                    Circle circle = map.addCircle(new CircleOptions()
                                            .center(location)
                                            .radius(region.getDouble("distance"))
                                            .fillColor(R.color.colorPrimary)
                                            .strokeColor(0)
                                            .strokeWidth(0));
                                    circlesList.add(circle);
                                }


                                Marker marker = map.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("map_marker",64,64)))
                                        .position(location)
                                        .title(region.getString("name")));
                                markersList.add(marker);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parse error: " + e.getMessage());
                        }


                    }

                    @Override
                    public void onError(NotificareError notificareError) {

                        Log.i("HTTP error", notificareError.getMessage());
                    }
                });

            }
        } else {
            ((MainActivity)getActivity()).askForegroundLocationPermission();
        }

    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        map = huaweiMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMapType(HuaweiMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        loadLocations();

    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }
}
