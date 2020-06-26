package com.fexed.coffeecounter.sys.widget;

import android.app.Activity;
import android.os.Bundle;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;

/**
 * Created by Federico Matteoni on 11/06/2019
 */
public class AddWidgetCnfg extends Activity {
    Coffeetype coffeetype = null;

    @Override
    public void onCreate(Bundle icile) {
        super.onCreate(icile);
        setContentView(R.layout.widget_addconfig);
    }
}
