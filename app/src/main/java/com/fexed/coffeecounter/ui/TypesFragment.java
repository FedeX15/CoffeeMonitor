package com.fexed.coffeecounter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.ui.adapters.TypeRecviewAdapter;

import java.util.Random;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class TypesFragment extends Fragment {
    private RecyclerView typesRecview;

    public static TypesFragment newInstance() {
        TypesFragment fragment = new TypesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancestate) {
        View root = inflater.inflate(R.layout.activity_types, container, false);

        typesRecview = root.findViewById(R.id.recview);
        typesRecview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        SnapHelper helper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                View centerView = findSnapView(layoutManager);
                if (centerView == null)
                    return RecyclerView.NO_POSITION;

                int position = layoutManager.getPosition(centerView);
                int targetPosition = -1;
                if (layoutManager.canScrollHorizontally()) {
                    if (velocityX < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }
                if (layoutManager.canScrollVertically()) {
                    if (velocityY < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                final int firstItem = 0;
                final int lastItem = layoutManager.getItemCount() - 1;
                targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));
                return targetPosition;
            }
        };
        helper.attachToRecyclerView(typesRecview);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        typesRecview.setAdapter(new TypeRecviewAdapter(MainActivity.db, typesRecview, MainActivity.state));

        String[] funfacts = getResources().getStringArray(R.array.funfacts);
        TextView funfactstxtv = getView().findViewById(R.id.funfacttxt);
        funfactstxtv.setText(funfacts[new Random().nextInt(funfacts.length)]);
    }

    @Override
    public void onStop() {
        super.onStop();
        typesRecview.setAdapter(null);
    }
}
