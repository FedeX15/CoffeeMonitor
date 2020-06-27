package com.fexed.coffeecounter.sys;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveImageToInternalTask extends AsyncTask<Bitmap, Void, String> {
    private Context context;

    public SaveImageToInternalTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        Bitmap bitmapImage = bitmaps[0];
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        File mypath = new File(directory, bitmapImage.hashCode() + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + bitmapImage.hashCode() + ".png";
    }

    @Override
    protected void onPreExecute() {
    }
}
