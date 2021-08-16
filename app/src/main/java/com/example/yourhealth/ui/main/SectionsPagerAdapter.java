package com.example.yourhealth.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.yourhealth.ExercisesFragment;
import com.example.yourhealth.GraphsFragment;
import com.example.yourhealth.HistoryFragment;
import com.example.yourhealth.R;
import com.example.yourhealth.TrainingManager;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_exercises, R.string.tab_graphs, R.string.tab_history};
    private final Context mContext;
    private CharSequence selectedExercise;

    private String username;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String username) {
        super(fm);
        mContext = context;
        this.username = username;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = TrainingManager.newInstance(username);
                break;
            case 1:
                fragment = GraphsFragment.newInstance(username);
                break;
            case 2:
                fragment = HistoryFragment.newInstance(username);
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}