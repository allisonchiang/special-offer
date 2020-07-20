package com.example.specialoffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int LOCATION_REQUEST = 111;
    public static final int ACCURACY_REQUEST = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // check permissions for ACCESS_FINE_LOCATION
        if (checkPermission()) {
            setup();
        }

    }

    public void setup() {
        new LocationAccuracyAsyncTask(this).execute();
    }

    public void setup2() {
        Log.d(TAG, "setup2: ");

        new Thread(() -> FenceMgr.getInstance(SplashActivity.this)).start();

        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
        // close this activity
        finish();
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        setup();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Please Allow Location Access");
                        builder.setMessage("App cannot be run without location access permission.");
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            setup2();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


}