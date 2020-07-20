package com.example.specialoffer;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.example.specialoffer.SplashActivity.ACCURACY_REQUEST;
@SuppressLint("StaticFieldLeak")

class LocationAccuracyAsyncTask extends AsyncTask<String, Void, Void> {

    private SplashActivity context;
    private static final String TAG = "LocationAccuracyAsyncTa";

    public LocationAccuracyAsyncTask(SplashActivity c) {
        this.context = c;
    }

    @Override
    protected Void doInBackground(String... strings) {
        checkLocationAccuracy();
        return null;
    }

    private void checkLocationAccuracy() {

        Log.d(TAG, "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(context, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: High Accuracy Already Present");
                context.setup2();
            }
        });

        task.addOnFailureListener(context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(context, ACCURACY_REQUEST);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });

    }
}