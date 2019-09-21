package com.fexed.coffeecounter;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import androidx.room.Room;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Spinner;

public class AddWidgetCnfg extends Activity {

    @Override
    public void onCreate(Bundle icile) {
        super.onCreate(icile);

        View form = LayoutInflater.from(getBaseContext()).inflate(R.layout.addwdgtcnfglayout, null, false);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(getBaseContext());
        Spinner coffetypespinner = form.findViewById(R.id.coffeetypespinner);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb").allowMainThreadQueries().build();
        coffetypespinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, db.coffetypeDao().getAll().toArray()));

        dialogbuilder.setView(form);
        dialogbuilder.create();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
        RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.addwidgetlayout);
    }
}
