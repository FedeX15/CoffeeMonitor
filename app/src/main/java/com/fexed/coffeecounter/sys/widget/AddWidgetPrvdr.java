package com.fexed.coffeecounter.sys.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.ui.MainActivity;

/**
 * Created by Federico Matteoni on 10/06/2019
 */
public class AddWidgetPrvdr extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("WDGT", "update");
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_addlayout);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.wdgtaddbtn, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
