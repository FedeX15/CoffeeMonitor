package com.fexed.coffeecounter.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.db.AppDatabase;

/**
 * A FragmentPagerAdapter that returns a fragment corresponding to
 * one of the tabs.
 *
 * Created by Federico Matteoni on 22/06/2020
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.title_statistics,
            R.string.title_dashboard,
            R.string.title_cups,
            R.string.title_preferences};
    private final Context context;
    private final SharedPreferences state;
    private final AppDatabase db;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        this.state = MainActivity.state;
        this.db = MainActivity.db;

    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StatFragment.newInstance();
            case 1:
                return TypesFragment.newInstance();
            case 2:
                return CupsFragment.newInstance();
            case 3:
                return PrefFragment.newInstance();
            default:
                return StatFragment.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 4;
    }
}