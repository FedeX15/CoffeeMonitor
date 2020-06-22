package com.fexed.coffeecounter.ui.main;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.fexed.coffeecounter.AppDatabase;
import com.fexed.coffeecounter.MainActivity;
import com.fexed.coffeecounter.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
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
                return StatFragment.newInstance(position + 1);
            case 1:
                return TypesFragment.newInstance(position + 1);
            case 2:
                return CupsFragment.newInstance(position + 1);
            case 3:
                return PrefFragment.newInstance(position + 1);
            default:
                return StatFragment.newInstance(-1);
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