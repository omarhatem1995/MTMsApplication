package com.example.mtmsapplication.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mtmsapplication.R;
import com.example.mtmsapplication.adapter.AddressList_Adapter;
import com.example.mtmsapplication.adapter.OnAddressItemClickListener;
import com.example.mtmsapplication.model.SourceLocation;
import com.example.mtmsapplication.utils.Constants;
import com.example.mtmsapplication.utils.MapsUtils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnAddressItemClickListener {

    private Location currentLocation;

    private ImageView menuIcon , backIcon, mGps;
    private TextView placeTxt;
    private EditText yourLocation;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private RecyclerView recyclerView;
    private AddressList_Adapter addressList_adapter;

    private GoogleMap mMap;
    private Location destinationPlaceLocation ;
    private Location yourPlaceLocation = new Location(LocationManager.GPS_PROVIDER);

    private Marker marker , markerYourLocation;

    private OnAddressItemClickListener onAddressItemClickListener;
    private String locationFullAddress = "null";
    private int icon;

    private List<SourceLocation> sourceLocationList = new ArrayList<>();
    private SourceLocation sourceLocation = new SourceLocation();
    private SourceLocation destinationLocation = new SourceLocation();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initMaps();

        initViews();

        getAddressesList();
    }

    private void initViews() {

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(500);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mGps = (ImageView) findViewById(R.id.ic_gps);

        bitmapIcon = MapsUtils.drawableToBitmap(getDrawable(Constants.driverMarkerRes));
        menuIcon = findViewById(R.id.menu_icon);
        backIcon = findViewById(R.id.back_icon);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        yourLocation = findViewById(R.id.yourlocation_editext);
        placeTxt = findViewById(R.id.chosen_place_txt);

        recyclerView = findViewById(R.id.your_location_places_recyclerview);

        icon = R.drawable.ic_marker;

        navigationView.setNavigationItemSelectedListener(this);
        menuIcon.setOnClickListener(this);
        backIcon.setOnClickListener(this);

        yourLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setAdapter();
                changeIcons();
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (mLocationPermissionGranted)
                    if (isLocationEnabled(MapsActivity.this)) {
                        Log.d("wslthena", " us called");
                        if (currentLocation != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17));

                        } else {
                            Toast.makeText(MapsActivity.this, R.string.please_try_again, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        enableGPS();
                        Log.d("wslthena", "is cnot ls");
                    }
                else {
                    Log.d("wslthena", "sdsddd");
                    getLocationPermission();
                }

            }
        });


        setUpAddressList();

    }

    private void initMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initGooglePlaces();
    }

    public void setUpAddressList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);
    }

    private void changeIcons(){
        backIcon.setVisibility(View.VISIBLE);
        menuIcon.setVisibility(View.INVISIBLE);
    }

    private void initGooglePlaces() {
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

                        if (marker!=null)
                            marker.remove();

                        final LatLng latLng = place.getLatLng();

                        locationFullAddress = place.getName();
                        placeTxt.setText(locationFullAddress);
                        destinationPlaceLocation = MapsUtils.convertLatLongToLocation(latLng);

                        destinationLocation.setName(place.getName());
                        destinationLocation.setLatitude(destinationPlaceLocation.getLatitude());
                        destinationLocation.setLongitude(destinationPlaceLocation.getLongitude());

                        Log.d("destinationLocation",destinationLocation.getName()+" , "
                        +destinationLocation.getLatitude()+ " , " + destinationLocation.getLongitude());

                        MapsUtils.zoomMap(destinationPlaceLocation, mMap);

                        marker = MapsUtils.addCustomizedMarker(latLng, "Destination Place", MapsActivity.this, icon, mMap);
                    }

                    @Override
                    public void onError(Status status) {
                        if (status != null) {
                            Log.d("APIError", status + "");
                        }
                    }
                });
    }

    private void setAdapter(){
        addressList_adapter = new AddressList_Adapter(getApplicationContext(),
                sourceLocationList,this);
        recyclerView.setAdapter(addressList_adapter);
        recyclerView.setVisibility(View.VISIBLE);

    }

    private void hideRecyclerView(){
        recyclerView.setVisibility(View.GONE);
        backIcon.setVisibility(View.INVISIBLE);
        menuIcon.setVisibility(View.VISIBLE);
    }

    private void getAddressesList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Source")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("LogTAG", document.getId() + " => " + document.getData().get("name"));
                                SourceLocation address = new SourceLocation();
                                address.setName(document.getData().get("name").toString());
                                address.setLatitude((Double) document.getData().get("latitude"));
                                address.setLongitude((Double) document.getData().get("longitude"));

                                sourceLocationList.add(address);

                            }

                        } else {
                            Log.w("LogTAG", "Error getting documents.", task.getException());
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
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (mLocationPermissionGranted) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null)
                        updateMarkLocationAndZoom(location);
                }
            });
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon));

                }
            });
        }

        }


    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location location = locationResult.getLastLocation();


            if (location != null && currentLocation != null) {

                if (location.distanceTo(currentLocation) > Constants.markerDistance) {
                    currentLocation = location;

                    updateMarkLocationAndZoom(location);

                }
            } else {
                if (location != null) {
                }
            }
        }
    };
    LatLng partnerLocationLatLng;
    Bitmap bitmapIcon;
    void updateMarkLocationAndZoom(Location location) {

        partnerLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (marker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(partnerLocationLatLng)
                    .anchor((float) 0.5, (float) 0.5)
                    .title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon));
            marker = mMap.addMarker(markerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(partnerLocationLatLng, 17));
        } else {
            marker.setPosition(partnerLocationLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(partnerLocationLatLng, 17));
        }

    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);

    }
    private void getLocationPermission() {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) { //handling location permission 10

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

            if (checkLocationPermissionCall()) {
                mLocationPermissionGranted = true;
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            } else
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);

        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) { //handling location permission 11 and higher
            if (checkLocationPermissionCall()) {
                mLocationPermissionGranted = true;
                locationPermissionGranted();
            } else
                locaitonPermissionDeined();
        } else {

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);

                } else
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            } else
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    protected void enableGPS() {

        Log.d("wslthena","is called");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {

        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);


                } catch (IntentSender.SendIntentException sendEx) {
                    Toast.makeText(this, sendEx.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean checkLocationPermissionCall() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    return true;

                } else
                    return false;
            } else
                return false;

        } else
            return false;
    }

    void locaitonPermissionDeined() {
        mLocationPermissionGranted = false;
        openAppSettings();
    }
    void openAppSettings() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_location_permission)
                .setMessage(R.string.location_permission_msg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setIcon(R.drawable.ic_marker)
                .setCancelable(false)
                .show();
    }
    private void locationPermissionGranted() {
        Log.d("requestPermission", "is called");
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()); // permission already checked before starting service

        mLocationPermissionGranted = true;

        enableGPS();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

    }
    private boolean checkLocationPermissionResult(int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        return true;

                    } else
                        return false;
                } else
                    return false;
            } else
                return false;
        } else
            return false;

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    if (checkLocationPermissionResult(grantResults)) {
                        locationPermissionGranted();
                    } else
                        locaitonPermissionDeined();
                } else {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()); // permission already checked before starting service
                                mLocationPermissionGranted = true;
                                enableGPS();

                                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map);
                                mapFragment.getMapAsync(MapsActivity.this);

                            } else
                                locaitonPermissionDeined();

                        } else
                            locaitonPermissionDeined();
                    } else
                        locaitonPermissionDeined();
                }

                return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_icon:
                openSideMenu();
                break;
            case R.id.back_icon:
                hideRecyclerView();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onItemClick(int position,String name,double lat,double lon) {

        hideRecyclerView();
        yourLocation.setText(name);
        sourceLocation.setName(name);
        sourceLocation.setLatitude(lat);
        sourceLocation.setLongitude(lon);

        if (markerYourLocation!=null)
            markerYourLocation.remove();

        yourLocation.setText(sourceLocation.getName());

        yourPlaceLocation.setLatitude(sourceLocation.getLatitude());
        yourPlaceLocation.setLongitude(sourceLocation.getLongitude());

        MapsUtils.zoomMap(yourPlaceLocation, mMap);
        LatLng latlng = new LatLng(yourPlaceLocation.getLatitude(), yourPlaceLocation.getLongitude());

        markerYourLocation = MapsUtils.addCustomizedMarker(latlng, "Your Location", MapsActivity.this, icon, mMap);


    }
}