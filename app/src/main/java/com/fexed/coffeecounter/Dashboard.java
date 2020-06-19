package com.fexed.coffeecounter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Room;
import androidx.room.migration.Migration;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.androidplot.pie.PieRenderer;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonOutsideTouchListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Dashboard extends AppCompatActivity {
    public SharedPreferences state;
    public SharedPreferences.Editor editor;
    public AppDatabase db;
    public RecyclerView typesRecview;
    public RecyclerView cupsRecview;
    public ImageView currentimageview;
    public Bitmap currentbitmap;

    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("CREATE TABLE IF NOT EXISTS `coffeetypenew` (`qnt` INTEGER NOT NULL, `liters` INTEGER NOT NULL, `name` TEXT, `desc` TEXT, `key` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `liquido` INTEGER NOT NULL, `sostanza` TEXT, `fav` INTEGER NOT NULL, `price` REAL NOT NULL)");
            database.execSQL("INSERT INTO coffeetypenew('qnt', 'liters', 'name', 'desc', 'key', 'liquido', 'sostanza', 'fav', 'price') SELECT * FROM coffeetype");
            database.execSQL("DROP TABLE coffeetype");
            database.execSQL("ALTER TABLE coffeetypenew RENAME TO coffeetype");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE coffeetype ADD COLUMN img TEXT");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_22_23 = new Migration(22, 23) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE cup ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE cup ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_23_24 = new Migration(23, 24) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE coffeetype ADD COLUMN defaulttype INTEGER NOT NULL DEFAULT 0");
            database.execSQL("COMMIT");
        }
    };

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    private void updateDefaultDatabase() {
        try {
            Locale locale = Locale.getDefault();
            String dbtxt;
            if (locale.getLanguage().equals("it")) dbtxt = new DBDownloader(state).execute("https://fexed.github.io/db/it/defaultcoffeetypes").get();
            else dbtxt = new DBDownloader(state).execute("https://fexed.github.io/db/en/defaultcoffeetypes").get();

            if (dbtxt != null) {
                if (db.coffetypeDao().getAll().size() == 0) {
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        db.coffetypeDao().insert(new Coffeetype(strtype[0], Integer.parseInt(strtype[2]), strtype[1], Boolean.parseBoolean(strtype[3]), strtype[4], Float.parseFloat(strtype[5]), null, true));
                    }
                    Snackbar.make(findViewById(R.id.viewflipper), R.string.dbupdated, Snackbar.LENGTH_SHORT).show();
                } else {
                    List<Coffeetype> coffeelist = db.coffetypeDao().getAll();
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        Coffeetype type = null;
                        for (Coffeetype listtype : coffeelist) {
                            if (listtype.getName().equals(strtype[0])) {
                                type = listtype; break;
                            }
                        }
                        if (type != null && type.isDefaulttype()) {
                            type.setLiters(Integer.parseInt(strtype[2]));
                            type.setDesc(strtype[1]);
                            type.setLiquido(Boolean.parseBoolean(strtype[3]));
                            type.setSostanza(strtype[4]);
                            type.setPrice(Float.parseFloat(strtype[5]));
                            db.coffetypeDao().update(type);
                        }
                    }
                    Snackbar.make(findViewById(R.id.viewflipper), R.string.dbupdated, Snackbar.LENGTH_SHORT).show();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.viewflipper), R.string.dbupdatefailed, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void adInitializer() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdView mAdView = findViewById(R.id.banner1);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                AdView mAdView2 = findViewById(R.id.banner2);
                adRequest = new AdRequest.Builder().build();
                mAdView2.loadAd(adRequest);
                AdView mAdView3 = findViewById(R.id.banner3);
                adRequest = new AdRequest.Builder().build();
                mAdView3.loadAd(adRequest);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favs:
                PopupMenu popup = new PopupMenu(findViewById(R.id.action_add).getContext(), findViewById(R.id.action_add));
                final List<Coffeetype> list = db.coffetypeDao().getFavs();
                for (Coffeetype type : list)
                    popup.getMenu().add(1, list.indexOf(type), 0, type.getName());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int pos = item.getItemId();
                        Coffeetype elem = list.get(pos);
                        elem.setQnt(elem.getQnt() + 1);
                        db.coffetypeDao().update(elem);
                        db.cupDAO().insert(new Cup(elem.getKey()));
                        graphUpdater();
                        typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
                        cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                        return true;
                    }
                });
                popup.show();
                break;
            case R.id.action_notifs: //TODO rework
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.tips));
                builder.setIcon(R.drawable.ic_info);
                builder.setMessage(generateTip());
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create();
                builder.show();
                break;

            case R.id.action_add:
                ViewFlipper vf = findViewById(R.id.viewflipper);
                if (vf.getDisplayedChild() == 1) addNewType();
                else addCup();
                break;
        }
        return true;
    }

    public String generateTip() {
        int maxCupsPerDay = 5;
        //int maxCaffeinePerDay = 400;
        int cupsToday = db.cupDAO().getAll(getStringFromLocalDate(Calendar.getInstance().getTime())).size();
        String tip = getString(R.string.tipsplaceholder);

        if (cupsToday > maxCupsPerDay)
            tip = getString(R.string.toomuchcupstip);
        else {
            String[] funfacts = getResources().getStringArray(R.array.funfacts);
            Random rnd = new Random();
            int i = rnd.nextInt(funfacts.length);
            tip = funfacts[i];
        }

        return tip;
    }

    public void addNewType() {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(findViewById(R.id.action_favs).getContext());
        final View form = getLayoutInflater().inflate(R.layout.addtypedialog, null);
        final TextView literstxt = form.findViewById(R.id.ltrsmgtext);
        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);
        final ImageView typeimage = form.findViewById(R.id.typeimage);

        editor.putInt("qnt", 0);
        editor.putString("suffix", (liquidckbx.isChecked()) ? " ml" : " mg");
        editor.commit();
        String str = state.getInt("qnt", 0) + state.getString("suffix", " ml");
        literstxt.setText(str);

        ImageButton addbtn = form.findViewById(R.id.incrbtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qnt = state.getInt("qnt", 0);
                qnt += 5;
                editor.putInt("qnt", qnt);
                editor.commit();
                String str = qnt + state.getString("suffix", " ml");
                literstxt.setText(str);
            }
        });

        ImageButton rmvbtn = form.findViewById(R.id.decrbtn);
        rmvbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qnt = state.getInt("qnt", 0);
                qnt = (qnt == 0) ? 0 : qnt - 5;
                editor.putInt("qnt", qnt);
                editor.commit();
                String str = qnt + state.getString("suffix", " ml");
                literstxt.setText(str);
            }
        });

        liquidckbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putString("suffix", (isChecked) ? " ml" : " mg");
                editor.commit();
                String str = state.getInt("qnt", 0) + state.getString("suffix", " ml");
                literstxt.setText(str);
            }
        });

        typeimage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                currentbitmap = null;
                currentimageview = typeimage;
                startActivityForResult(i, 9);
                return true;
            }
        });

        dialogbuilder.setView(form);
        dialogbuilder.create();
        final AlertDialog dialog = dialogbuilder.show();

        Button positive = form.findViewById(R.id.confirmbtn);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameedittxt = form.findViewById(R.id.nametxt);
                EditText descedittxt = form.findViewById(R.id.desctxt);
                EditText sostedittxt = form.findViewById(R.id.sosttxt);
                EditText pricetedittxt = form.findViewById(R.id.pricetxt);
                CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

                String name = nameedittxt.getText().toString();
                if (name.isEmpty()) {
                    final Balloon balloon = new Balloon.Builder(Dashboard.this)
                            .setText(Dashboard.this.getString(R.string.nameemptyalert))
                            .setBackgroundColorResource(R.color.colorAccent)
                            .setWidthRatio(0.75f)
                            .setBalloonAnimation(BalloonAnimation.FADE)
                            .setArrowVisible(true)
                            .setArrowOrientation(ArrowOrientation.TOP)
                            .build();
                    balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                        @Override
                        public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                            balloon.dismiss();
                        }
                    });
                    balloon.showAlignBottom(nameedittxt);
                } else {
                    int liters = state.getInt("qnt", 0);
                    String desc = descedittxt.getText().toString();
                    String sostanza = sostedittxt.getText().toString();
                    float price = Float.parseFloat(pricetedittxt.getText().toString());
                    boolean liquid = liquidckbx.isChecked();
                    String bmpuri = "";
                    if (currentbitmap != null) {
                        bmpuri = saveToInternalStorage(currentbitmap);
                        currentbitmap = null;
                    }
                    Coffeetype newtype = new Coffeetype(name, liters, desc, liquid, sostanza, price, bmpuri);

                    db.coffetypeDao().insert(newtype);

                    cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                    typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));

                    dialog.dismiss();
                }
            }
        });

        Button negative = form.findViewById(R.id.cancelbtn);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ImageButton fromdefaultbtn = form.findViewById(R.id.defaultbtn);
        fromdefaultbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dbtxt = state.getString("defaultdb", null);
                final ArrayList<Coffeetype> defaultlist = new ArrayList<>();
                ArrayList<String> defaultindb = new ArrayList<>();
                for (Coffeetype type : db.coffetypeDao().getAll()) { if (type.isDefaulttype()) defaultindb.add(type.getName()); }
                ArrayList<String> namelist = new ArrayList<>();
                if (dbtxt != null) {
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        if (strtype.length == 6) {
                            Coffeetype type = new Coffeetype(strtype[0], Integer.parseInt(strtype[2]), strtype[1], Boolean.parseBoolean(strtype[3]), strtype[4], Float.parseFloat(strtype[5]), null, true);
                            if (!defaultindb.contains(type.getName())) {
                                defaultlist.add(type);
                                namelist.add(strtype[0]);
                            }
                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
                    builder.setTitle(R.string.defaultdbtitle);
                    builder.setItems(namelist.toArray(new CharSequence[namelist.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.coffetypeDao().insert(defaultlist.get(i));
                            cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                            typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.defaultdbnotavailalert, Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton qrscan = form.findViewById(R.id.scanqrbtn);
        qrscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(Dashboard.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scansiona un codice QR");
                integrator.setOrientationLocked(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.initiateScan();
                dialog.dismiss();
            }
        });
    }

    public void historyGraphInitializer(GraphView graph) {
        graph.getViewport().setMaxXAxisSize(30);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
    }

    public void daygraphInitializer(GraphView daygraph) {
        daygraph.getViewport().setMaxXAxisSize(7);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(daygraph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{dayFromNumber(0), dayFromNumber(1), dayFromNumber(2), dayFromNumber(3), dayFromNumber(4), dayFromNumber(5), dayFromNumber(6)});
        daygraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        daygraph.getViewport().setYAxisBoundsManual(true);
        daygraph.getViewport().setMinY(0);
    }

    public void graphInitializer() {
        GraphView graph = findViewById(R.id.historygraph);
        GraphView daygraph = findViewById(R.id.daygraph);

        historyGraphInitializer(graph);
        daygraphInitializer(daygraph);
    }

    public Date getLocalDateFromString(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getStringFromLocalDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    public Date plusDays(Date from, int toadd) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(from);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, toadd);
        return c.getTime();
    }

    public void historyGraph(final GraphView graph) {
        //days[0] è sempre il primo giorno nel db
        //days.length = cups.length
        final List<String> days = db.cupDAO().getDays();
        final List<Integer> cups = db.cupDAO().perDay();

        if (days.size() > 0) {
            Date fromDate = getLocalDateFromString(days.get(0));
            Date toDate = Calendar.getInstance().getTime();
            Date current = fromDate;
            toDate = plusDays(toDate, 1);
            List<Date> dates = new ArrayList<>(25);
            while (current.getTime() < toDate.getTime()) {
                dates.add(current);
                current = plusDays(current, 1);
            }

            graph.getViewport().setScalable(true);
            //graph.getViewport().setMinY(0);
            //graph.getViewport().setMaxY(20);
            graph.getViewport().setMaxX((dates.get(dates.size() - 2)).getTime());
            if (dates.size() <= 30)
                graph.getViewport().setMinX((dates.get(0)).getTime());
            else
                graph.getViewport().setMinX((dates.get(dates.size() - 29)).getTime());
            graph.getViewport().setScalable(false);

            List<DataPoint> points = new ArrayList<>();
            int j = 0;
            int i, maxc = 0;
            for (i = 0; i < dates.size(); i++) {
                String day = getStringFromLocalDate(dates.get(i));
                Date daydate = dates.get(i);
                if (j < days.size() && day.equals(days.get(j))) {
                    points.add(new DataPoint(daydate, cups.get(j)));
                    if (cups.get(j) > maxc) maxc = cups.get(j);
                    j++;
                } else points.add(new DataPoint(daydate, 0));
            }
            DataPoint[] pointsv = new DataPoint[points.size()];
            pointsv = points.toArray(pointsv);
            graph.removeAllSeries();
            graph.getViewport().setMaxY(maxc);

            if (state.getBoolean("historyline", false)) {
                BarGraphSeries<DataPoint> seriesb = new BarGraphSeries<>(pointsv);
                seriesb.setDrawValuesOnTop(true);
                seriesb.setColor(getResources().getColor(R.color.colorAccent));
                seriesb.setSpacing(25);
                graph.addSeries(seriesb);
                seriesb.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        DateFormat mDateFormat = android.text.format.DateFormat.getDateFormat(Dashboard.this.getApplicationContext());
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis((long) dataPoint.getX());
                        final Balloon balloon = new Balloon.Builder(getBaseContext())
                                .setText(mDateFormat.format(mCalendar.getTimeInMillis()) + ": " + (int) dataPoint.getY() + " " + getString(R.string.tazzine_totali).toLowerCase())
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(graph);
                    }
                });
            } else {
                LineGraphSeries<DataPoint> seriesl = new LineGraphSeries<>(pointsv);
                seriesl.setColor(getResources().getColor(R.color.colorAccent));
                graph.addSeries(seriesl);
                seriesl.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        DateFormat mDateFormat = android.text.format.DateFormat.getDateFormat(Dashboard.this.getApplicationContext());
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis((long) dataPoint.getX());
                        final Balloon balloon = new Balloon.Builder(getBaseContext())
                                .setText(mDateFormat.format(mCalendar.getTimeInMillis()) + ": " + (int) dataPoint.getY() + " " + getString(R.string.tazzine_totali).toLowerCase())
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(graph);
                    }
                });
                seriesl.setThickness(5);
                seriesl.setDataPointsRadius(10);
                seriesl.setDrawDataPoints(true);
            }

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    if (isValueX) {
                        // format as date
                        mCalendar.setTimeInMillis((long) value);
                        return format.format(mCalendar.getTimeInMillis());
                    } else {
                        return super.formatLabel(value, false);
                    }
                }
            });
            graph.getGridLabelRenderer().setHumanRounding(false);
            graph.getViewport().setXAxisBoundsManual(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void typePie(final PieChart pie) {
        pie.clear();
        List<Coffeetype> types = db.coffetypeDao().getAll();
        List<Coffeetype> favs = db.coffetypeDao().getFavs();
        int totalcups = 0;
        for (Coffeetype type : types) totalcups += type.getQnt();
        for (Coffeetype type : types) {
            int clr;
            boolean isfav = favs.contains(type);
            if (isfav) {
                clr = getResources().getColor(R.color.colorAccent);
            } else clr = getResources().getColor(R.color.colorAccentDark);

            String name = "";
            int n = db.cupDAO().getAll(type.getKey()).size();
            double perc;
            if (totalcups > 0) perc = (double) (n * 100) / totalcups;
            else perc = 0;
            if (perc > 2.5) name = type.getName();
            Segment segment = new Segment(name, n);
            SegmentFormatter formatter = new SegmentFormatter(clr);
            formatter.setRadialInset((float) 1);
            Paint pnt = new Paint(formatter.getLabelPaint());
            pnt.setTextSize(30);
            if (db.coffetypeDao().getFavs().contains(type)) {
                pnt.setFakeBoldText(true);
            }
            formatter.setLabelPaint(pnt);
            pie.addSegment(segment, formatter);
        }

        pie.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(@NonNull  View view, @NonNull MotionEvent motionEvent) {
                view.performClick();
                PointF click = new PointF(motionEvent.getX(), motionEvent.getY());
                if(pie.getPie().containsPoint(click)) {
                    Segment segment = pie.getRenderer(PieRenderer.class).getContainingSegment(click);

                    if(segment != null) {
                        int n = segment.getValue().intValue();
                        String str = segment.getTitle();

                        final Balloon balloon = new Balloon.Builder(getBaseContext())
                                .setText(str + ": " + n)
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull  View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(pie);
                    }
                }
                return true;
            }
        });

        pie.redraw();
    }

    public String dayFromNumber(int n) { //Get localized day name
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());

        try {
            switch (n) {
                case 0:
                    date = sdf1.parse("2019/09/15");
                    break;
                case 1:
                    date = sdf1.parse("2019/09/16");
                    break;
                case 2:
                    date = sdf1.parse("2019/09/17");
                    break;
                case 3:date = sdf1.parse("2019/09/18");
                    break;
                case 4:date = sdf1.parse("2019/09/19");
                    break;
                case 5:
                    date = sdf1.parse("2019/09/20");
                    break;
                case 6:
                    date = sdf1.parse("2019/09/21");
                    break;
            }

            return sdf.format(date);
        } catch (ParseException e) {
            return "";
        }

    }

    public void dayGraph(final GraphView daygraph) {
        List<Cup> allcups = new ArrayList<>();
        for (Coffeetype type : db.coffetypeDao().getAll()) {
            allcups.addAll(db.cupDAO().getAll(type.getKey()));
        }
        int[] cupPerDay = new int[7];
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
        Calendar clndr = Calendar.getInstance();
        int day;
        for (Cup cup : allcups) {
            try {
                //Log.d("DAYS", sdf.format(sdf1.parse(cup.getDay())));
                clndr.setTime(sdf1.parse(cup.getDay()));
                day = clndr.get(Calendar.DAY_OF_WEEK) - 1;
                cupPerDay[day]++;
            } catch (ParseException ignored) {}
        }
        int max = 0;

        DataPoint[] pointsv = new DataPoint[7];
        for (int i = 0; i < 7; i++) {
            pointsv[i] = new DataPoint(i, cupPerDay[i]);
            if (cupPerDay[i] > max) max = cupPerDay[i];
        }

        BarGraphSeries<DataPoint> dayseries = new BarGraphSeries<>(pointsv);
        dayseries.setDrawValuesOnTop(true);
        dayseries.setColor(getResources().getColor(R.color.colorAccent));
        dayseries.setSpacing(25);

        dayseries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String day = dayFromNumber((int)dataPoint.getX());
                final Balloon balloon = new Balloon.Builder(getBaseContext())
                        .setText(day + ": " + String.format(Locale.getDefault(), "%d",(int)dataPoint.getY()) + " " + getString(R.string.tazzine_totali).toLowerCase())
                        .setWidthRatio(0.5f)
                        .setBackgroundColorResource(R.color.colorAccent)
                        .setBalloonAnimation(BalloonAnimation.FADE)
                        .setArrowVisible(false)
                        .build();
                balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                    @Override
                    public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                        balloon.dismiss();
                    }
                });
                balloon.showAlignBottom(daygraph);
            }
        });

        daygraph.removeAllSeries();
        daygraph.addSeries(dayseries);
        daygraph.getViewport().setMaxY(max);
    }

    public void graphUpdater() {
        GraphView graph = findViewById(R.id.historygraph);
        PieChart pie = findViewById(R.id.piegraph);
        GraphView daygraph = findViewById(R.id.daygraph);
        TextView totalcupstxtv = findViewById(R.id.totalcups);
        TextView totalcupslastmonthtxtv = findViewById(R.id.totalcups_lastmonth);
        TextView totalliterstxtv = findViewById(R.id.totalliters);
        Calendar c = Calendar.getInstance();
        int curmonth = c.get(Calendar.MONTH);
        int curyear = c.get(Calendar.YEAR);
        int month;
        int year;
        int cupstotal = 0;
        int cupstotal_lastmonth = 0;
        int milliliterstotal = 0;

        for (Coffeetype type : db.coffetypeDao().getAll()) {
            if (type.isLiquido()) milliliterstotal += (type.getLiters() * type.getQnt());
            cupstotal += type.getQnt();
        }
        for (String day : db.cupDAO().getDays()) {
            Date date = getLocalDateFromString(day);
            c.setTime(date);
            month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
            if (month == curmonth && year == curyear) {
                cupstotal_lastmonth += db.cupDAO().getAll(day).size();
            }
        }
        String str = "" + cupstotal;
        totalcupstxtv.setText(str);
        str = "" + cupstotal_lastmonth;
        totalcupslastmonthtxtv.setText(str);
        if (milliliterstotal < 1000) {
            str = milliliterstotal + " ml";
            totalliterstxtv.setText(str);
        }
        else {
            str = milliliterstotal / 1000 + " l";
            totalliterstxtv.setText(str);
        }

        historyGraph(graph);
        typePie(pie);
        dayGraph(daygraph);

    }

    public void startAlarmBroadcastReceiver(Context context) { //TODO reimplementare
        Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, state.getInt("notifhour", 20));
        calendar.set(Calendar.MINUTE, state.getInt("notifmin", 30));
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void stopAlarmBroadcastReceiver(Context context) { //TODO reimplementare
        Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void addCup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selecttype);
        final List<Coffeetype> list = db.coffetypeDao().getAll();
        Calendar cld = Calendar.getInstance();
        final DatePickerDialog StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
                final String date = sdf.format(newDate.getTime());
                sdf = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
                final String day = sdf.format(newDate.getTime());
                builder.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.type_element, list), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int pos) {
                        list.get(pos).setQnt(list.get(pos).getQnt() + 1);
                        db.coffetypeDao().update(list.get(pos));
                        cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                        typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
                        db.cupDAO().insert(new Cup(list.get(pos).getKey(), date, day));
                        graphUpdater();
                    }
                });
                builder.show();
            }
        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
        StartTime.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, R.string.annulla, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String str = result.getContents();
                    String[] strtype = str.split("::");
                    final Coffeetype coffeetype = new Coffeetype(strtype[0], Integer.parseInt(strtype[2]), strtype[1], Boolean.parseBoolean(strtype[3]), strtype[4], Float.parseFloat(strtype[5]), null, false);
                    final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
                    final View form = View.inflate(this, R.layout.addtypedialog, null);
                    final TextView literstxt = form.findViewById(R.id.ltrsmgtext);
                    final CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);
                    final ImageView typeimage = form.findViewById(R.id.typeimage);
                    final boolean liquido = coffeetype.isLiquido();
                    final int qnt = coffeetype.getLiters();
                    EditText nameedittxt = form.findViewById(R.id.nametxt);
                    EditText descedittxt = form.findViewById(R.id.desctxt);
                    EditText sostedittxt = form.findViewById(R.id.sosttxt);
                    EditText pricetedittxt = form.findViewById(R.id.pricetxt);
                    ImageButton defaultdbbtn = form.findViewById(R.id.defaultbtn);
                    ImageButton qrbtn = form.findViewById(R.id.scanqrbtn);
                    defaultdbbtn.setVisibility(View.INVISIBLE);
                    qrbtn.setVisibility(View.INVISIBLE);

                    nameedittxt.setText(coffeetype.getName());
                    descedittxt.setText(coffeetype.getDesc());
                    sostedittxt.setText(coffeetype.getSostanza());
                    str = "" + coffeetype.getPrice();
                    pricetedittxt.setText(str);

                    if (liquido) liquidckbx.setChecked(true);
                    else liquidckbx.setChecked(false);
                    str = qnt + (liquido ? " ml" : " mg");
                    literstxt.setText(str);

                    ImageButton addbtn = form.findViewById(R.id.incrbtn);
                    addbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            coffeetype.setLiters(coffeetype.getLiters() + 5);
                            String str = coffeetype.getLiters() + (liquidckbx.isChecked() ? " ml" : " mg");
                            literstxt.setText(str);
                        }
                    });

                    ImageButton rmvbtn = form.findViewById(R.id.decrbtn);
                    rmvbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            coffeetype.setLiters(coffeetype.getLiters() - 5);
                            if (coffeetype.getLiters() < 0)
                                coffeetype.setLiters(0);
                            String str = coffeetype.getLiters() + (liquidckbx.isChecked() ? " ml" : " mg");
                            literstxt.setText(str);
                        }
                    });

                    liquidckbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            coffeetype.setLiquido(isChecked);
                            String str = coffeetype.getLiters() + (liquidckbx.isChecked() ? " ml" : " mg");
                            literstxt.setText(str);
                        }
                    });

                    typeimage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            currentbitmap = null;
                            currentimageview = typeimage;
                            startActivityForResult(i, 9);
                            return true;
                        }
                    });

                    dialogbuilder.setView(form);
                    dialogbuilder.create();
                    final AlertDialog dialog = dialogbuilder.show();
                    if (coffeetype.isDefaulttype()) {
                        Snackbar.make(form.findViewById(R.id.linearLayout), R.string.editdefaultalert, Snackbar.LENGTH_SHORT).show();
                    }

                    Button positive = form.findViewById(R.id.confirmbtn);
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText nameedittxt = form.findViewById(R.id.nametxt);
                            EditText descedittxt = form.findViewById(R.id.desctxt);
                            EditText sostedittxt = form.findViewById(R.id.sosttxt);
                            EditText pricetedittxt = form.findViewById(R.id.pricetxt);
                            CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

                            String name = nameedittxt.getText().toString();
                            if (name.isEmpty()) {
                                final Balloon balloon = new Balloon.Builder(Dashboard.this)
                                        .setText(Dashboard.this.getString(R.string.nameemptyalert))
                                        .setBackgroundColorResource(R.color.colorAccent)
                                        .setWidthRatio(0.75f)
                                        .setBalloonAnimation(BalloonAnimation.FADE)
                                        .setArrowVisible(true)
                                        .setArrowOrientation(ArrowOrientation.TOP)
                                        .build();
                                balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                                    @Override
                                    public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                        balloon.dismiss();
                                    }
                                });
                                balloon.showAlignBottom(nameedittxt);
                            } else {
                                coffeetype.setName(nameedittxt.getText().toString());
                                coffeetype.setDesc(descedittxt.getText().toString());
                                coffeetype.setLiquido(liquidckbx.isChecked());
                                coffeetype.setSostanza(sostedittxt.getText().toString());
                                coffeetype.setPrice(Float.parseFloat(pricetedittxt.getText().toString()));
                                if (currentbitmap != null) {
                                    String uri = saveToInternalStorage(currentbitmap);
                                    coffeetype.setImg(uri);
                                    currentbitmap = null;
                                }
                                coffeetype.setDefaulttype(false);

                                db.coffetypeDao().insert(coffeetype);
                                cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                                typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
                                dialog.dismiss();
                            }
                        }
                    });

                    Button negative = form.findViewById(R.id.cancelbtn);
                    negative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            coffeetype.setLiquido(liquido);
                            coffeetype.setQnt(qnt);
                            dialog.dismiss();
                        }
                    });
                    Toast.makeText(this, coffeetype.getName(), Toast.LENGTH_LONG).show();
                } catch (Exception e) { Toast.makeText(this, "Error", Toast.LENGTH_LONG).show(); }
            }
        } else {
            if (requestCode == 9) {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri uri = data.getData();
                    InputStream in;
                    try {
                        in = getContentResolver().openInputStream(uri);
                        final Bitmap selected_img = BitmapFactory.decodeStream(in);
                        currentimageview.setImageBitmap(selected_img);
                        currentbitmap = selected_img;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.placeholder, Toast.LENGTH_LONG).show(); //TODO error message
                    }
                }
            }
        }
    }

    public String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        // Create imageDir
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

    public Bitmap loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scheduleNotification(Notification notification, int delay) {
        Intent notificationIntent = new Intent(this, NotifReceiver.class);
        notificationIntent.putExtra(NotifReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotifReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotifReceiver.NOTIFICATION_ID);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.coffeeicon);
        return builder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        state = this.getSharedPreferences(getString(R.string.apppkg), MODE_PRIVATE);
        editor = state.edit();

        createNotificationChannel();
        if (state.getBoolean("notifonoff", true))
            startAlarmBroadcastReceiver(getApplicationContext());
        else stopAlarmBroadcastReceiver(getApplicationContext());
        //scheduleNotification(getNotification("5 second delay"), 5000);

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTopToolbar.setNavigationIcon(R.drawable.ic_hamburger);
        TextView vertxtv = findViewById(R.id.vertxt);
        String str = "Version " + BuildConfig.VERSION_NAME;
        vertxtv.setText(str);

        final DrawerLayout drawer = findViewById(R.id.containerdrawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawer.closeDrawers();
                ViewFlipper vf = findViewById(R.id.viewflipper);

                switch (menuItem.getItemId()) { //TODO REDO
                    case R.id.navigation_statistics:
                        if (vf.getDisplayedChild() != 0) {
                            graphUpdater();
                            vf.setDisplayedChild(0);
                            if (state.getBoolean("statstutorial", true)) {
                                final Balloon balloon = new Balloon.Builder(getBaseContext())
                                        .setText(getString(R.string.tutorial_stats))
                                        .setBackgroundColorResource(R.color.colorAccent)
                                        .setBalloonAnimation(BalloonAnimation.FADE)
                                        .setArrowVisible(false)
                                        .build();
                                balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                                    @Override
                                    public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                        balloon.dismiss();
                                    }
                                });
                                balloon.showAlignTop(findViewById(R.id.toolbar));
                                editor.putBoolean("statstutorial", false);
                                editor.apply();
                            }
                        }

                        return true;
                    case R.id.navigation_types:
                        if (vf.getDisplayedChild() != 1) {
                            String[] funfacts = getResources().getStringArray(R.array.funfacts);
                            Random rnd = new Random();
                            int i = rnd.nextInt(funfacts.length);

                            TextView funfactstxtv = findViewById(R.id.funfacttxt);
                            funfactstxtv.setText(funfacts[i]);
                            typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));

                            vf.setDisplayedChild(1);

                            if (state.getBoolean("typestutorial", true)) {
                                final Balloon balloon = new Balloon.Builder(getBaseContext())
                                        .setText(getString(R.string.tutorial_types))
                                        .setBackgroundColorResource(R.color.colorAccent)
                                        .setBalloonAnimation(BalloonAnimation.FADE)
                                        .setArrowVisible(true)
                                        .setArrowPosition(0.90f)
                                        .setArrowOrientation(ArrowOrientation.TOP)
                                        .build();
                                balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                                    @Override
                                    public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                        balloon.dismiss();
                                    }
                                });
                                balloon.showAlignBottom(findViewById(R.id.action_add));
                                editor.putBoolean("typestutorial", false).apply();
                            }
                        }

                        return true;
                    case R.id.navigation_settings:
                        if (vf.getDisplayedChild() != 2) {
                            vf.setDisplayedChild(2);
                        }

                        return true;

                    case R.id.navigation_cups:
                        if (vf.getDisplayedChild() != 3) {
                            String[] funfacts = getResources().getStringArray(R.array.funfacts);
                            Random rnd = new Random();
                            int i = rnd.nextInt(funfacts.length);

                            TextView funfactstxtv = findViewById(R.id.cupsfunfacttxt);
                            funfactstxtv.setText(funfacts[i]);
                            cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));

                            vf.setDisplayedChild(3);

                            try {
                                if (state.getBoolean("cupstutorial", true)) {
                                    final Balloon balloon = new Balloon.Builder(getBaseContext())
                                            .setText(getString(R.string.tutorial_cups))
                                            .setBackgroundColorResource(R.color.colorAccent)
                                            .setBalloonAnimation(BalloonAnimation.FADE)
                                            .setArrowVisible(true)
                                            .setArrowOrientation(ArrowOrientation.BOTTOM)
                                            .build();
                                    balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                                        @Override
                                        public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                            balloon.dismiss();
                                        }
                                    });
                                    balloon.showAlignBottom(findViewById(R.id.cupsrecview));
                                    editor.putBoolean("cupstutorial", false).apply();
                                }
                            } catch (Exception ignored) {}
                        }

                        return true;
                }
                return false;
            }
        });


        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb")
                .allowMainThreadQueries() //TODO fix
                .addMigrations(MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
                .build();
        //Log.d("ROOMDB", "path: " + getDatabasePath("typedb").getAbsolutePath());
        updateDefaultDatabase();

        for (Coffeetype type : db.coffetypeDao().getAll()) {
            type.setQnt(db.cupDAO().getAll(type.getKey()).size());
            db.coffetypeDao().update(type);
        }

        adInitializer();
        graphInitializer();

        cupsRecview = findViewById(R.id.cupsrecview);
        cupsRecview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));

        typesRecview = findViewById(R.id.recview);
        typesRecview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
        SnapHelper helper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                View centerView = findSnapView(layoutManager);
                if (centerView == null)
                    return RecyclerView.NO_POSITION;

                int position = layoutManager.getPosition(centerView);
                int targetPosition = -1;
                if (layoutManager.canScrollHorizontally()) {
                    if (velocityX < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }
                if (layoutManager.canScrollVertically()) {
                    if (velocityY < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                final int firstItem = 0;
                final int lastItem = layoutManager.getItemCount() - 1;
                targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));
                return targetPosition;
            }
        };
        helper.attachToRecyclerView(typesRecview);

        Button rstdbbtn = findViewById(R.id.resetdbbtn);
        rstdbbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage(getString(R.string.resetdb) + "?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.cupDAO().nuke();
                                db.coffetypeDao().nuke();
                                updateDefaultDatabase();
                                cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                                typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview));
                                Snackbar.make(findViewById(R.id.container), "Database resettato", Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar.make(findViewById(R.id.container), "Annullato", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                dialogbuilder.create();
                dialogbuilder.show();
            }
        });

        Button showstatbtn = findViewById(R.id.statbtn);
        showstatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int milliliterstotal = 0;
                int cupstotal = 0;
                StringBuilder cupsstat = new StringBuilder();

                for (Coffeetype type : db.coffetypeDao().getAll()) {
                    cupsstat.append(type.getName()).append("\n");
                    milliliterstotal += (type.getLiters() * type.getQnt());
                    cupstotal += type.getQnt();
                    for (Cup cup : db.cupDAO().getAll(type.getKey()))
                        cupsstat.append("\t[").append(cup.toString()).append("]\n");
                }

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage("Bevuti in totale " + milliliterstotal + " ml in " + cupstotal + " tazzine.\n\n" + cupsstat)
                        .setNeutralButton("OK", null);
                dialogbuilder.create();
                dialogbuilder.show();
            }
        });

        final Switch historyswitch = findViewById(R.id.historybarswitch);
        historyswitch.setChecked(state.getBoolean("historyline", false));
        historyswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("historyline", b).apply();
                graphUpdater();
            }
        });

        Button exportdata = findViewById(R.id.exportdatabtn);
        exportdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDBPath = getDatabasePath("typedb").getAbsolutePath();
                Toast.makeText(getApplicationContext(), currentDBPath, Toast.LENGTH_SHORT).show();

            }
        });

        final Button resettutorialbtn = findViewById(R.id.resettutorialbtn);
        resettutorialbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("dashboardtutorial", true);
                editor.putBoolean("typestutorial", true);
                editor.putBoolean("cupstutorial", true);
                editor.commit();
            }
        });

        final TextView notiftimetxtv = findViewById(R.id.notiftimetxt);
        if (state.getBoolean("notifonoff", true)) notiftimetxtv.setText(String.format(Locale.getDefault(), "%d:%d", state.getInt("notifhour", 20), state.getInt("notifmin", 30)));
        else notiftimetxtv.setText("--:--");
        notiftimetxtv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (state.getBoolean("notifonoff", true)) {
                    // Get Current Time
                    final Calendar c = Calendar.getInstance();
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            editor.putInt("notifhour", hourOfDay);
                            editor.putInt("notifmin", minute);
                            editor.commit();
                            notiftimetxtv.setText(String.format(Locale.getDefault(), "%02d:%02d", state.getInt("notifhour", 20), state.getInt("notifmin", 30)));
                            startAlarmBroadcastReceiver(getApplicationContext());
                        }
                    }, mHour, mMinute, true);
                    timePickerDialog.show();
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.notifica_giornaliera) + " OFF", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });

        final Switch dailynotifswitch = findViewById(R.id.dailynotifswitch);
        dailynotifswitch.setChecked(state.getBoolean("notifonoff", true));
        dailynotifswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("notifonoff", isChecked);
                editor.commit();

                if (isChecked) {
                    startAlarmBroadcastReceiver(getApplicationContext());
                    notiftimetxtv.setText(String.format(Locale.getDefault(), "%d:%d", state.getInt("notifhour", 20), state.getInt("notifmin", 30)));
                }
                else {
                    stopAlarmBroadcastReceiver(getApplicationContext());
                    notiftimetxtv.setText("--:--");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        graphUpdater();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notificationstitle);
            String description = getString(R.string.notificationdescr);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotifReceiver.NOTIFICATION_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}