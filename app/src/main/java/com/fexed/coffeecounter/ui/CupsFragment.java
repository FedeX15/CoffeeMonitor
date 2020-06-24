package com.fexed.coffeecounter.ui;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        final FloatingActionButton mapfab = root.findViewById(R.id.gotomapfab);
        mapfab.setOnClickListener(this);

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
        cupsRecview.setAdapter(new CupRecviewAdapter(MainActivity.db, 0));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cupsfunfacttxt:
                FloatingActionButton mapfab = getView().findViewById(R.id.gotomapfab);
                if (mapfab.getVisibility() == View.VISIBLE) mapfab.hide();
                else mapfab.show();
                break;
            case R.id.gotomapfab:
                Intent mapiintent = new Intent(getContext(), MapActivity.class);
                startActivity(mapiintent);
                break;
        }
    }
}
