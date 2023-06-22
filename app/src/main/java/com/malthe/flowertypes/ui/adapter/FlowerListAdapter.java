package com.malthe.flowertypes.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.malthe.flowertypes.R;
import com.malthe.flowertypes.data.enums.ActivityOrigin;
import com.malthe.flowertypes.data.model.Flower;
import com.malthe.flowertypes.data.service.FlowerService;
import com.malthe.flowertypes.data.enums.FlowerFilter;

import java.util.ArrayList;
import java.util.List;


public class FlowerListAdapter extends RecyclerView.Adapter<FlowerListAdapter.FlowerViewHolder> {

    private List<Flower> flowers;
    private FlowerService flowerService;
    private Context context;
    private int layoutId;
    private OnItemClickListener onItemClickListener;
    private ActivityOrigin activityOrigin;

    public FlowerListAdapter(Context context, int layoutId) {
        this.flowers = new ArrayList<>();
        this.flowerService = new FlowerService();
        this.context = context;
        this.layoutId = layoutId;
    }

    public FlowerListAdapter(Context context, int layoutId,  ActivityOrigin activityOrigin) {
        this.flowers = new ArrayList<>();
        this.flowerService = new FlowerService();
        this.context = context;
        this.layoutId = layoutId;
        this.activityOrigin = activityOrigin;
    }


    public void loadFlowers(FlowerFilter filter) {
        FlowerService.OnFlowersFetchedCallback callback = new FlowerService.OnFlowersFetchedCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFlowersFetched(List<Flower> fetchedFlowers) {
                flowers.clear();
                flowers.addAll(fetchedFlowers);
                notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                showError("Error fetching flowers: " + e.getMessage());
            }
        };

        if (filter == FlowerFilter.MY_PLANTS) {
            flowerService.getAllMyPlantsFlowers(callback);
        } else {
            flowerService.getAllNoneMyPlantsFlowers(callback);
        }
    }



    private void showError(String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public FlowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new FlowerViewHolder(view, onItemClickListener);
    }


    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    @Override
    public void onBindViewHolder(@NonNull FlowerViewHolder holder, int position) {
        Flower flower = flowers.get(position);
        holder.bind(flower);

        ConstraintLayout small = holder.itemView.findViewById(R.id.constraintLayout);
        ImageView smallImage = holder.itemView.findViewById(R.id.flowerImageView);


        if (small != null && smallImage != null) {
            ViewGroup.LayoutParams params = small.getLayoutParams();
            ViewGroup.LayoutParams params2 = smallImage.getLayoutParams();

            if (layoutId == R.layout.myplants_item_flower) {
                if (activityOrigin == ActivityOrigin.SEE_ALL_MY_PLANTS) {
                    // Apply saved flower layout changes
                    params.width = dpToPx(278);
                    params2.height = dpToPx(180);
                } else {
                    // Apply not-yet-saved flower layout changes
                    params.width = dpToPx(135);
                    params2.height = dpToPx(155);
                }
            }

            small.setLayoutParams(params);
            smallImage.setLayoutParams(params2);
        }

        if (activityOrigin == ActivityOrigin.SEE_ALL_MY_PLANTS) {
            holder.updateButton.setIcon(ContextCompat.getDrawable(context, R.drawable.heart_minus));
        }

    }




    @Override
    public int getItemCount() {
        return flowers.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Flower flower);
        void onDeleteClick(String documentId);
        void onUpdateClick(String documentId);
    }

    public class FlowerViewHolder extends RecyclerView.ViewHolder {
        private TextView flowerTypeTextView;
        private TextView botanicalNameTextView;
        private TextView plantTypeTextView;
        private TextView plantHeightTextView;
        private ImageView flowerImageView;
        private MaterialButton deleteButton;
        private MaterialButton updateButton;

        public FlowerViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            initializeViews(itemView);
            setupListeners(listener);
        }

        private void initializeViews(View itemView) {
            deleteButton = itemView.findViewById(R.id.deleteButton);
            updateButton = itemView.findViewById(R.id.updateButton);
            flowerTypeTextView = itemView.findViewById(R.id.flowerTypeTextView);

            botanicalNameTextView = itemView.findViewById(R.id.botanicalNameTextView);
            plantTypeTextView = itemView.findViewById(R.id.plantTypeTextView);
            plantHeightTextView = itemView.findViewById(R.id.plantHeightTextView);
            flowerImageView = itemView.findViewById(R.id.flowerImageView);
        }

        private void setupListeners(OnItemClickListener listener) {
            deleteButton.setOnClickListener(v -> handleDeleteClick(listener));
            itemView.setOnClickListener(v -> handleItemClick(listener));
            updateButton.setOnClickListener(v -> handleUpdateClick(listener));
        }

        private void handleDeleteClick(OnItemClickListener listener) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Flower clickedFlower = flowers.get(position);
                listener.onDeleteClick(clickedFlower.getDocumentId());
            }
        }
        private void handleItemClick(OnItemClickListener listener) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Flower clickedFlower = flowers.get(position);
                listener.onItemClick(clickedFlower);
            }
        }

        private void handleUpdateClick(OnItemClickListener listener) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Flower clickedFlower = flowers.get(position);
                listener.onUpdateClick(clickedFlower.getDocumentId());
            }
        }

        public void bind(Flower flower) {
            flowerTypeTextView.setText(flower.getFlowerName());

            if (botanicalNameTextView != null) {
                botanicalNameTextView.setText(flower.getBotanicalName() != null ? flower.getBotanicalName() : "N/A");
            }

            if (plantTypeTextView != null) {
                plantTypeTextView.setText(flower.getPlantType() != null ? flower.getPlantType() : "N/A");
            }

            if (plantHeightTextView != null) {
                plantHeightTextView.setText(flower.getPlantHeight() != null ? flower.getPlantHeight() : "N/A");
            }

            // Load image from Firestore as a URL string
            if (flower.getImageUrl() != null) {
                Glide.with(context)
                        .load(flower.getImageUrl())
                        .into(flowerImageView);
            } else {
                flowerImageView.setVisibility(View.GONE);
            }
        }




    }
}