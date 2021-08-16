package com.example.yourhealth;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {

    private Activity activity;
    private AlertDialog dialog;

    /*
    Custom constructor which set the desired
    activity to show the loading dialog on.
    Input: the activity
     */
    LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    /*
    Starts the loading dialog, shows it in the activity.
    Input: void
    Output: void
     */
    void startLoadingDialog() {
        // Build an Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false); // Only when dismissed on purpose, the dialog will be canceled.

        dialog = builder.create();
        dialog.show();
    }

    /*
    Dismiss the dialog when finished loading.
    Input: void
    Output: void
     */
    void dismissDialog() {
        dialog.dismiss();
    }
}
