package com.malthe.flowertypes.ui.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    public static Snackbar createSnackbar(View view, String message, String actionText, View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);

        if (actionText != null && action != null) {
            snackbar.setAction(actionText, action);
        }

        return snackbar;
    }
}
