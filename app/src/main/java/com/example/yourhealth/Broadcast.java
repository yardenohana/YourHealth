package com.example.yourhealth;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class Broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String IntentAction = intent.getAction();
        if (IntentAction != null) {
            String finalMsg = "";

            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                finalMsg = "Boot completed";
                myAlert(context, finalMsg, "Boot has just completed!");
            }
            else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                finalMsg = "Boot completed";
                myAlert(context, finalMsg, "Power connected");
            }
            else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                finalMsg = "Boot completed";
                myAlert(context, finalMsg, "Power disconnected");
            }
            else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                finalMsg = "Battery is low";
                myAlert(context, finalMsg, "Attention! your battery is running low, you " +
                                                    "won't be able to practice sports.");
            }

            if (!finalMsg.isEmpty()) {
                Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void myAlert(Context context, String msg, String detail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(msg);
        builder.setMessage(detail);
        builder.setCancelable(false);
        builder.setNeutralButton("Ok", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create();
        builder.show();
    }
}

