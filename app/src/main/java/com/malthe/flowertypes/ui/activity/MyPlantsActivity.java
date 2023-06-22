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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.malthe.flowertypes.data.enums.ActivityOrigin;
import com.malthe.flowertypes.data.model.Flower;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.data.service.FlowerService;
import com.malthe.flowertypes.ui.utils.ImageUtils;
import com.malthe.flowertypes.ui.utils.SnackbarUtils;
import com.malthe.flowertypes.ui.utils.ml.ImageClassificationHandler;
import com.malthe.flowertypes.ui.utils.ml.ImageClassifier;
import com.malthe.flowertypes.viewmodel.FlowerActionHandler;
import com.malthe.flowertypes.data.enums.FlowerFilter;
import com.malthe.flowertypes.ui.adapter.FlowerListAdapter;

public class MyPlantsActivity extends AppCompatActivity implements FlowerActionHandler.ActionCallback, ImageUtils.ImageClassificationListener, LocationListener {
    private RecyclerView recyclerView;
    private FlowerListAdapter flowerListAdapter;
    private final FlowerFilter myPlants = FlowerFilter.MY_PLANTS;
    private ImageClassificationHandler imageClassificationHandler;
    private LocationManager locationManager;
    private FlowerActionHandler flowerActionHandler;
    private double latitude;
    private double longitude;
    private FlowerService flowerService;
    private ImageClassifier imageClassifier;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private LinearLayout placeholderLayout;
    LinearLayoutManager HorizontalLayout;
    private int size;
    LinearProgressIndicator progressIndicator;
    private final ActivityOrigin seeAllMyFlowers = ActivityOrigin.SEE_ALL_MY_PLANTS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplants);
        progressIndicator = findViewById(R.id.progress_circular);
        initializeViews();
        setupViews();

        initializeDependencies();

        initializeLocationManager();
        setupClickListener();
        loadFlowers();

    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.myPlantsRecyclerView);
        placeholderLayout = findViewById(R.id.placeholderLayout);
    }

    private void setupViews() {
        setupRecyclerView();
        setupToolbar();
        setUpBottomAppBar();
        setUpMySnapsButton();
        setupFabCamera();
        setUpSeeAllMyPlants();
        setupLogoIcon();
    }

    private void initializeDependencies() {
        flowerActionHandler = new FlowerActionHandler();
        flowerService = new FlowerService();
        imageClassifier = new ImageClassifier(this);
        imageClassificationHandler = new ImageClassificationHandler(this, latitude, longitude, imageClassifier, flowerService, flowerListAdapter, progressIndicator);
        imageClassificationHandler.setImageClassificationListener(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flowerListAdapter = new FlowerListAdapter(this, R.layout.myplants_item_flower,seeAllMyFlowers);
        HorizontalLayout
                = new LinearLayoutManager(
                MyPlantsActivity.this,
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(flowerListAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(view -> navigateToMySnapsHistory());
    }

    private void setUpSeeAllMyPlants() {
        Button seeAllSnapFlowers = findViewById(R.id.seeAll);
        seeAllSnapFlowers.setOnClickListener(v -> navigateToSeeAllMyPlantsActivity());
    }

    private void setUpBottomAppBar() {
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_navigation);
        bottomAppBar.setNavigationOnClickListener(view -> {
            // Handle navigation icon press
        });

        bottomAppBar.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.mapsButton) {
                navigateToMapsActivity();
                return true;
            } else if (id == R.id.action_gallery) {
                imageClassificationHandler.openGallery();
                return true;
            }
            return false;
        });
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

    private void setupLogoIcon() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, AllFlowersActivity.class);
            startActivity(intent);
        });
    }


    private void setUpMySnapsButton() {
        Button mySnapsButton = findViewById(R.id.mySnaps);
        mySnapsButton.setOnClickListener(v -> navigateToMySnapsHistory());
    }


    private void setupFabCamera() {
        FloatingActionButton fabCamera = findViewById(R.id.action_camera);
        fabCamera.setOnClickListener(view -> imageClassificationHandler.openCamera());
    }

    private void setupClickListener() {
        flowerListAdapter.setOnItemClickListener(new FlowerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Flower flower) {
                navigateToDetailActivity(flower);
            }

            @Override
            public void onDeleteClick(String documentId) {
                deletePlant(documentId);
            }

            @Override
            public void onUpdateClick(String documentId) {
                updateFavoriteStatus(documentId);
            }
        });
    }

    private void loadFlowers() {
        flowerListAdapter.loadFlowers(myPlants);
    }

    private void navigateToDetailActivity(Flower flower) {
        Intent intent = new Intent(MyPlantsActivity.this, DetailActivity.class);
        intent.putExtra("documentId", flower.getDocumentId());
        startActivity(intent);
    }

    private void navigateToMapsActivity() {
        Intent intent = new Intent(MyPlantsActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void navigateToMySnapsHistory() {
        Intent intent = new Intent(MyPlantsActivity.this, SnapPlantsActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToSeeAllMyPlantsActivity() {
        Intent intent = new Intent(MyPlantsActivity.this, SeeAllMyPlantsActivity.class);
        startActivity(intent);
        finish();
    }


    private void deletePlant(String documentId) {
        flowerActionHandler.deletePlant(documentId, this);
    }

    private void updateFavoriteStatus(String documentId) {
        flowerActionHandler.updateFavoriteStatus(documentId, this);
    }

    private void reloadFlowers() {
        flowerListAdapter.loadFlowers(myPlants);
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
            // Update the ImageClassificationHandler with the new latitude and longitude
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


    private void getSizeOfFlowers() {
        flowerActionHandler.countFavoriteFlowers(new FlowerActionHandler.OnFlowerCountCallback() {
            @Override
            public void onCountReceived(int count) {
                size = count;
                setupPlaceholder();
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }

    private void setupPlaceholder() {
        if (size == 0) {
            placeholderLayout.setVisibility(View.VISIBLE);

        } else {
            placeholderLayout.setVisibility(View.GONE);

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        flowerListAdapter.loadFlowers(myPlants);
        getSizeOfFlowers();
    }


    private void showUndoActionToast(String message, LinearLayout snackbarLayout) {
        Toast toast = Toast.makeText(MyPlantsActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();
    }


    private void undoAction(String documentId) {
        LinearLayout snackbarLayout = findViewById(R.id.snackbarLayout);
        flowerService.updateFavoriteStatus(documentId)
                .addOnSuccessListener(aVoid -> {
                    showUndoActionToast("Flower marked as not favorite", snackbarLayout);
                    flowerListAdapter.loadFlowers(myPlants);

                })
                .addOnFailureListener(e -> {
                    onActionFailure("Error marking plant as not favorite");
                });
    }

    @Override
    public void onActionSuccess(String message, String documentId) {
        if (message.contains("Flower deleted")) {
            Toast toast = Toast.makeText(MyPlantsActivity.this, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
            toast.show();
        } else {
            LinearLayout snackbarLayout = findViewById(R.id.snackbarLayout);
            SnackbarUtils.createSnackbar(
                    snackbarLayout,
                    message,
                    "Undo",
                    v -> undoAction(documentId)
            ).setAnchorView(R.id.action_camera).show();
            reloadFlowers();
        }
        flowerListAdapter.loadFlowers(myPlants);

    }

    @Override
    public void onActionFailure(String error) {
        Toast toast = Toast.makeText(MyPlantsActivity.this, error, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();
    }
}