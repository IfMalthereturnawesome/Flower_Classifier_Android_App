package com.malthe.flowertypes.ui.utils;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ImageUtils {


    public static final int REQUEST_CAMERA = 3;
    public static final int REQUEST_GALLERY = 1;


    public interface ImageClassificationListener {
        void onImageClassified(Bitmap imageBitmap, Uri imageUri);

    }
    public interface PermissionResultListener {
        void onPermissionGranted();
    }

    public static void openCamera(AppCompatActivity activity, PermissionResultListener listener) {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            if (listener != null) {
                listener.onPermissionGranted();
            }
        }
    }

    public static void openGallery(AppCompatActivity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(galleryIntent, REQUEST_GALLERY);
        } else {
            Toast.makeText(activity, "No suitable apps found to open the gallery.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void handlePermissionsResult(AppCompatActivity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionResultListener listener) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (listener != null) {
                    listener.onPermissionGranted();
                }
            } else {
                Toast.makeText(activity, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public static void uploadImageToFirebaseStorage(AppCompatActivity activity, String predictedClass, Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference flowerImagesRef = storageRef.child("flower_images/" + predictedClass + ".jpg");

        UploadTask uploadTask = flowerImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("TAG", "Upload failed", exception);
                Toast.makeText(activity, "Image upload failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                flowerImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DocumentReference flowerRef = FirebaseFirestore.getInstance()
                                .collection("flowers")
                                .document(predictedClass);
                        flowerRef.update("imageUrl", uri.toString());
                    }
                });
            }
        });
    }

    public static void handleActivityResult(AppCompatActivity activity, int requestCode, int resultCode, @Nullable Intent data, ImageClassificationListener listener) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            Bitmap imageBitmap;
            Uri imageUri = null;
            if (requestCode == REQUEST_CAMERA) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_GALLERY) {
                imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    imageBitmap = null;
                }
            } else {
                return;
            }

            if (listener != null) {
                listener.onImageClassified(imageBitmap, imageUri);

            }
        } else {
            if (listener != null) {
                listener.onImageClassified(null, null);

            }
        }
    }


}
