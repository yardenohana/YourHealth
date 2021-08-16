package com.example.yourhealth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import org.bson.Document;

import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExercisesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExercisesFragment extends DialogFragment {

    // Specific dialog tag
    public static final String TAG = "SearchExercisesDialog";

    private ProgressBar progressBar;
    private String selectedExercise; // The exercise which has been selected
    private androidx.appcompat.widget.SearchView searchExercises;

    private MongoCommunicator dbCommunicator;
    private ListView listOfExercises;

    ArrayList<String> list;
    ArrayAdapter<String> adapter;

    public interface OnInputSelected {
        void sendInput(String exercise, String details);
    }

    public OnInputSelected mOnInputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exercises, container, false);
        getDialog().setTitle(TAG);
        listOfExercises = (ListView) rootView.findViewById(R.id.ListOfExercises);
        searchExercises = (androidx.appcompat.widget.SearchView) rootView.findViewById(R.id.searchViewExercises);

        // Start a login dialog as we are retrieving data from the db
        progressBar = rootView.findViewById(R.id.progressExercises);

        selectedExercise = "";
        connectDatabase(); // Connect to the MongoDB atlas database
        updateSearch(); // Asynchronously load the exercises from the db

        // Set a listener to catch the item clicking event
        listOfExercises.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String resExercise = (String) adapter.getItem(position);
                String details = dbCommunicator.exercisesCollection.
                        findOne(new Document().append("Name", resExercise)).
                        get().getString("Description");

                // send the exercise selected to the activity
                mOnInputSelected.sendInput(resExercise, details);

                getDialog().dismiss();
            }
        });

        return rootView;
    }

    /*
    Connects to the mongoDB database
    Input: void
    Output: void
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

    // TODO: Rename and change types and number of parameters
    public static ExercisesFragment newInstance(String param1, String param2) {
        ExercisesFragment fragment = new ExercisesFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    /*
    Updates the search view responsible for searching exercises,
    in an asynchronous way, while retrieving data from the database.
    Input: void
    Output: void
     */
    public void updateSearch() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Document> exercisesDocs =  dbCommunicator.getCollectionDocuments(dbCommunicator.exercisesCollection);

                list = new ArrayList<String>();
                for (Document doc : exercisesDocs) {
                    list.add(doc.get("Name").toString());
                }

                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void run() {
                        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);
                        listOfExercises.setAdapter(adapter);

                        // Dismiss the loading interface after finishing
                        progressBar.setVisibility(View.GONE);
                        listOfExercises.setVisibility(View.VISIBLE);

                        // TODO: Get the data after search
                        searchExercises.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                selectedExercise = query; // update the current exercise selected
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                adapter.getFilter().filter(newText);
                                return false;
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /*
    When the fragment is being attached to the activity
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "OnAttach: ClassCastException: " + e.getMessage());
        }
    }
}