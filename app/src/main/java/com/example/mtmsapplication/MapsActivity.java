package com.example.mtmsapplication;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ImageView menuIcon;
    private TextView placeTxt;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private GoogleMap mMap;
    private Location destinationPlaceLocation;
    private Marker marker;

    private String locationFullAddress = "null";
    private int icon ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initMaps();

        initViews();

    }

    private void initViews() {
        menuIcon = findViewById(R.id.menu_icon);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        placeTxt = findViewById(R.id.chosen_place_txt);

        icon = R.drawable.ic_marker;


        navigationView.setNavigationItemSelectedListener(this);
        menuIcon.setOnClickListener(this);

    }

    private void initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initGooglePlaces();
    }

    private void initGooglePlaces(){
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAPRgZ9k_a5Sr2KWtK7TJulY53URh8FkTw");
        }
        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.destination_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autocompleteSupportFragment.setActivityMode(AutocompleteActivityMode.OVERLAY);


        autocompleteSupportFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        final LatLng latLng = place.getLatLng();

                        locationFullAddress = place.getName();
                        placeTxt.setText(locationFullAddress);
                        destinationPlaceLocation = MapsUtils.convertLatLongToLocation(latLng);

                        MapsUtils.zoomMap(destinationPlaceLocation,mMap);

                        marker = MapsUtils.addCustomizedMarker(latLng,"Your Favorite Place",MapsActivity.this,icon,mMap);


                    }

                    @Override
                    public void onError(Status status) {
                        if (status != null) {
                            Toast.makeText(MapsActivity.this, status + " is error", Toast.LENGTH_SHORT).show();
                            Log.d("APIError", status + "");
                        }
                    }
                });
    }


    private void openSideMenu() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_icon:
                openSideMenu();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}