package com.example.mtmsapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsUtils {

    public static Location convertLatLongToLocation(LatLng latLng) {

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        return location;
    }

    public static void zoomMap(Location myLocation, GoogleMap map) {

        Location location = myLocation;
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public static Marker addCustomizedMarker(final LatLng location, String title, final Context context,
                                             int drawable, GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.title(title).icon((BitmapDescriptorFactory.fromBitmap(drawableToBitmap(context.getDrawable(drawable)))));
        return googleMap.addMarker(markerOptions);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String getLocationAddress(LatLng latLng, Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String finalAddress = "Un defined Location";

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0) {

                finalAddress = "";

                String knownName = addresses.get(0).getFeatureName() + "";
                String streetName = addresses.get(0).getThoroughfare() + "";
                String cityName = addresses.get(0).getSubAdminArea() + "";


                finalAddress += knownName.trim() + ", ";

                if (!streetName.equals(knownName))
                    finalAddress += streetName.trim() + ", ";

                if (!streetName.equals(cityName) && !cityName.equals(knownName))
                    finalAddress += cityName.trim();

                if (finalAddress.contains(" null,"))
                    finalAddress = finalAddress.replace("null,", "");

                else if (finalAddress.contains(", null"))
                    finalAddress = finalAddress.replaceAll(", null", "");

                if (String.valueOf(finalAddress.charAt(finalAddress.trim().length() - 1)).equals(","))
                    finalAddress = finalAddress.replaceAll(",", "");


            }

        } catch (IOException e) {
            //Toast.makeText(context, R.string.problem_getting_address, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return "Undefined Location";
        }

            return finalAddress;

    }


}
