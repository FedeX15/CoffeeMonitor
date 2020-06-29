package com.fexed.coffeecounter.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        switch (api.isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS:
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(this, "Google Play Services Missing", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(this, "Update Google Play Services", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, api.isGooglePlayServicesAvailable(this), Toast.LENGTH_SHORT).show();
        }
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