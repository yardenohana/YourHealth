package com.example.yourhealth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphsFragment extends Fragment {

    private static final String CURRENT_USER = "currentUser";
    private String mCurrentUser;

    public GraphsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser Parameter 1.
     * @return A new instance of fragment GraphsFragment.
     */
    public static GraphsFragment newInstance(String currentUser) {
        GraphsFragment fragment = new GraphsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_graphs, container, false);

        final GraphView graph = (GraphView) view.findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        graph.setTitle("Your Journey");

        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Days");
        gridLabel.setVerticalAxisTitle("Weight (Kg)");

        try {
            LineGraphSeries <DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                    new DataPoint(0, 78),
                    new DataPoint(5, 77.5),
                    new DataPoint(10, 77.2),
                    new DataPoint(15, 76.7)
            });
            graph.addSeries(series);

        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }
}