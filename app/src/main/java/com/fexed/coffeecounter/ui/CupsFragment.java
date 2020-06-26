package com.fexed.coffeecounter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.ui.adapters.CupRecviewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class CupsFragment extends Fragment implements View.OnClickListener {
    private RecyclerView cupsRecview;

    public static CupsFragment newInstance() {
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

        final Button mapbtn = root.findViewById(R.id.gotomapbtn);
        mapbtn.setOnClickListener(this);

        TextView funfactstxtv = root.findViewById(R.id.cupsfunfacttxt);
        funfactstxtv.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] funfacts = getResources().getStringArray(R.array.funfacts);
        TextView funfactstxtv = getView().findViewById(R.id.cupsfunfacttxt);
        funfactstxtv.setText(funfacts[new Random().nextInt(funfacts.length)]);
        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, -1));

        Spinner filterspinner = getView().findViewById(R.id.filtersspinner);
        ArrayList<String> filters = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.filters)));
        for (Coffeetype type : MainActivity.db.coffetypeDao().getAll()) filters.add(type.getName());
        filterspinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, filters.toArray(new String[filters.size()])));
        filterspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, -1));
                        break;
                    case 1:
                        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, -2));
                        break;
                    default:
                        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, position-2));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cupsfunfacttxt:
                break;
            case R.id.gotomapbtn:
                Intent mapiintent = new Intent(getContext(), MapActivity.class);
                startActivity(mapiintent);
                break;
        }
    }
}
