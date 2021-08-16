package com.example.yourhealth;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.mongo.result.InsertOneResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExercisesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExercisesListFragment extends Fragment implements AddExerciseFragment.OnExerciseSelected {

    private MongoCommunicator dbCommunicator;
    private ListView listViewExercises;

    // Get the data from the add exercise dialog, and insert it
    // to the database. Finally updating the list in accordance.
    @Override
    public void sendInput(String name, String details) {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startLoadingDialog();

        // Insert exercise into the database
        RealmResultTask<InsertOneResult> resultTask = dbCommunicator.exercisesCollection.insertOne(new Document()
                .append("Name", name)
                .append("Description", details)
        );

        InsertOneResult result = resultTask.get();

        // finally dismiss loading dialog
        loadingDialog.dismissDialog();

        updateExercisesList(); // after insertion, update the list view
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String rTitle[];
        String rDescription[];
        int rButton;

        public MyAdapter(@NonNull Context c, String title[], String description[]) {
            super(c, R.layout.list_item_exercise, R.id.ExerciseName, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.list_item_exercise, parent, false);

            ImageButton imageButton = (ImageButton) row.findViewById(R.id.deleteExercise);
            TextView exerciseName = (TextView) row.findViewById(R.id.ExerciseName);
            TextView exerciseDetails = (TextView) row.findViewById(R.id.ExerciseDetail);

            exerciseName.setText(rTitle[position]);
            exerciseDetails.setText(rDescription[position]);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedExercise = exerciseName.getText().toString();
                    Log.v("Delete exercise", selectedExercise);

                    // Delete the exercise from the database
                    boolean successDelete = dbCommunicator.deleteExercise(selectedExercise);
                    if (successDelete) {
                        // if succeed in deleting, remove the item from the list view
                        updateExercisesList();

                    } else {
                        Toast.makeText(getContext(),
                                "Failed: Item not found, please try again",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });

            return row;
        }
    }

    public ExercisesListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters if provided.
     * @return A new instance of fragment ExercisesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExercisesListFragment newInstance() {
        ExercisesListFragment fragment = new ExercisesListFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
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
        View view =  inflater.inflate(R.layout.fragment_exercises_list, container, false);

        Button addExercise = (Button) view.findViewById(R.id.AddExerciseBtn);
        listViewExercises = (ListView) view.findViewById(R.id.listOfExpandedExercises);

        addExercise.setBackground(getActivity().getDrawable(R.drawable.buttons_design));

        connectDatabase();
        updateExercisesList();

        // Add new exercise to the list
        addExercise.setOnClickListener(v -> {
            // Create a dialog to add exercise
            AddExerciseFragment dialog = new AddExerciseFragment();
            dialog.setTargetFragment(ExercisesListFragment.this, 2);
            dialog.show(getActivity().getSupportFragmentManager(), AddExerciseFragment.TAG);
        });

        return view;
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
     * The method updates the list view asynchronously with the exercises'
     * names and descriptions, and updates the list view.
     */
    private void updateExercisesList() {
        // Start a loading dialog as we retrieve data from the database
        final LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startLoadingDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Get the exercises collection documents from the database
                ArrayList<Document> exercisesDocs =  dbCommunicator.getCollectionDocuments(dbCommunicator.exercisesCollection);

                // Declare Arrays of exercises names, and descriptions
                ArrayList<String> exercisesNames = new ArrayList<>();
                ArrayList<String> exercisesDetails = new ArrayList<>();

                for (Document doc : exercisesDocs) {
                    exercisesNames.add(doc.get("Name").toString());
                    exercisesDetails.add(doc.get("Description").toString());
                }

                // Set the arrays size to match rhe retrieved data
                String[] exercisesNamesArray = new String[exercisesNames.size()];
                String[] exercisesDetailsArray = new String[exercisesNames.size()];

                exercisesNames.toArray(exercisesNamesArray);
                exercisesDetails.toArray(exercisesDetailsArray);

                // Set a custom adapter which uses the custom layout
                MyAdapter myAdapter = new MyAdapter(getContext(), exercisesNamesArray, exercisesDetailsArray);

                // Change UI components asynchronously
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listViewExercises.setAdapter(myAdapter); // set the data into the adapter

                        // Set the list to be visible immediately after
                        // finished retrieving data from the database
                        listViewExercises.setVisibility(View.VISIBLE);
                        loadingDialog.dismissDialog(); // dismiss loading
                    }
                });
            }
        }).start();
    }
}