package com.fexed.coffeecounter.sys.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.fexed.coffeecounter.R;

/**
 * Created by Federico Matteoni on 10/06/2019
 */
public class AddWidgetPrvdr extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_addlayout);
            Bundle cfg = appWidgetManager.getAppWidgetOptions(id);
            view.setTextViewText(R.id.wdgttxtv, cfg.getString("TYPENAME", "Err"));
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
