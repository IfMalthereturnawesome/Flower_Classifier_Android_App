package com.malthe.flowertypes.viewmodel;

import com.malthe.flowertypes.data.repo.FlowerRepository;

public class FlowerActionHandler {
    private final FlowerRepository flowerRepository;

    public FlowerActionHandler() {
        flowerRepository = new FlowerRepository();
    }

    public void deletePlant(String documentId, ActionCallback callback) {
        flowerRepository.deletePlant(documentId)
                .addOnSuccessListener(aVoid -> {
                    callback.onActionSuccess("Flower deleted");
                })
                .addOnFailureListener(e -> {
                    callback.onActionFailure("Error deleting flower");
                });
    }

    public void updateFavoriteStatus(String documentId, ActionCallback callback) {
        flowerRepository.updateFavoriteStatus(documentId)
                .addOnSuccessListener(aVoid -> {
                    callback.onActionSuccess("Flower updated");
                })
                .addOnFailureListener(e -> {
                    callback.onActionFailure("Error updating flower");
                });
    }

    public interface ActionCallback {
        void onActionSuccess(String message);

        void onActionFailure(String error);
    }
}

