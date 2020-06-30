package com.fexed.coffeecounter.db;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Default database downloader
 * Created by Federico Matteoni on 17/06/2020
 */
public class DBDownloader extends AsyncTask<String, Void, String> {
    private SharedPreferences state;

    public DBDownloader(SharedPreferences state) {
        this.state = state;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {
        int count;
        try {
            URL url = new URL(urls[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            int fileLen = connection.getContentLength();
            long lastmod = connection.getLastModified();
            if (state.getLong("dblastmodified", -1) < lastmod) {
                state.edit().putLong("dblastmodified", lastmod).apply();

                InputStream istream = new BufferedInputStream(url.openStream(), 8192);
                StringBuffer strbuff = new StringBuffer();
                byte[] data = new byte[1024];

                while ((count = istream.read(data)) != -1) {
                    strbuff.append(new String(data, 0, count));
                }

                istream.close();
                String defaultdb = new String(strbuff);
                state.edit().putString("defaultdb", defaultdb).apply();
                return defaultdb;
            } else { //Database Already updated
                return null;
            }
        } catch (MalformedURLException ex) {
            Log.e("URL", urls[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
