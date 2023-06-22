package com.malthe.flowertypes.viewmodel;

import com.malthe.flowertypes.data.service.FlowerService;

public class FlowerActionHandler {
    private final FlowerService flowerService;

    public FlowerActionHandler() {
        flowerService = new FlowerService();
    }

    public void deletePlant(String documentId, ActionCallback callback) {
        flowerService.deletePlant(documentId)
                .addOnSuccessListener(aVoid -> {
                    callback.onActionSuccess("Flower deleted");
                })
                .addOnFailureListener(e -> {
                    callback.onActionFailure("Error deleting flower");
                });
    }

    public void updateFavoriteStatus(String documentId, ActionCallback callback) {
        flowerService.updateFavoriteStatus(documentId)
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

