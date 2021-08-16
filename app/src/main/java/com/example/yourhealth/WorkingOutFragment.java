package com.example.yourhealth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.yourhealth.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkingOutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkingOutFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CURRENT_USER = "current_user";

    private View myFragment;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private String mCurrentUser;

    public WorkingOutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser Parameter 1.
     * @return A new instance of fragment WorkingOutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WorkingOutFragment newInstance(String currentUser) {
        WorkingOutFragment fragment = new WorkingOutFragment();
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

    private void setUpViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getContext(), getFragmentManager(), mCurrentUser);
        viewPager.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_workout, container, false);

        viewPager = myFragment.findViewById(R.id.view_pager);
        tabLayout = myFragment.findViewById(R.id.tabs);

        // set up the view pager
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return myFragment;
    }
}