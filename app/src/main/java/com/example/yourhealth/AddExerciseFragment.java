package com.example.yourhealth;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddExerciseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddExerciseFragment extends DialogFragment {

    public static final String TAG = "AddExerciseDialog";
    public OnExerciseSelected mOnExerciseSelected;

    // Interface used to create a listener to pass the data.
    public interface OnExerciseSelected {

        /**
         * When exercise is being selected, the callback is being called
         * and used to pass the new exercise to the database communicator.
         * @param name the name of the exercise
         * @param details the details of the exercise
         */
        void sendInput(String name, String details);
    }

    public AddExerciseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment AddExercise.
     */
    // TODO: Rename and change types and number of parameters
    public static AddExerciseFragment newInstance(String param1, String param2) {
        AddExerciseFragment fragment = new AddExerciseFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);

        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add_exercise, container, false);
        getDialog().setTitle(TAG);

        EditText exerciseName = (EditText) view.findViewById(R.id.ExerciseNameInput);
        EditText exerciseDetail = (EditText) view.findViewById(R.id.ExerciseDetailInput);
        TextView errorPrompt = (TextView) view.findViewById(R.id.SubmitExerciseError);
        Button addExerciseButton = (Button) view.findViewById(R.id.SubmitNewExercise);

        addExerciseButton.setBackground(getActivity().getDrawable(R.drawable.buttons_design));

        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = exerciseName.getText().toString();
                String details = exerciseDetail.getText().toString();

                if (InputValidator.doesSomeFieldsEmpty(new TextView[]{exerciseName, exerciseDetail})) {
                    errorPrompt.setText("Please fill in all fields.");
                } else if (!InputValidator.isLettersOnly(name) ||
                           !InputValidator.isLettersOnly(details)) {
                    errorPrompt.setText("All fields have to consist only of letters.");
                } else {
                    // send the data to the underlying fragment
                    mOnExerciseSelected.sendInput(name, details);
                    dismiss();
                }
            }
        });

        return view;
    }

    /*
    Called When the fragment is being attached to the activity.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnExerciseSelected = (OnExerciseSelected) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "OnAttach: ClassCastException: " + e.getMessage());
        }
    }
}