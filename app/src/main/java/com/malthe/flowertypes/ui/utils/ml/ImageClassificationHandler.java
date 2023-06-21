package com.malthe.flowertypes.ui.utils.ml;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.data.model.Flower;
import com.malthe.flowertypes.data.repo.FlowerRepository;
import com.malthe.flowertypes.ui.activity.DetailActivity;
import com.malthe.flowertypes.ui.adapter.FlowerListAdapter;
import com.malthe.flowertypes.ui.utils.ImageUtils;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class ImageClassificationHandler implements ImageUtils.ImageClassificationListener {
    private AppCompatActivity activity;

    private double latitude;
    private double longitude;

    private ImageClassifier imageClassifier;

    private FlowerRepository flowerRepository;
    private FlowerListAdapter flowerListAdapter;
    private ImageUtils.ImageClassificationListener imageClassificationListener;

    private List<Flower> flowers = new ArrayList<>();

    private LinearProgressIndicator progressIndicator;


    public ImageClassificationHandler(AppCompatActivity activity, double latitude, double longitude, ImageClassifier imageClassifier, FlowerRepository flowerRepository, FlowerListAdapter flowerListAdapter, LinearProgressIndicator progressIndicator) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageClassifier = imageClassifier;
        this.flowerRepository = flowerRepository;
        this.flowerListAdapter = flowerListAdapter;
        this.progressIndicator = progressIndicator;
    }

    public ImageClassificationHandler(AppCompatActivity activity, double latitude, double longitude, ImageClassifier imageClassifier, FlowerRepository flowerRepository, FlowerListAdapter flowerListAdapter) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageClassifier = imageClassifier;
        this.flowerRepository = flowerRepository;
        this.flowerListAdapter = flowerListAdapter;

    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public void handleImageClassification(Bitmap imageBitmap, Uri imageUri) {

        if (imageBitmap != null) {
            progressIndicator.setVisibility(View.VISIBLE);

            classifyImage(imageBitmap, imageUri);
        }
    }


    private void classifyImage(Bitmap image, Uri imageUri) {
        activity.runOnUiThread(() -> progressIndicator.setVisibility(View.VISIBLE));
        String predictedClass = imageClassifier.classifyImage(image);
        if (predictedClass != null) {
            Flower flower = new Flower(predictedClass);
            flower.setLatitude(latitude);
            flower.setLongitude(longitude);
            flower.setClassificationDate(Timestamp.now());

            flowerRepository.addFlower(flower)
                    .addOnSuccessListener(documentReference -> {
                        flower.setDocumentId(documentReference.getId());

                        Intent intent = new Intent(activity, DetailActivity.class);
                        intent.putExtra("predictedClass", predictedClass);
                        intent.putExtra("imageUri", imageUri != null ? imageUri.toString() : null);
                        intent.putExtra("documentId", documentReference.getId());

                        ImageUtils.uploadImageToFirebaseStorage(activity, predictedClass, image);

                        activity.startActivity(intent);
                        activity.runOnUiThread(() -> progressIndicator.setVisibility(View.GONE));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Error adding flower to Firestore", Toast.LENGTH_SHORT).show();
                        activity.runOnUiThread(() -> progressIndicator.setVisibility(View.GONE));
                    });

        } else {
            Toast.makeText(activity, "Sorry, I could not classify the image", Toast.LENGTH_SHORT).show();
            activity.runOnUiThread(() -> progressIndicator.setVisibility(View.GONE));
        }
    }

    public void openCamera() {
        ImageUtils.openCamera(activity);
    }

    public void openGallery() {
        ImageUtils.openGallery(activity);
    }

    public void setImageClassificationListener(ImageUtils.ImageClassificationListener listener) {
        this.imageClassificationListener = listener;
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ImageUtils.handleActivityResult(activity, requestCode, resultCode, data, this);
    }

    @Override
    public void onImageClassified(Bitmap imageBitmap, Uri imageUri) {

        handleImageClassification(imageBitmap, imageUri);
    }
}
