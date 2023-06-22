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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.data.enums.ActivityOrigin;
import com.malthe.flowertypes.data.enums.FlowerFilter;
import com.malthe.flowertypes.data.model.Flower;
import com.malthe.flowertypes.data.service.FlowerService;
import com.malthe.flowertypes.ui.adapter.FlowerListAdapter;
import com.malthe.flowertypes.ui.utils.ImageUtils;
import com.malthe.flowertypes.ui.utils.ml.ImageClassificationHandler;
import com.malthe.flowertypes.ui.utils.ml.ImageClassifier;
import com.malthe.flowertypes.viewmodel.FlowerActionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class AllFlowersActivity extends AppCompatActivity implements ImageUtils.ImageClassificationListener, FlowerActionHandler.ActionCallback, LocationListener {


    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private FlowerListAdapter flowerListAdapter;

    private RecyclerView recyclerViewNotYetSaved;
    private RecyclerView recyclerViewSaved;
    private FlowerListAdapter flowerListAdapterNotYetSaved;
    private FlowerListAdapter flowerListAdapterSaved;
    private FlowerService flowerService;
    private ImageClassifier imageClassifier;
    private LinearLayout placeholderLayout;
    private FlowerActionHandler flowerActionHandler;
    private final FlowerFilter notMyPlants = FlowerFilter.NOT_MY_PLANTS;

    private final FlowerFilter myPlants = FlowerFilter.MY_PLANTS;
    private ImageClassificationHandler imageClassificationHandler;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private int size;

    private int sizeFavoriteFlowers;

    LinearLayoutManager horizontalLayoutSaved;

    LinearLayoutManager horizontalLayoutNotYetSaved;

    private final ActivityOrigin seeSnapFlowers = ActivityOrigin.SEE_SNAP_FLOWERS;
    private final ActivityOrigin seeAllMyFlowers = ActivityOrigin.SEE_ALL_MY_PLANTS;
    LinearProgressIndicator progressIndicator;


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merged_flowers);

        progressIndicator = findViewById(R.id.progress_circular);
        initializeDependencies();
        initializeViews();
        initializeLocationManager();
        setupViews();
        loadInitialData();


    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, update your UI here
            loadInitialData();
        } else {
            // User is not signed in, you might want to show sign-in UI here
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build()
            );

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();

            signInLauncher.launch(signInIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        flowerListAdapterNotYetSaved.loadFlowers(notMyPlants);
        flowerListAdapterSaved.loadFlowers(myPlants);
        getSizeOfFlowers();
        getSizeOfFavoriteFlowers();
        setupPlaceholder();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // User is signed in, call onActionSuccess
                onActionSuccess("Successfully signed in");
                loadInitialData();
            } else {
                // User is null, sign-in failed, call onActionFailure
                onActionFailure("Sign-in failed");
            }
        } else {
            // Sign-in failed, call onActionFailure
            if (response != null) {
                onActionFailure("Sign-in error: " + Objects.requireNonNull(response.getError()).getLocalizedMessage());
            } else {
                onActionFailure("Sign-in failed");
            }
        }
    }



    private void initializeDependencies() {
        flowerActionHandler = new FlowerActionHandler();
        flowerService = new FlowerService();
        imageClassifier = new ImageClassifier(this);
        imageClassificationHandler = new ImageClassificationHandler(this, latitude, longitude, imageClassifier, flowerService, flowerListAdapter, progressIndicator);
        imageClassificationHandler.setImageClassificationListener(this);
    }

    private void initializeViews() {
        recyclerViewSaved = findViewById(R.id.recyclerViewSaved);
        recyclerViewNotYetSaved = findViewById(R.id.recyclerViewNotYetSaved);
        placeholderLayout = findViewById(R.id.placeholderLayout);
    }

    private void setupViews() {
        setupRecyclerViews();

        setupFabCamera();
        setupBottomAppBar();
        setupFlowerListAdapter();
        setupFlowerListAdapterNotSaved();
        setupSeeAllSnapFlowers();
        setUpSeeAllMyPlants();
        setupToolbar();
        setupLogoIcon();

    }

    private void loadInitialData() {
        flowerListAdapterNotYetSaved.loadFlowers(notMyPlants);
        flowerListAdapterSaved.loadFlowers(myPlants);
        getSizeOfFlowers();
        getSizeOfFavoriteFlowers();
    }

    private void setupRecyclerViews() {
        recyclerViewSaved.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotYetSaved.setLayoutManager(new LinearLayoutManager(this));

        flowerListAdapterSaved = new FlowerListAdapter(this, R.layout.myplants_item_flower, seeAllMyFlowers);
        flowerListAdapterNotYetSaved = new FlowerListAdapter(this, R.layout.myplants_item_flower, seeSnapFlowers);

        horizontalLayoutSaved = new LinearLayoutManager(AllFlowersActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutNotYetSaved = new LinearLayoutManager(AllFlowersActivity.this, LinearLayoutManager.HORIZONTAL, false);

        recyclerViewSaved.setLayoutManager(horizontalLayoutSaved);
        recyclerViewNotYetSaved.setLayoutManager(horizontalLayoutNotYetSaved);

        recyclerViewSaved.setAdapter(flowerListAdapterSaved);
        recyclerViewNotYetSaved.setAdapter(flowerListAdapterNotYetSaved);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.action_logo) {
            Intent intent = new Intent(this, AllFlowersActivity.class);

            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.signout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {

                        // Sign-out completed, handle UI updates or navigate to sign-in screen
                        Intent intent = new Intent(this, LauncherActivity.class);
                        startActivity(intent);
                        finish();
                    });

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLogoIcon() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

    }


    private void setupFabCamera() {
        FloatingActionButton fabCamera = findViewById(R.id.action_camera);
        fabCamera.setOnClickListener(view -> imageClassificationHandler.openCamera());
    }

    private void setupSeeAllSnapFlowers() {
        Button seeAllSnapFlowers = findViewById(R.id.seeAllNotYetSaved);
        seeAllSnapFlowers.setOnClickListener(v -> navigateToSeeSnapFlowersActivity());
    }

    private void setUpSeeAllMyPlants() {
        Button seeAllSnapFlowers = findViewById(R.id.seeAllSaved);
        seeAllSnapFlowers.setOnClickListener(v -> navigateToSeeAllMyPlantsActivity());
    }

    private void setupToolbar() {
        Button learnMoreButton = findViewById(R.id.learnMoreButton);
        learnMoreButton.setOnClickListener(v -> openMyPlantsActivity());
    }


    private void setupBottomAppBar() {
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
            } else if (id == R.id.action_camera) {
                imageClassificationHandler.openCamera();
                return true;
            }
            return false;
        });
    }

    private void setupFlowerListAdapter() {
        flowerListAdapterSaved.setOnItemClickListener(new FlowerListAdapter.OnItemClickListener() {
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

    private void setupFlowerListAdapterNotSaved() {
        flowerListAdapterNotYetSaved.setOnItemClickListener(new FlowerListAdapter.OnItemClickListener() {
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

    private void deletePlant(String documentId) {
        flowerActionHandler.deletePlant(documentId, this);
    }

    private void updateFavoriteStatus(String documentId) {
        flowerActionHandler.updateFavoriteStatus(documentId, this);
    }


    private void navigateToSeeAllMyPlantsActivity() {
        Intent intent = new Intent(AllFlowersActivity.this, SeeAllMyPlantsActivity.class);
        startActivity(intent);
        finish();
    }

    private void openMyPlantsActivity() {
        Intent intent = new Intent(AllFlowersActivity.this, MyPlantsActivity.class);
        startActivity(intent);
        finish();
    }


    private void navigateToSeeSnapFlowersActivity() {
        Intent intent = new Intent(AllFlowersActivity.this, SeeSnapFlowersActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDetailActivity(Flower flower) {
        Intent intent = new Intent(AllFlowersActivity.this, DetailActivity.class);
        intent.putExtra("documentId", flower.getDocumentId());
        startActivity(intent);
    }

    private void navigateToMapsActivity() {
        Intent intent = new Intent(AllFlowersActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void getSizeOfFlowers() {
        flowerService.countNoneFavoriteFlowers(new FlowerService.OnCountCallback() {
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

    private void getSizeOfFavoriteFlowers() {
        flowerService.countFavoriteFlowers(new FlowerService.OnCountCallback() {
            @Override
            public void onCountReceived(int count) {
                sizeFavoriteFlowers = count;
                setupPlaceholder();
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }

    private void setupPlaceholder() {
        if (size == 0 && sizeFavoriteFlowers == 0) {
            placeholderLayout.setVisibility(View.VISIBLE);

        } else {
            placeholderLayout.setVisibility(View.GONE);
            placeholderHelper();

        }
    }

    private void placeholderHelper() {
        TextView savedText = findViewById(R.id.mySavedFlowersTextView);
        TextView notSavedText = findViewById(R.id.flowersNotYetSavedTextView);
        MaterialButton savedButton = findViewById(R.id.seeAllSaved);
        MaterialButton notSavedButton = findViewById(R.id.seeAllNotYetSaved);

        if (size == 0) {
            notSavedText.setVisibility(View.GONE);
            notSavedButton.setVisibility(View.GONE);
        } else {
            notSavedText.setVisibility(View.VISIBLE);
            notSavedButton.setVisibility(View.VISIBLE);
        }
        if (sizeFavoriteFlowers == 0) {
            savedText.setVisibility(View.GONE);
            savedButton.setVisibility(View.GONE);
        } else {
            savedText.setVisibility(View.VISIBLE);
            savedButton.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, initialize location manager
                initializeLocationManager();
                ImageUtils.handlePermissionsResult(this, requestCode, permissions, grantResults, () -> ImageUtils.openCamera(AllFlowersActivity.this));
            } else {
                // Permissions denied, handle accordingly
                onActionFailure("Location permission denied");
            }
        }

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


    @Override
    public void onActionSuccess(String message) {

        Toast toast = Toast.makeText(AllFlowersActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();
        flowerListAdapterNotYetSaved.loadFlowers(notMyPlants);
        flowerListAdapterSaved.loadFlowers(myPlants);
        getSizeOfFlowers();
        getSizeOfFavoriteFlowers();
    }

    @Override
    public void onActionFailure(String error) {
        Toast toast = Toast.makeText(AllFlowersActivity.this, error, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();
    }

}
