package com.malthe.flowertypes.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.malthe.flowertypes.data.model.Flower;
// Import Date
import java.sql.Timestamp;
import java.util.Date;
import com.malthe.flowertypes.data.repo.FlowerRepository;
import com.malthe.flowertypes.ui.adapter.FlowerListAdapter;
import com.malthe.flowertypes.ui.utils.ml.ImageClassificationHandler;
import com.malthe.flowertypes.ui.utils.ml.ImageClassifier;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.ui.utils.ImageUtils;
import com.malthe.flowertypes.ui.utils.SnackbarUtils;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements ImageUtils.ImageClassificationListener, OnMapReadyCallback, LocationListener {
    private TextView botanicalNameView;
    private TextView plantTypeView;
    private TextView plantHeightView;
    private TextView descriptionView;
    private ImageView imageView;
    private TextView result;
    private TextView dateTextView;
    private BottomAppBar bottomAppBar;

    private ImageClassifier imageClassifier;
    private static final int REQUEST_DETAIL = 2;
    private ArrayList<Flower> classifiedFlowers = new ArrayList<>();
    private RecyclerView recyclerView;

    private Flower currentFlower;
    private FlowerRepository flowerRepository;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private FlowerListAdapter flowerListAdapter;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ImageClassificationHandler imageClassificationHandler;

    private GoogleMap googleMap;

    LinearProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        progressIndicator = findViewById(R.id.progress_circular);
        initializeViews();
        setupBottomAppBar();
        setupRecyclerView();
        setupLogoIcon();
        initializeDependencies();

        handleIntentData();
        initializeLocationManager();
        setupFavoriteAction();
        setupLearnMoreButton();

        mapFragment();
    }


    private void initializeDependencies() {
        flowerRepository = new FlowerRepository();
        imageClassifier = new ImageClassifier(this);
        imageClassificationHandler = new ImageClassificationHandler(this, latitude, longitude, imageClassifier, flowerRepository, flowerListAdapter, progressIndicator);
        imageClassificationHandler.setImageClassificationListener(this);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        imageView = findViewById(R.id.imageView);
        result = findViewById(R.id.result);
        botanicalNameView = findViewById(R.id.botanicalName);
        plantTypeView = findViewById(R.id.plantType);
        plantHeightView = findViewById(R.id.plantHeight);
        descriptionView = findViewById(R.id.description);
        dateTextView = findViewById(R.id.dateTextView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flowerListAdapter = new FlowerListAdapter(this, R.layout.myplants_item_flower);
        recyclerView.setAdapter(flowerListAdapter);
    }

    private void setupBottomAppBar() {
        bottomAppBar = findViewById(R.id.bottom_navigation);

        bottomAppBar.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_gallery) {
                imageClassificationHandler.openGallery();
                return true;
            } else if (id == R.id.action_camera) {
                imageClassificationHandler.openCamera();
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupFavoriteAction() {
        ExtendedFloatingActionButton favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(view -> makeFavorite());
    }

    private void setupLearnMoreButton() {
        Button learnMoreButton = findViewById(R.id.learnMoreButton);
        learnMoreButton.setOnClickListener(v -> openMyPlantsActivity());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logo) {
            Intent intent = new Intent(this, AllFlowersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLogoIcon() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBarDetail);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, AllFlowersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

    }


    private void mapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        String predictedClass = intent.getStringExtra("predictedClass");
        String imageUriString = intent.getStringExtra("imageUri");
        String flowerDocumentId = getIntent().getStringExtra("documentId");

        if (predictedClass != null && imageUriString != null) {
            displayClassificationResult(predictedClass, imageUriString);
            fetchFlowerDetails(flowerDocumentId);
        } else if (flowerDocumentId != null) {
            fetchFlowerDetails(flowerDocumentId);
        }
    }

    private void makeFavorite() {
        if (currentFlower != null) {
            String flowerDocumentId = currentFlower.getDocumentId();

            flowerRepository.updateFlowerToFavorite(flowerDocumentId)
                    .addOnSuccessListener(aVoid -> {
                        LinearLayout snackbarLayout = findViewById(R.id.snackbarLayout);
                        SnackbarUtils.createSnackbar(
                                snackbarLayout,
                                "Flower marked as favorite",
                                "Undo",
                                v -> undoAction(flowerDocumentId)
                        ).setAnchorView(R.id.favoriteButton).show();
                    })
                    .addOnFailureListener(e -> {
                        showErrorMessage("Error marking plant as favorite");
                    });
        }
    }

    private void undoAction(String documentId) {
        LinearLayout snackbarLayout = findViewById(R.id.snackbarLayout);
        flowerRepository.updateFlowerToNotFavorite(documentId)
                .addOnSuccessListener(aVoid -> {
                    showUndoActionToast("Flower marked as not favorite", snackbarLayout);
                })
                .addOnFailureListener(e -> {
                    showUndoActionToast("Error marking plant as not favorite", snackbarLayout);
                });
    }

    private void showUndoActionToast(String message, LinearLayout snackbarLayout) {
        Toast toast = Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, snackbarLayout.getBottom() * 4);
        toast.show();
    }

    private void openMyPlantsActivity() {
        Intent intent = new Intent(DetailActivity.this, MyPlantsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageClassificationHandler.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageClassified(Bitmap imageBitmap, Uri imageUri) {
        imageClassificationHandler.handleImageClassification(imageBitmap, imageUri);
    }
    private void initializeLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            imageClassificationHandler.setLocation(latitude, longitude);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Called when the user enables the location provider (e.g., GPS)
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Called when the user disables the location provider (e.g., GPS)
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Called when the status of the location provider changes
    }

    private void updateFlowerDetails(Flower flower) {
        com.google.firebase.Timestamp classificationDate = flower.getClassificationDate();
        Date date = classificationDate.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy", Locale.getDefault());
        String formattedDate = sdf.format(date);

        dateTextView.setText(formattedDate);
        botanicalNameView.setText(flower.getBotanicalName());
        plantTypeView.setText(flower.getPlantType());
        plantHeightView.setText(flower.getPlantHeight());
        descriptionView.setText(flower.getDescription());
        result.setText(flower.getFlowerName());

        Glide.with(this)
                .load(flower.getImageUrl())
                .into(imageView);
    }

    private void fetchFlowerDetails(String flowerDocumentId) {
        flowerRepository.getFlower(flowerDocumentId)
                .addOnSuccessListener(documentSnapshot -> {
                    Flower flower = documentSnapshot.toObject(Flower.class);
                    if (flower != null) {
                        currentFlower = flower;
                        updateFlowerDetails(flower);

                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(this);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showErrorMessage("Error fetching flower data");
                });
    }

    private void displayClassificationResult(String predictedClass, String imageUriString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(currentDate);
        currentFlower = new Flower(predictedClass);

        Uri imageUri = Uri.parse(imageUriString);
        Glide.with(this)
                .load(imageUri)
                .into(imageView);

        result.setText(predictedClass);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (currentFlower != null) {

            double latitude = currentFlower.getLatitude();
            double longitude = currentFlower.getLongitude();




            LatLng flowerLocation = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions().position(flowerLocation));

            Log.d("myTag", "Flower location: " + flowerLocation);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(flowerLocation, 12f));
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}


