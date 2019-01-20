package com.fexed.coffeecounter;

import android.app.DatePickerDialog;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Dashboard extends AppCompatActivity {
    public SharedPreferences state;
    public SharedPreferences.Editor editor;
    public AppDatabase db;
    public RecyclerView typesRecview;
    public RecyclerView cupsRecview;

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    private void insertStandardTypes() {
        if (db.coffetypeDao().getAll().size() == 0) {
            db.coffetypeDao().insert(new Coffeetype("Caffè espresso", 30, "Tazzina di caffè da bar o da moka.", true, "Caffeina", 0));
            db.coffetypeDao().insert(new Coffeetype("Cappuccino", 150, "Tazza di cappuccino da bar.", true, "Caffeina", 0));
            db.coffetypeDao().insert(new Coffeetype("Caffè ristretto", 15, "Tazzina di caffè ristretto.", true, "Caffeina", 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

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

    public void adInitializer() {
        MobileAds.initialize(this, "ca-app-pub-9387595638685451~3707270987");
        AdView mAdView = findViewById(R.id.banner1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView mAdView2 = findViewById(R.id.banner2);
        mAdView2.loadAd(adRequest);
        AdView mAdView3 = findViewById(R.id.banner3);
        mAdView3.loadAd(adRequest);
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
                        typesRecview.setAdapter(new TypeRecviewAdapter(db));
                        cupsRecview.setAdapter(new CupRecviewAdapter(db));
                        return true;
                    }
                });
                popup.show();
                break;
            case R.id.action_notifs:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Tips");
                builder.setIcon(R.drawable.ic_info);
                builder.setMessage(generateTip());
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
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
        return "Questo è un consiglio. Trattalo bene /s";
    }

    public void addNewType() {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(findViewById(R.id.action_favs).getContext());
        final View form = getLayoutInflater().inflate(R.layout.addtypedialog, null);
        final TextView literstxt = form.findViewById(R.id.ltrsmgtext);
        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

        editor.putInt("qnt", 0);
        editor.putString("suffix", (liquidckbx.isChecked()) ? " ml" : " mg");
        editor.commit();
        literstxt.setText(state.getInt("qnt", 0) + state.getString("suffix", " ml"));

        ImageButton addbtn = form.findViewById(R.id.incrbtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qnt = state.getInt("qnt", 0);
                qnt += 5;
                editor.putInt("qnt", qnt);
                editor.commit();
                literstxt.setText(qnt + state.getString("suffix", " ml"));
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
                literstxt.setText(qnt + state.getString("suffix", " ml"));
            }
        });

        liquidckbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putString("suffix", (isChecked) ? " ml" : " mg");
                editor.commit();
                literstxt.setText(state.getInt("qnt", 0) + state.getString("suffix", " ml"));
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
                    Snackbar.make(findViewById(R.id.containerdrawer), "Il nome non può essere vuoto", Snackbar.LENGTH_SHORT).show();
                } else {
                    int liters = state.getInt("qnt", 0);
                    String desc = descedittxt.getText().toString();
                    String sostanza = sostedittxt.getText().toString();
                    float price = Float.parseFloat(pricetedittxt.getText().toString());

                    boolean liquid = liquidckbx.isChecked();
                    Coffeetype newtype = new Coffeetype(name, liters, desc, liquid, sostanza, price);

                    db.coffetypeDao().insert(newtype);

                    cupsRecview.setAdapter(new CupRecviewAdapter(db));
                    typesRecview.setAdapter(new TypeRecviewAdapter(db));
                    Snackbar.make(findViewById(R.id.containerdrawer), "Tipo " + newtype.getName() + " aggiunto", Snackbar.LENGTH_SHORT).show();

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
    }

    public void historyGraphInitializer(GraphView graph) {
        //graph.getViewport().setMinimalViewport(Double.NaN, Double.NaN, 0, Double.NaN);
        graph.getViewport().setMaxXAxisSize(30);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
    }

    public void daygraphInitializer(GraphView daygraph) {
        daygraph.getViewport().setMaxXAxisSize(7);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(daygraph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{"Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab"});
        daygraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        daygraph.getViewport().setYAxisBoundsManual(true);
        daygraph.getViewport().setMinY(0);
    }

    public void graphInitializer() {
        GraphView graph = findViewById(R.id.historygraph);
        GraphView daygraph = findViewById(R.id.daygraph);
        PieChart pie = findViewById(R.id.piegraph);

        historyGraphInitializer(graph);
        daygraphInitializer(daygraph);
    }

    public LocalDate getLocalDateFromString(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            LocalDate ret = format.parse(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return ret;
        } catch (ParseException e) {
            return null;
        }
    }

    public String getStringFromLocalDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return date.format(formatter);
    }

    public void historyGraph(GraphView graph) {
        //days[0] è sempre il primo giorno nel db
        //days.length = cups.length
        final List<String> days = db.cupDAO().getDays();
        final List<Integer> cups = db.cupDAO().perDay();

        if (days.size() > 0) {
            LocalDate fromDate = getLocalDateFromString(days.get(0));
            LocalDate toDate = LocalDate.now();
            LocalDate current = fromDate;
            toDate = toDate.plusDays(1);
            List<LocalDate> dates = new ArrayList<>(25);
            while (current.isBefore(toDate)) {
                dates.add(current);
                current = current.plusDays(1);
            }

            graph.getViewport().setScalable(true);
            //graph.getViewport().setMinY(0);
            //graph.getViewport().setMaxY(20);
            graph.getViewport().setMaxX(Date.from(dates.get(dates.size() - 1).atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
            if (dates.size() <= 30)
                graph.getViewport().setMinX(Date.from(dates.get(0).atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
            else
                graph.getViewport().setMinX(Date.from(dates.get(dates.size() - 29).atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
            graph.getViewport().setScalable(false);

            List<DataPoint> points = new ArrayList<>();
            int j = 0;
            int i, maxc = 0;
            for (i = 0; i < dates.size(); i++) {
                String day = getStringFromLocalDate(dates.get(i));
                Date daydate = Date.from(dates.get(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
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
                seriesb.setColor(getColor(R.color.colorAccent));
                seriesb.setSpacing(25);
                graph.addSeries(seriesb);
            } else {
                LineGraphSeries<DataPoint> seriesl = new LineGraphSeries<>(pointsv);
                seriesl.setColor(getColor(R.color.colorAccent));
                graph.addSeries(seriesl);
            }

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
            graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getGridLabelRenderer().setHumanRounding(false);
        }
    }

    public void typePie(PieChart pie) {
        pie.clear();
        List<Coffeetype> types = db.coffetypeDao().getAll();
        List<Coffeetype> favs = db.coffetypeDao().getFavs();
        for (Coffeetype type : types) {
            int clr;
            boolean isfav = favs.contains(type);
            if (isfav) {
                clr = getColor(R.color.colorAccent);
            } else clr = getColor(R.color.colorAccentDark);

            Segment segment = new Segment(type.getName(), db.cupDAO().getAll(type.getKey()).size());
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
        pie.redraw();
    }

    public void dayGraph(GraphView daygraph) {
        List<Cup> allcups = new ArrayList<>();
        for (Coffeetype type : db.coffetypeDao().getAll()) {
            for (Cup cup : db.cupDAO().getAll(type.getKey())) allcups.add(cup);
        }
        int[] cupPerDay = new int[7];
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
        Calendar clndr = Calendar.getInstance();
        int day;
        for (Cup cup : allcups) {
            try {
                Log.d("DAYS", sdf.format(sdf1.parse(cup.getDay())));
                clndr.setTime(sdf1.parse(cup.getDay()));
                day = clndr.get(Calendar.DAY_OF_WEEK) - 1;
                cupPerDay[day]++;
            } catch (ParseException e) {
            }
        }
        int max = 0;

        DataPoint[] pointsv = new DataPoint[7];
        for (int i = 0; i < 7; i++) {
            pointsv[i] = new DataPoint(i, cupPerDay[i]);
            if (cupPerDay[i] > max) max = cupPerDay[i];
        }

        BarGraphSeries<DataPoint> dayseries = new BarGraphSeries<>(pointsv);
        dayseries.setDrawValuesOnTop(true);
        dayseries.setColor(getColor(R.color.colorAccent));
        dayseries.setSpacing(25);
        daygraph.removeAllSeries();
        daygraph.addSeries(dayseries);
        daygraph.getViewport().setMaxY(max);
    }

    public void graphUpdater() {
        GraphView graph = findViewById(R.id.historygraph);
        PieChart pie = findViewById(R.id.piegraph);
        GraphView daygraph = findViewById(R.id.daygraph);

        historyGraph(graph);
        typePie(pie);
        dayGraph(daygraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        state = this.getSharedPreferences(getString(R.string.apppkg), MODE_PRIVATE);
        editor = state.edit();

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTopToolbar.setNavigationIcon(R.drawable.ic_hamburger);
        final DrawerLayout drawer = findViewById(R.id.containerdrawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawer.closeDrawers();
                ViewFlipper vf = findViewById(R.id.viewflipper);

                switch (menuItem.getItemId()) {
                    case R.id.navigation_statistics:

                        if (vf.getDisplayedChild() != 0) {
                            graphUpdater();
                            vf.setDisplayedChild(0);
                        }

                        return true;
                    case R.id.navigation_dashboard:

                        if (vf.getDisplayedChild() != 1) {
                            String[] funfacts = getResources().getStringArray(R.array.funfacts);
                            Random rnd = new Random();
                            int i = rnd.nextInt(funfacts.length);

                            TextView funfactstxtv = findViewById(R.id.funfacttxt);
                            funfactstxtv.setText(funfacts[i]);
                            typesRecview.setAdapter(new TypeRecviewAdapter(db));

                            vf.setDisplayedChild(1);
                        }

                        return true;
                    case R.id.navigation_notifications:

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
                            cupsRecview.setAdapter(new CupRecviewAdapter(db));

                            vf.setDisplayedChild(3);
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
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_19_20)
                .build();
        Log.d("ROOMDB", "path: " + getDatabasePath("typedb").getAbsolutePath());
        insertStandardTypes();

        for (Coffeetype type : db.coffetypeDao().getAll()) {
            type.setQnt(db.cupDAO().getAll(type.getKey()).size());
            db.coffetypeDao().update(type);
        }

        adInitializer();
        graphInitializer();
        graphUpdater();

        cupsRecview = findViewById(R.id.cupsrecview);
        cupsRecview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        cupsRecview.setAdapter(new CupRecviewAdapter(db));

        typesRecview = findViewById(R.id.recview);
        typesRecview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        typesRecview.setAdapter(new TypeRecviewAdapter(db));
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
                                insertStandardTypes();
                                cupsRecview.setAdapter(new CupRecviewAdapter(db));
                                typesRecview.setAdapter(new TypeRecviewAdapter(db));
                                Snackbar.make(findViewById(R.id.container), "Database resettato", Snackbar.LENGTH_SHORT).show();
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
                String cupsstat = "";

                for (Coffeetype type : db.coffetypeDao().getAll()) {
                    cupsstat = cupsstat + type.getName() + "\n";
                    milliliterstotal += (type.getLiters() * type.getQnt());
                    cupstotal += type.getQnt();
                    for (Cup cup : db.cupDAO().getAll(type.getKey()))
                        cupsstat = cupsstat + ("\t[" + cup.toString() + "]\n");
                }

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage("Bevuti in totale " + milliliterstotal + " ml in " + cupstotal + " tazzine.\n\n" + cupsstat)
                        .setNeutralButton("OK", null);
                dialogbuilder.create();
                dialogbuilder.show();
            }
        });

        Switch historyswitch = findViewById(R.id.historybarswitch);
        historyswitch.setChecked(state.getBoolean("historyline", false));
        historyswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("historyline", b).apply();
                graphUpdater();
            }
        });

        ImageButton sharebtn1 = findViewById(R.id.sharegraph1);
        final GraphView graph1 = findViewById(R.id.historygraph);
        sharebtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graph1.takeSnapshotAndShare(getApplicationContext(), "Coffee Monitor History Graph", "Coffee Monitor History Graph");
            }
        });

        ImageButton sharebtn2 = findViewById(R.id.sharegraph2);
        final PieChart graph2 = findViewById(R.id.piegraph);
        sharebtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Bitmap inImage = loadBitmapFromView(graph2);
                inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), inImage, "Coffee Monitor Pie Chart", null);
                if (path == null) {
                    // most likely a security problem
                    throw new SecurityException("Could not get path from MediaStore. Please check permissions.");
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                try {
                    getApplicationContext().startActivity(Intent.createChooser(i, "Coffee Monitor Pie Chart"));
                } catch (android.content.ActivityNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ImageButton sharebtn3 = findViewById(R.id.sharegraph3);
        final GraphView graph3 = findViewById(R.id.daygraph);
        sharebtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graph3.takeSnapshotAndShare(getApplicationContext(), "Coffee Monitor Days Graph", "Coffee Monitor Days Graph");
            }
        });

        final Button addcupdatebtn = findViewById(R.id.addcupdatebtn);
        Calendar cld = Calendar.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selecttype);
        final List<Coffeetype> list = db.coffetypeDao().getAll();

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
                        cupsRecview.setAdapter(new CupRecviewAdapter(db));
                        typesRecview.setAdapter(new TypeRecviewAdapter(db));

                        db.cupDAO().insert(new Cup(list.get(pos).getKey(), date, day));
                    }
                });
                builder.show();
            }
        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
        addcupdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTime.show();
            }
        });
    }

    public void addCup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selecttype);
        final List<Coffeetype> list = db.coffetypeDao().getAll();
        builder.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.type_element, list), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                list.get(pos).setQnt(list.get(pos).getQnt() + 1);
                db.coffetypeDao().update(list.get(pos));
                cupsRecview.setAdapter(new CupRecviewAdapter(db));
                typesRecview.setAdapter(new TypeRecviewAdapter(db));
                db.cupDAO().insert(new Cup(list.get(pos).getKey()));
                graphUpdater();
            }
        });
        builder.show();
    }
}