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
                    callback.onActionSuccess("Flower deleted", documentId);
                })
                .addOnFailureListener(e -> {
                    callback.onActionFailure("Error deleting flower");
                });
    }

    public void updateFavoriteStatus(String documentId, ActionCallback callback) {
        flowerService.updateFavoriteStatus(documentId)
                .addOnSuccessListener(aVoid -> {

                    callback.onActionSuccess("Flower updated", documentId);

                })
                .addOnFailureListener(e -> {
                    callback.onActionFailure("Error updating flower");
                });
    }



    public void countNoneFavoriteFlowers(OnFlowerCountCallback callback) {
        flowerService.countNoneFavoriteFlowers(new FlowerService.OnCountCallback() {
            @Override
            public void onCountReceived(int count) {
                callback.onCountReceived(count);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void countFavoriteFlowers(OnFlowerCountCallback callback) {
        flowerService.countFavoriteFlowers(new FlowerService.OnCountCallback() {
            @Override
            public void onCountReceived(int count) {
                callback.onCountReceived(count);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }



    public interface ActionCallback {
        void onActionSuccess(String message, String documentId);

        void onActionFailure(String error);
    }

    public interface OnFlowerCountCallback {
        void onCountReceived(int count);

        void onError(Exception e);
    }
}

