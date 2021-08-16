package com.example.yourhealth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private static final String CURRENT_USER = "currentUser";

    private MongoCommunicator dbCommunicator;
    private ListView listViewTrainings;

    private String mCurrentUser;

    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] rTitle;
        String[] rDate;
        String[] rDuration;
        String[] rSets;
        String[] rReps;
        String[] rWeight;

        public MyAdapter(@NonNull Context c, String[] title, String[] date, String[] duration, String[] sets, String[] reps, String[] weight) {
            super(c, R.layout.list_item_training, R.id.TrainingExerciseName, title);
            this.context = c;
            this.rTitle = title;
            this.rDate = date;
            this.rDuration = duration;
            this.rSets = sets;
            this.rReps = reps;
            this.rWeight = weight;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.list_item_training, parent, false);

            TextView trainingExercise = (TextView) row.findViewById(R.id.TrainingExerciseName);
            TextView trainingDate = (TextView) row.findViewById(R.id.TrainingDate);
            TextView trainingDuration = (TextView) row.findViewById(R.id.TrainingDuration);
            TextView trainingSets = (TextView) row.findViewById(R.id.TrainingSets);
            TextView trainingReps = (TextView) row.findViewById(R.id.TrainingReps);
            TextView trainingWeight = (TextView) row.findViewById(R.id.TrainingWeight);

            trainingExercise.setText(rTitle[position]);
            trainingDate.setText(rDate[position]);
            trainingDuration.setText(rDuration[position]);
            trainingSets.setText(rSets[position]);
            trainingReps.setText(rReps[position]);
            trainingWeight.setText(rWeight[position]);

            return row;
        }
    }

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser the user name.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String currentUser) {
        HistoryFragment fragment = new HistoryFragment();
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

    /**
     * Connects to the database & Realm backend.
     */
    private void connectDatabase() {
        // Avoid network errors
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // DB initialization
        dbCommunicator = new MongoCommunicator();
        dbCommunicator.InitActivity(getContext()); // get context

        try {
            dbCommunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed. @exercise_fragment");
            Toast.makeText(getContext(), "ERROR: can't connect to database. @exercise_fragment", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The method updates the list view asynchronously with the trainings'
     * description, and updates the list view.
     */
    private void updateExercisesList() {
        // Start a loading dialog as we retrieve data from the database
        final LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startLoadingDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Get the exercises collection documents from the database
                ArrayList<Document> exercisesDocs =  dbCommunicator.getTrainings(mCurrentUser);

                // Declare Arrays of exercises names, and descriptions
                ArrayList<String> exercisesNames = new ArrayList<>();
                ArrayList<String> exercisesDates = new ArrayList<>();
                ArrayList<String> exercisesDurations = new ArrayList<>();
                ArrayList<String> exercisesSets = new ArrayList<>();
                ArrayList<String> exercisesReps = new ArrayList<>();
                ArrayList<String> exercisesWeights = new ArrayList<>();

                for (Document doc : exercisesDocs) {
                    exercisesNames.add(doc.get("Exercise").toString());
                    exercisesDates.add(doc.get("Date").toString());
                    exercisesDurations.add(doc.get("Duration").toString());
                    exercisesSets.add(doc.get("Sets").toString());
                    exercisesReps.add(doc.get("Repeats").toString());
                    exercisesWeights.add(doc.get("Weight").toString());
                }

                // Set the arrays size to match rhe retrieved data
                String[] exercisesNamesArray = new String[exercisesNames.size()];
                String[] exercisesDatesArray = new String[exercisesNames.size()];
                String[] exercisesDurationsArray = new String[exercisesNames.size()];
                String[] exercisesSetsArray = new String[exercisesNames.size()];
                String[] exercisesRepsArray = new String[exercisesNames.size()];
                String[] exercisesWeightArray = new String[exercisesNames.size()];

                exercisesNames.toArray(exercisesNamesArray);
                exercisesDates.toArray(exercisesDatesArray);
                exercisesDurations.toArray(exercisesDurationsArray);
                exercisesSets.toArray(exercisesSetsArray);
                exercisesReps.toArray(exercisesRepsArray);
                exercisesWeights.toArray(exercisesWeightArray);

                // Set a custom adapter which uses the custom layout
                HistoryFragment.MyAdapter myAdapter = new HistoryFragment.MyAdapter(getContext(), exercisesNamesArray,
                        exercisesDatesArray, exercisesDurationsArray, exercisesSetsArray, exercisesRepsArray, exercisesWeightArray);

                // Change UI components asynchronously
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listViewTrainings.setAdapter(myAdapter); // set the data into the adapter

                        // Set the list to be visible immediately after
                        // finished retrieving data from the database
                        listViewTrainings.setVisibility(View.VISIBLE);
                        loadingDialog.dismissDialog(); // dismiss loading
                    }
                });
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_history, container, false);

        listViewTrainings = (ListView) view.findViewById(R.id.listOfTrainings);

        connectDatabase();
        updateExercisesList();

        return view;
    }
}