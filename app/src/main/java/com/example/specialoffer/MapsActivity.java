package com.example.specialoffer;

import android.app.NotificationManager;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Needs:
    //      implementation 'com.google.android.gms:play-services-maps:17.0.0'
    //      implementation 'com.google.android.gms:play-services-location:17.0.0'

    //      android:usesCleartextTraffic="true"

    private static final String TAG = "MapsActivity";

    private CheckBox checkBox1, checkBox2;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Polyline llHistoryPolyline;
    private ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private boolean zooming = false;
    private float oldZoom;
    private Marker carMarker;
    private FenceMgr fenceMgr;
    private Geocoder geocoder;
    private TextView locationText;

    private ArrayList<Circle> circles = new ArrayList<>();
    private List<PatternItem> pattern = Collections.<PatternItem>singletonList(new Dot());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Typeface textFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        fenceMgr = FenceMgr.getInstance(MapsActivity.this);

        locationText = findViewById(R.id.locationText);
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);

        locationText.setTypeface(textFont);
        checkBox1.setTypeface(textFont);
        checkBox2.setTypeface(textFont);

        geocoder = new Geocoder(this);

        setupMap();

    }

    public void setupMap() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    /**
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        zooming = true;

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        setupLocationListener();
        setupZoomListener();
        drawFences();

        Log.d(TAG, "onMapReady: DONE");
    }



    private void setupLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // checkbox to show/remove fences
    public void showGeofenceClicked(View v) {
        if (checkBox1.isChecked()) {
            drawFences();
        } else {
            eraseFences();
        }
    }

    // checkbox to show/remove addresses
    public void showAddressClicked(View v) {
        if (checkBox2.isChecked()) {
            locationText.setVisibility(View.VISIBLE);
        } else {
            locationText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void drawFences() {
        Log.d(TAG, "drawFences: ");
        for (FenceData fd : FenceMgr.fences.values()) {
            drawFence(fd);
        }
    }

    public void drawFence(FenceData fd) {
        Log.d(TAG, "drawFence: ");

        int line = Color.parseColor(fd.getFenceColor());
        int fill = ColorUtils.setAlphaComponent(line, 85);

        LatLng latLng = new LatLng(fd.getLat(), fd.getLon());
        Circle c = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circles.add(c);
    }

    void eraseFences() {
        Log.d(TAG, "eraseFences: ");
        for (Circle c : circles) {
            c.remove();
            Log.d(TAG, "eraseFences: " + c.getId());
        }
        circles.clear();
    }

    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (zooming) {
                    Log.d(TAG, "onCameraIdle: DONE ZOOMING: " + mMap.getCameraPosition().zoom);
                    zooming = false;
                    oldZoom = mMap.getCameraPosition().zoom;
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (mMap.getCameraPosition().zoom != oldZoom) {
                    Log.d(TAG, "onCameraMove: ZOOMING: " + mMap.getCameraPosition().zoom);
                    zooming = true;
                }
            }
        });
    }

    // called from location listener
    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            locationText.setText(address.getAddressLine(0));

        } catch (IOException e) {
            e.printStackTrace();
            locationText.setText("");
        }


        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);


            float r = getRadius();
            if (r > 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.car);
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());

                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);
            }
        }

        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        return 15f * z - 145f;
    }
}