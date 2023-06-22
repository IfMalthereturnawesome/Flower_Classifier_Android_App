package com.malthe.flowertypes.ui.utils.ml;

import android.content.Context;

import android.graphics.Bitmap;

import com.malthe.flowertypes.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageClassifier {
    private Model model;
    private int modelImageSize = 224;
    private static final float CONFIDENCE_THRESHOLD = 0.7f;
    private float classificationConfidence;

    public ImageClassifier(Context context) {
        try {
            model = Model.newInstance(context.getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String classifyImage(Bitmap image) {
        if (model == null) {
            return null;
        }

        // Creates inputs for reference.
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * modelImageSize * modelImageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[modelImageSize * modelImageSize];
        Bitmap scaledImage = Bitmap.createScaledBitmap(image, modelImageSize, modelImageSize, false);
        scaledImage.getPixels(intValues, 0, scaledImage.getWidth(), 0, 0, scaledImage.getWidth(), scaledImage.getHeight());
        int pixel = 0;
        //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        for (int i = 0; i < modelImageSize; i++) {
            for (int j = 0; j < modelImageSize; j++) {
                int val = intValues[pixel++];

                float r = ((val >> 16) & 0xFF) / 255.0f;
                float g = ((val >> 8) & 0xFF) / 255.0f;
                float b = (val & 0xFF) / 255.0f;

                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }
        inputFeature0.loadBuffer(byteBuffer);

        // Runs model inference and gets result.
        Model.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        float[] confidences = outputFeature0.getFloatArray();
        // find the index of the class with the biggest confidence.
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence && confidences[i] >= CONFIDENCE_THRESHOLD) {
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        if (maxConfidence < CONFIDENCE_THRESHOLD) {
            classificationConfidence = 0;
            return null; // Return null if confidence is below the threshold
        }
        classificationConfidence = maxConfidence;
        String[] classes = {"Lilly", "Lotus", "Orchid", "Sunflower", "Tulip"};
        return classes[maxPos];
    }

    public void close() {
        if (model != null) {
            model.close();
        }
    }

    public float getClassificationConfidence() {
        return classificationConfidence;
    }
}

