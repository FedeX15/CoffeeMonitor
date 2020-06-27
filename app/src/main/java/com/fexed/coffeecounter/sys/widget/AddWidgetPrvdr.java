package com.fexed.coffeecounter.sys.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.ui.MainActivity;

/**
 * Created by Federico Matteoni on 10/06/2019
 */
public class AddWidgetPrvdr extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_addlayout);
            Bundle cfg = appWidgetManager.getAppWidgetOptions(id);
            view.setTextViewText(R.id.wdgttxtv, cfg.getString("TYPENAME", "Err"));
            Intent clickIntent = new Intent(context, MainActivity.class);
            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            clickIntent.putExtra("TYPENAME", cfg.getString("TYPENAME", "Err"));
            PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
            view.setOnClickPendingIntent(R.id.wdgtaddbtn, clickPendingIntent);
            appWidgetManager.updateAppWidget(id, view);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
