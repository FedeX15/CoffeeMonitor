package com.fexed.coffeecounter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.ui.adapters.CupRecviewAdapter;

import java.util.Random;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class CupsFragment extends Fragment {
    private RecyclerView cupsRecview;

    public static CupsFragment newInstance(int index) {
        CupsFragment fragment = new CupsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancestate) {
        View root = inflater.inflate(R.layout.activity_cups, container, false);

        cupsRecview = root.findViewById(R.id.cupsrecview);
        cupsRecview.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,
                false));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] funfacts = getResources().getStringArray(R.array.funfacts);
        TextView funfactstxtv = getView().findViewById(R.id.cupsfunfacttxt);
        funfactstxtv.setText(funfacts[new Random().nextInt(funfacts.length)]);
        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, 0));
    }
}
