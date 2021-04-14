package com.example.mtmsapplication.ui;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.mtmsapplication.utils.MapsUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.widget.LinearLayout.HORIZONTAL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnAddressItemClickListener {

    private ImageView menuIcon;
    private TextView placeTxt;
    private EditText yourLocation;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private RecyclerView recyclerView;
    private AddressList_Adapter addressList_adapter;

    private GoogleMap mMap;
    private Location destinationPlaceLocation;
    private Marker marker;
    private OnAddressItemClickListener onAddressItemClickListener;
    private String locationFullAddress = "null";
    private int icon;
    private List<SourceLocation> sourceLocationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initMaps();

        initViews();

        getAddressesList();
    }

    private void initViews() {
        menuIcon = findViewById(R.id.menu_icon);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        yourLocation = findViewById(R.id.yourlocation_editext);
        placeTxt = findViewById(R.id.chosen_place_txt);

        recyclerView = findViewById(R.id.your_location_places_recyclerview);

        icon = R.drawable.ic_marker;

        navigationView.setNavigationItemSelectedListener(this);
        menuIcon.setOnClickListener(this);

        yourLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setAdapter();
                return false;
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

                        final LatLng latLng = place.getLatLng();

                        locationFullAddress = place.getName();
                        placeTxt.setText(locationFullAddress);
                        destinationPlaceLocation = MapsUtils.convertLatLongToLocation(latLng);

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
                                SourceLocation sourceLocation = new SourceLocation();
                                sourceLocation.setName(document.getData().get("name").toString());
                                sourceLocation.setLatitude(document.getData().get("latitude").toString());
                                sourceLocation.setLogitude(document.getData().get("longitude").toString());

                                sourceLocationList.add(sourceLocation);

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

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, position + " is clicked", Toast.LENGTH_SHORT).show();
        Log.d("itemisClicked","is called");
    }
}