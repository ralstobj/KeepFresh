package com.rgw.keepfresh;


import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * Created by Ralstonb on 12/23/2016.
 */

public class GroceryLocations extends Fragment {
    View myView;
    public static final String LOG_TAG = GroceryLocations.class.getSimpleName();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.grocery_locations, container, false);

        Button button = (Button) myView.findViewById(R.id.grocery_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Location loc = GPS.getLastLocation(getActivity().getApplicationContext());
                    Double myLongitude = loc.getLongitude();
                    Double myLatitude = loc.getLatitude();
                    //using google maps you will create a map setting lat & long.
                    String urlAddress = "http://maps.google.com/maps?q=" + myLatitude + "," + myLongitude + "?z=10&q=Grocery";
                    Uri gmmIntentUri = Uri.parse(urlAddress);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch(Exception e){
                    Log.e(LOG_TAG, "Error with Loading Maps");
                    Toast.makeText(getActivity(), getString(R.string.error_maps),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return myView;
    }
}
