package com.example.yourhealth;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrainingManager#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrainingManager extends Fragment implements ExercisesFragment.OnInputSelected, chronometerActivity.OnTrainingFinished {

    public static final String TAG = "TrainingManagerFragment";
    public static final String CURRENT_USER = "currentUser";

    public String currentUser;
    public EditText selectedExercise;
    public TextView selectedExerciseDetails;
    String selectedExerciseText;

    // database
    MongoCommunicator dbCommunicator;

    boolean hasExerciseSelected = false; // used to check if any exercise been selected
    LinearLayout resultsLayout; // result of training layout
    TextView finishedTime; // finished training time
    Button startTraining;

    // weight sets and reps
    EditText weight;
    EditText repeats;
    EditText sets;

    TextView startTrainingError; // error prompt

    // Callback, called when the exercise is selected.
    // Displays the selected exercise.
    @Override
    public void sendInput(String exercise, String details) {
        // Set the selected exercise as the text shown
        selectedExercise.setHint((CharSequence) exercise);

        // set the description of the exercise
        selectedExerciseDetails.setText((CharSequence) details);

        hasExerciseSelected = true;

        selectedExerciseText = exercise;
    }

    // Callback, called when the training ends.
    // reveals to the user the time took for the training,
    // and finally sends the data to the database.
    @Override
    public void sendTrainingInfo(String duration) {
        // finish music service
        getContext().stopService(new Intent(getContext(), MyService.class));

        finishedTime.setText(duration);
        resultsLayout.setVisibility(View.VISIBLE);
        finishedTime.setText(duration);

        // Send the data to the database

        initializeDatabase();
        String user = getArguments().getString(CURRENT_USER);

        int setsInt = Integer.parseInt(sets.getText().toString());
        int repsInt = Integer.parseInt(repeats.getText().toString());
        int weightInt = Integer.parseInt(weight.getText().toString());

        // Async update
        new Thread(() -> {
            // TODO: fix selected exercise
            dbCommunicator.addNewTraining(user, selectedExerciseText,
                    getCurrentDate(), duration, setsInt, repsInt, weightInt);
        }).start();
    }

    /*
    Initializes the database and sets a message in accordance when error occurs.
    Input: void
    Output: void
    */
    private void initializeDatabase() {
        dbCommunicator = new MongoCommunicator();
        dbCommunicator.InitActivity(getContext()); // get context

        try {
            dbCommunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed");
            Toast.makeText(getContext(), "ERROR: can't connect to database", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static TrainingManager newInstance(String currentUser) {
        TrainingManager fragment = new TrainingManager();
        Bundle args = new Bundle();
        args.putString(CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_training_manager, container, false);

        // results layout
        resultsLayout = (LinearLayout) view.findViewById(R.id.resultTraining);
        finishedTime = (TextView) view.findViewById(R.id.finishedTime);

        // start training button
        startTraining = (Button) view.findViewById(R.id.startTraining);
        startTraining.setBackground(getActivity().getDrawable(R.drawable.buttons_design));

        // select exercise button
        selectedExercise = (EditText) view.findViewById(R.id.selectExerciseText);
        selectedExercise.setHint("Select exercise");
        selectedExerciseDetails = (TextView) view.findViewById(R.id.exerciseDetailText);

        // Weight, sets, reps variables
        weight = (EditText) view.findViewById(R.id.weightTraining);
        sets = (EditText) view.findViewById(R.id.setsTraining);
        repeats = (EditText) view.findViewById(R.id.repsTraining);

        // error prompt
        startTrainingError = (TextView) view.findViewById(R.id.startTrainingError);

        selectedExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedExerciseText = selectedExercise.getText().toString();

                // Show the search exercise fragment dialog
                ExercisesFragment dialog = new ExercisesFragment();
                dialog.setTargetFragment(TrainingManager.this, 1);
                dialog.show(getActivity().getSupportFragmentManager(), ExercisesFragment.TAG);
            }
        });

        // start training
        startTraining.setOnClickListener(v -> {
            // Start music service
            Intent intent = new Intent(getContext(), MyService.class);
            getContext().startService(intent);

            // first check exercise has been selected,
            // and number of sets & reps is logical
            String repsAsStr = repeats.getText().toString();
            String setsAsStr = sets.getText().toString();

            if (repsAsStr.equals("0") || setsAsStr.equals("0")) {
                startTrainingError.setText("Select your desired repeats and sets.");
                return;
            } else if (!hasExerciseSelected) {
                startTrainingError.setText("No exercise has been selected.");
                return;
            }

            // Show the search exercise fragment dialog
            chronometerActivity dialog = new chronometerActivity();
            dialog.setTargetFragment(TrainingManager.this, 5);
            dialog.setCancelable(false);
            dialog.show(getActivity().getSupportFragmentManager(), chronometerActivity.TAG);
        });

        return view;
    }

    /*
    Gets the current date as a string.
    Input: void
    Output: the date
     */
    private String getCurrentDate() {
        Calendar calender = Calendar.getInstance();
        return DateFormat.getDateInstance(DateFormat.FULL).format(calender.getTime());
    }
}