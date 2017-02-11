package re.notifica.hybrid;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
public class RegionsFragment extends Fragment implements OnMapReadyCallback {

    public MapView mapView;
    public GoogleMap map;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public List<Circle> circlesList;
    public List<Polygon> polygonsList;
    public List<Marker> markersList;

    public RegionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_regions);
        View rootView = inflater.inflate(R.layout.fragment_regions, container, false);

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        return rootView;
    }


    // Check for permission to access Location
    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }
    // Asks for permission
    private void askPermission() {
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                REQUEST_CODE_ASK_PERMISSIONS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission()){
                        map.setMyLocationEnabled(true);
                        loadLocations();
                    }


                } else {
                    // Permission denied

                }
                break;
            }
        }
    }

    public void loadLocations(){


        double lat = Notificare.shared().getCurrentLocation().getLatitude();
        double lon = Notificare.shared().getCurrentLocation().getLongitude();

        // Updates the location and zoom of the MapView
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));


        circlesList = new ArrayList<Circle>();
        polygonsList = new ArrayList<Polygon>();
        markersList = new ArrayList<Marker>();

        Notificare.shared().doCloudRequest("GET", "/api/region", null, null, new NotificareCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

                JSONArray regions = null;
                try {
                    regions = jsonObject.getJSONArray("regions");
                    for (int i = 0; i < regions.length(); i++) {

                        JSONObject region = (JSONObject) regions.get(i);

                        LatLng location = new LatLng((double) region.getJSONObject("geometry").getJSONArray("coordinates").get(1), (double) region.getJSONObject("geometry").getJSONArray("coordinates").get(0));

                        if (!region.isNull("advancedGeometry")) {

                            JSONArray coordinates = (JSONArray) region.getJSONObject("advancedGeometry").getJSONArray("coordinates").get(0);

                            ArrayList<LatLng> locations = new ArrayList<LatLng>();

                            for (int j = 0; j < coordinates.length(); j++) {
                                //locations.add(new LatLng(coordinates[j][1], coordinates[j][0]));
                            }


                            //It's a polygon
                            Polygon polygon = map.addPolygon(new PolygonOptions()
                                            .add()
                                            .fillColor(R.color.colorPrimary)
                                            .strokeColor(0)
                                            .strokeWidth(0));

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
                    e.printStackTrace();
                }


            }

            @Override
            public void onError(NotificareError notificareError) {

                Log.i("HTTP ERROR", notificareError.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);


        if (!checkPermission()){
            askPermission();
        } else {
            map.setMyLocationEnabled(true);
            loadLocations();
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}
