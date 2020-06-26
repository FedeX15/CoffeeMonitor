package com.fexed.coffeecounter.sys.widget;

import android.app.Activity;
import android.app.PendingIntent;
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
import com.fexed.coffeecounter.ui.MainActivity;

import static com.fexed.coffeecounter.R.id;
import static com.fexed.coffeecounter.R.layout;

/**
 * Created by Federico Matteoni on 11/06/2019
 */
public class AddWidgetCnfg extends Activity {
    Coffeetype coffeetype = null;

    @Override
    public void onCreate(Bundle icile) {
        super.onCreate(icile);
        setContentView(layout.widget_addconfig);
        setResult(RESULT_CANCELED);

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
                int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(AddWidgetCnfg.this);
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_addlayout);
                    views.setTextViewText(R.id.wdgttxtv, coffeetype.getName());
                    Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
                    clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    clickIntent.putExtra("TYPENAME", coffeetype.getName());
                    PendingIntent clickPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_ONE_SHOT);
                    views.setOnClickPendingIntent(id.wdgtaddbtn, clickPendingIntent);
                    appWidgetManager.updateAppWidget(appWidgetId, views);

                    Bundle cfg = new Bundle();
                    cfg.putString("TYPENAME", coffeetype.getName());
                    appWidgetManager.updateAppWidgetOptions(appWidgetId, cfg);

                    Intent result = new Intent();
                    result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, result);
                    finish();
                }
                setResult(RESULT_CANCELED);
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
