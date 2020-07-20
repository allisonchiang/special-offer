package com.example.specialoffer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("StaticFieldLeak")
class FenceDataDownloader extends AsyncTask<String, Void, String> {

    private static final String TAG = "FenceDownloader";
    private Geocoder geocoder;
    private FenceMgr fenceMgr;
    static ArrayList<FenceData> fences = new ArrayList<>();
    private static final String FENCE_URL = "http://www.christopherhield.com/data/fences.json";

    FenceDataDownloader(Activity activity, FenceMgr fenceMgr) {
        this.fenceMgr = fenceMgr;
        geocoder = new Geocoder(activity);
    }

    @Override
    protected void onPostExecute(String result) {

        if (result == null)
            return;

        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fObj = jArr.getJSONObject(i);
                String id = fObj.getString("id");
                String address = fObj.getString("address");
                String website = fObj.getString("website");
                float rad = (float) fObj.getDouble("radius");
                int type = fObj.getInt("type");
//                double lat = fObj.getDouble("lat");
//                double lon = fObj.getDouble("lon");
                String message = fObj.getString("message");
                String code = fObj.getString("code");
                String color = fObj.getString("fenceColor");
                String logo = fObj.getString("logo");

                LatLng ll = getLatLong(address);

                FenceData fd = new FenceData(id, ll.latitude, ll.longitude, address, website, rad, type, message, code, color, logo);
                fences.add(fd);
                Log.d(TAG, "onPostExecute: " + fd.getId());
            }
            fenceMgr.addFences(fences);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private LatLng getLatLong(String address) {
        try {

            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            Address a = addressList.get(0);
            return new LatLng(a.getLatitude(), a.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}