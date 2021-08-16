package com.example.yourhealth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeightFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeightFragment extends Fragment {

    private static final String CURRENT_USER = "currentUser";
    private String mCurrentUser;

    private ImageButton updateWeight;
    private ImageButton updateHeight;
    private ImageButton bmiInfo;

    private MongoCommunicator mongo_cummunicator;

    public WeightFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser Parameter 1.
     * @return A new instance of fragment WeightFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeightFragment newInstance(String currentUser) {
        WeightFragment fragment = new WeightFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentUser = getArguments().getString(CURRENT_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_weight, container, false);

        // Avoid network errors
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initializeDatabase();

        updateWeight = (ImageButton) view.findViewById(R.id.updateWeight);
        updateHeight = (ImageButton) view.findViewById(R.id.updateHeight);
        bmiInfo = (ImageButton) view.findViewById(R.id.bmi);

        updateWeight.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Weight update");
            builder.setView(getView());

            builder.setPositiveButton(android.R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
                dialog.dismiss();
                //m_Text = input.getText().toString();
            });

            builder.show();
        });

        return view;
    }

    /**
     * Calculates the bmi.
     * @param height the person's height in cm (ex: 175)
     * @param weight the person's weight in kg (ex: 80)
     * @return the BMI.
     */
    private static double calcBMI(double height, double weight) {
        return weight / (height * height);
    }

    /*
    Initializes the database and sets a message in accordance when error occurs.
    Input: void
    Output: void
     */
    private void initializeDatabase() {
        // DB initialization
        mongo_cummunicator = new MongoCommunicator();
        mongo_cummunicator.InitActivity(getContext());

        try {
            mongo_cummunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed");
            Toast.makeText(getContext(), "ERROR: can't connect to database", Toast.LENGTH_SHORT).show();
        }
    }
}