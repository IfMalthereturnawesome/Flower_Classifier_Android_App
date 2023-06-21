package com.malthe.flowertypes.ui.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.data.model.Flower;
import com.malthe.flowertypes.data.repo.FlowerRepository;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    private FlowerRepository flowerRepository;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplants_maps);

        flowerRepository = new FlowerRepository();
        setupToolbar();
        mapFragment();
        setupLogoIcon();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.action_logo) {
            Intent intent = new Intent(this, AllFlowersActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLogoIcon(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void loadFavoriteFlowers() {
        flowerRepository.getAllMyPlantsFlowers(new FlowerRepository.OnFlowersFetchedCallback() {
            @Override
            public void onFlowersFetched(List<Flower> flowers) {
                addMarkersOnMap(flowers);
                adjustCameraPosition(flowers);
            }

            @Override
            public void onError(Exception e) {
                Log.e("MapsActivity", "Error fetching favorite flowers", e);
            }
        });
    }

    private void addMarkersOnMap(List<Flower> flowers) {
        // Ensure googleMap object is not null
        if (googleMap == null) {
            Log.e("MapsActivity", "GoogleMap is null");
            return;
        }

        // Clear existing markers
        googleMap.clear();

        for (Flower flower : flowers) {
            double latitude = flower.getLatitude();
            double longitude = flower.getLongitude();

            LatLng flowerLocation = new LatLng(latitude, longitude);

            Marker marker = googleMap.addMarker(new MarkerOptions().position(flowerLocation));
            assert marker != null;
            marker.setTag(flower);


        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the flower object associated with the clicked marker
        Flower flower = (Flower) marker.getTag();
        if (flower != null) {
            // Handle the click event and navigate to the detail activity for that flower
            navigateToDetailActivity(flower);
        }
        return false;
    }

    private void navigateToDetailActivity(Flower flower) {
        Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
        intent.putExtra("documentId", flower.getDocumentId());
        startActivity(intent);
    }

    private void navigateToMyPlantsActivity() {
        Intent intent = new Intent(MapsActivity.this, MyPlantsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            Flower flower = (Flower) marker.getTag();
            if (flower != null) {
                navigateToDetailActivity(flower);
            }
            return false;
        });

        loadFavoriteFlowers();
    }

    private void mapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myPlantsMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void adjustCameraPosition(List<Flower> flowers) {
        // Ensure googleMap object is not null
        if (googleMap == null) {
            Log.e("MyPlantsMapsActivity", "GoogleMap is null");
            return;
        }

        // Create a LatLngBounds.Builder to calculate the bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Flower flower : flowers) {
            double latitude = flower.getLatitude();
            double longitude = flower.getLongitude();

            LatLng flowerLocation = new LatLng(latitude, longitude);

            // Include each flower location in the bounds
            builder.include(flowerLocation);
        }

        // Build the LatLngBounds
        LatLngBounds bounds = builder.build();

        // Calculate padding to ensure all markers are visible with a margin
        int padding = 100;

        // Move the camera to the bounds and apply padding
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.moveCamera(cameraUpdate);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> navigateToMyPlantsActivity());
    }



}
