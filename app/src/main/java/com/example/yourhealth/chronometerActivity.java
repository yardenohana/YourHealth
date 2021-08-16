package com.example.yourhealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;

public class chronometerActivity extends DialogFragment {
    public static final String TAG = "SearchExercisesDialog";

    private Chronometer chronometer;
    private long pauseOffset;
    private boolean isRunning; // does the chronometer run

    // End the training button
    public Button endTraining;

    // When training finished, send the data to the underlying fragment
    public interface OnTrainingFinished {
        void sendTrainingInfo(String duration);
    }

    public chronometerActivity.OnTrainingFinished mOnTrainingFinished;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_chronometer, container, false);

        Button startChrono = (Button) rootView.findViewById(R.id.startChrono);
        Button pauseChrono = (Button) rootView.findViewById(R.id.pauseChrono);
        Button resetChrono = (Button) rootView.findViewById(R.id.resetChrono);
        endTraining = (Button) rootView.findViewById(R.id.EndTraining);

        // set buttons design
        startChrono.setBackground(getActivity().getDrawable(R.drawable.buttons_design));
        pauseChrono.setBackground(getActivity().getDrawable(R.drawable.buttons_design));
        resetChrono.setBackground(getActivity().getDrawable(R.drawable.buttons_design));
        endTraining.setBackground(getActivity().getDrawable(R.drawable.buttons_design_pink));

        chronometer = (Chronometer) rootView.findViewById(R.id.my_chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        startChrono.setOnClickListener(v -> {
            startChronometer();
        });

        pauseChrono.setOnClickListener(v -> {
            pauseChronometer();
        });

        resetChrono.setOnClickListener(v -> {
            resetChronometer();
        });

        // End training button listener
        endTraining.setOnClickListener(v -> {

            // duration in seconds
            int totalSecs =  (((int) (SystemClock.elapsedRealtime() - chronometer.getBase())) / 1000);

            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;

            // represent the duration as a string
            @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            // send the data to the underlying fragment and dismiss the dialog
            mOnTrainingFinished.sendTrainingInfo(timeString);
            dismiss();
        });

        return rootView;
    }

    /*
    Start counting time using the chronometer.
     */
    public void startChronometer() {
        if (!isRunning) {
            // By default the chronometer starts counting from the start of the app
            // so we set the base to be from the current time, and on
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);

            chronometer.start();
            isRunning = true;
        }
    }

    /*
    Pause the chronometer, and save the pause offset.
     */
    public void pauseChronometer() {
        if (isRunning) {
            chronometer.stop();

            // the offset is basically the current time minus
            // the time which we have started counting
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isRunning = false;
        }
    }

    /*
    Reset the chronometer back to 0.
     */
    public void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }


    // When the fragment is being attached to the activity.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnTrainingFinished = (chronometerActivity.OnTrainingFinished) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "OnAttach: ClassCastException: " + e.getMessage());
        }
    }
}