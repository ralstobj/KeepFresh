package com.rgw.keepfresh;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Ralstonb on 12/24/2016.
 */

public class GPS {

    private static boolean pGps, pNetwork;
    private static LocationManager locManager;
    private static String provider;
    private static double longitude;
    private static double latitude;


    private static void updateAvailability() {
        try {
            pNetwork = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            provider = LocationManager.NETWORK_PROVIDER;
        } catch (Exception ex) {
            Log.w(TAG, "Ex getting NETWORK provider");
        }
        try {
            pGps = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            provider = LocationManager.GPS_PROVIDER;
        } catch (Exception ex) {
            Log.w(TAG, "Ex getting GPS provider");
        }
    }

    public static Location getLastLocation(Context ctx) {
        Location loc = null;
        if (ctx != null) {
            if (locManager == null) {
                locManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            }
            updateAvailability();
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null;
                }
                loc = locManager.getLastKnownLocation(provider);
            }
        }
        return loc;
    }


}