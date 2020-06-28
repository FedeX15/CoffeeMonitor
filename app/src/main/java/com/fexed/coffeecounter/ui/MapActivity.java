package com.fexed.coffeecounter.ui;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by Federico Matteoni on 24/06/2020
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<Coffeetype> types;
        try {
            types = MainActivity.db.getTypes().get();
            for (Coffeetype type : types) {
                List<Cup> typecups = MainActivity.db.getCups(type.getKey()).get();
                for (Cup cup : typecups) {
                    if (cup.getLongitude() != 0.0) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(cup.getLatitude(), cup.getLongitude())).title(type.getName() + " " + cup.toString()));
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}