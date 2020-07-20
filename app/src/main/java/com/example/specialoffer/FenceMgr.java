package com.example.specialoffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;

class FenceMgr {

    private static final String TAG = "FenceMgr";
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    public static HashMap<String, FenceData> fences = new HashMap<>();
    private static FenceMgr instance;
    private Activity activity;

    public static FenceMgr getInstance(Activity a) {
        if (instance == null)
            instance = new FenceMgr(a);
        return instance;
    }

    private FenceMgr(Activity activity) {
        this.activity = activity;

        // get the location service geofencing client
        geofencingClient = LocationServices.getGeofencingClient(activity);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: removeGeofences");
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: removeGeofences");
//                        Toast.makeText(mapsActivity, "Trouble removing existing fences: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        loadFences();
    }

    public static FenceData getFenceData(String id) {
        return fences.get(id);
    }

    void loadFences() {
        new FenceDataDownloader(activity, this).execute();
    }

//    public void drawFences(MapsActivity mapsActivity) {
//        for (FenceData fd : fences.values()) {
//            mapsActivity.drawFence(fd);
//        }
//    }

    void addFences(ArrayList<FenceData> fenceList) {
        // clear the hashmap
        fences.clear();

        for (FenceData fd : fenceList) {
            fences.put(fd.getId(), fd);
        }

        // for each fence in file, create/add Geofence
        for (FenceData fd : fences.values()) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(fd.getId())
                    .setCircularRegion(
                            fd.getLat(),
                            fd.getLon(),
                            fd.getRadius())
                    .setTransitionTypes(fd.getType())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE) //Fence expires after N millis  -or- Geofence.NEVER_EXPIRE
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .build();

            geofencePendingIntent = getGeofencePendingIntent();

            geofencingClient
                    .addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: addGeofences");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "onFailure: addGeofences");
                            Toast.makeText(activity, "Trouble adding new fence: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
        }
    }

    private PendingIntent getGeofencePendingIntent() {

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(activity, GeofenceBroadcastReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return geofencePendingIntent;
    }
}