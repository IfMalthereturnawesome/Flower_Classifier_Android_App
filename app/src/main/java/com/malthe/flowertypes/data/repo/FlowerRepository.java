package com.malthe.flowertypes.data.repo;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.malthe.flowertypes.data.model.Flower;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FlowerRepository {

    private static final String COLLECTION_MY_PLANTS = "MyPlants";
    private static final String FIELD_FLOWER = "flower";
    private final FirebaseFirestore db;
    private final CollectionReference myPlantsCollection;

    public FlowerRepository() {
        db = FirebaseFirestore.getInstance();
        myPlantsCollection = db.collection(COLLECTION_MY_PLANTS);
    }

    public interface OnFlowersFetchedCallback {
        void onFlowersFetched(List<Flower> flowers);

        void onError(Exception e);
    }

    public interface OnCountCallback {
        void onCountReceived(int count);
        void onError(Exception e);
    }


    public void fetchFlowerDetails(String flowerName, OnSuccessListener<DocumentSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        DocumentReference documentRef = db.collection("flowers").document(flowerName);
        documentRef.get().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    public Task<Void> updatePlant(String documentId, String flowerDocumentId) {
        DocumentReference plantRef = myPlantsCollection.document(documentId);
        DocumentReference flowerRef = db.collection("flowers").document(flowerDocumentId);

        // Update the "flower" field with the new flower reference
        return plantRef.update(FIELD_FLOWER, flowerRef);
    }

    public void countNoneFavoriteFlowers(OnCountCallback callback) {
        CollectionReference myPlantsCollection = FirebaseFirestore.getInstance().collection("MyPlants");

        Query query = myPlantsCollection.whereEqualTo("favorite", false);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    callback.onCountReceived(count);
                })
                .addOnFailureListener(callback::onError);
    }

    public void countFavoriteFlowers(OnCountCallback callback) {
        CollectionReference myPlantsCollection = FirebaseFirestore.getInstance().collection("MyPlants");

        Query query = myPlantsCollection.whereEqualTo("favorite", true);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    callback.onCountReceived(count);
                })
                .addOnFailureListener(callback::onError);
    }

    public Task<DocumentReference> addFlower(Flower flower) {
        DocumentReference flowerRef = db.collection("flowers").document(flower.getFlowerName());

        // Fetch the flower data first
        return flowerRef.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the data of the document
                    Map<String, Object> flowerData = document.getData();
                    if (flowerData != null) {
                        // Add the flowerName field
                        flowerData.put("flowerName", flower.getFlowerName());
                        flowerData.put("favorite", false);
                        flowerData.put("longitude", flower.getLongitude());
                        flowerData.put("latitude", flower.getLatitude());
                        flowerData.put("classificationDate", flower.getClassificationDate());


                        // Add the plant data and get the newly created document reference
                        return myPlantsCollection.add(flowerData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Set the Firestore document ID to the flower
                                        flower.setDocumentId(documentReference.getId());

                                        // Update the document in Firestore with the documentId
                                        documentReference.update("documentId", documentReference.getId());
                                    }
                                });
                    } else {
                        throw new IllegalArgumentException("No data in document");
                    }
                } else {
                    throw new IllegalArgumentException("No such document");
                }
            } else {
                throw Objects.requireNonNull(task.getException());
            }
        });
    }

    public Task<DocumentSnapshot> getFlower(String flowerDocumentId) {
        DocumentReference flowerRef = myPlantsCollection.document(flowerDocumentId);
        return flowerRef.get();
    }

    public Task<Void> updateFavoriteStatus(String flowerDocumentId) {
        DocumentReference flowerRef = myPlantsCollection.document(flowerDocumentId);

        return flowerRef.get().continueWithTask(task -> {
            DocumentSnapshot snapshot = task.getResult();

            if (snapshot.exists()) {
                boolean currentFavoriteStatus = Boolean.TRUE.equals(snapshot.getBoolean("favorite"));
                boolean newFavoriteStatus = !currentFavoriteStatus; // Invert the value

                return flowerRef.update("favorite", newFavoriteStatus);
            }

            return null;
        });
    }



    public Task<Void> updateFlowerToFavorite(String flowerDocumentId) {
        DocumentReference flowerRef = myPlantsCollection.document(flowerDocumentId);
        // Updating the favorite field to true
        return flowerRef.update("favorite", true);
    }


    public Task<Void> updateFlowerToNotFavorite(String flowerDocumentId) {
        DocumentReference flowerRef = myPlantsCollection.document(flowerDocumentId);
        return flowerRef.update("favorite", false);
    }

    public void getAllMyPlantsFlowers(OnFlowersFetchedCallback callback) {
        db.collection("MyPlants")
                .whereEqualTo("favorite", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Flower> flowers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Flower flower = document.toObject(Flower.class);
                            flower.setDocumentId(document.getId());

                            flowers.add(flower);
                        }
                        callback.onFlowersFetched(flowers);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void getAllNoneMyPlantsFlowers(OnFlowersFetchedCallback callback) {
        db.collection("MyPlants")
                .whereEqualTo("favorite", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Flower> flowers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Flower flower = document.toObject(Flower.class);
                            flower.setDocumentId(document.getId());

                            flowers.add(flower);
                        }
                        callback.onFlowersFetched(flowers);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }


    public Task<Void> deletePlant(String documentId) {
        DocumentReference plantRef = myPlantsCollection.document(documentId);

        // Delete the document from the "MyPlants" collection
        return plantRef.delete();
    }

    public void getAllFlowers(OnFlowersFetchedCallback callback) {
        db.collection("MyPlants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Flower> flowers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Flower flower = document.toObject(Flower.class);
                            flower.setDocumentId(document.getId());

                            flowers.add(flower);
                        }
                        callback.onFlowersFetched(flowers);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }


}
