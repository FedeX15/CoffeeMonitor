package com.fexed.coffeecounter.ui;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.fexed.coffeecounter.R;

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

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
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
            default:
                return PrefFragment.newInstance();
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