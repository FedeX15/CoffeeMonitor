package com.fexed.coffeecounter.sys.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;

import androidx.room.Room;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.db.AppDatabase;

/**
 * Created by Federico Matteoni on 11/06/2019
 */
public class AddWidgetCnfg extends Activity {
    Coffeetype coffeetype = null;

    @Override
    public void onCreate(Bundle icile) {
        super.onCreate(icile);
        setContentView(R.layout.widget_addconfig);

        Spinner coffetypespinner = findViewById(R.id.coffeetypespinner);
        final AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb").allowMainThreadQueries().build();
        coffetypespinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, db.coffetypeDao().getAll().toArray()));

        coffetypespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                coffeetype = db.coffetypeDao().getAll().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button confirm = findViewById(R.id.editcupconfirmbtn);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                if (extras != null)
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
                RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_addlayout);
                views.setString(R.id.wdgttxtv, "setText", coffeetype.getName());
                finish();
            }
        });
        Button cancel = findViewById(R.id.editcupcancelbtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
