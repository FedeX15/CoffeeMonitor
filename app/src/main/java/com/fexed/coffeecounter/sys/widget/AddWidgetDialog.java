package com.fexed.coffeecounter.sys.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.fexed.coffeecounter.db.DBAccess;

import java.util.List;

public class AddWidgetDialog extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("TYPENAME", null) != null ) {
            DBAccess db = new DBAccess(getApplication());
            String typename = bundle.getString("TYPENAME", null);
            Log.d("WDGT", typename);
            try {
                List<Coffeetype> list = db.getTypes().get();
                for (Coffeetype type : list) {
                    if (type.getName().equals(typename)) {
                        Cup cup = new Cup(type.getKey());
                        cup = geoTag(cup);
                        db.insertCup(cup);
                        Toast.makeText(this, getString(R.string.added, type.getName()), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public Cup geoTag(Cup cup) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setCostAllowed(false);
            String provider = locationManager.getBestProvider(criteria, true);
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                cup.setLongitude(location.getLongitude());
                cup.setLatitude(location.getLatitude());
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(this, R.string.locationerror, Toast.LENGTH_SHORT).show();
            }
            return cup;
        }
        return cup;
    }
}
