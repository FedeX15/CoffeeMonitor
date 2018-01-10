package com.fexed.coffeecounter;

import android.app.DatePickerDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Dashboard extends AppCompatActivity {
    public SharedPreferences state;
    public SharedPreferences.Editor editor;
    public AppDatabase db;
    public RecyclerView recview;

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
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO icone nel menù di navigazione
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.navigation, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ViewFlipper vf = findViewById(R.id.viewflipper);

                        switch (item.getItemId()) {
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

                                    vf.setDisplayedChild(1);
                                }

                                return true;
                            case R.id.navigation_notifications:

                                if (vf.getDisplayedChild() != 2) {
                                    vf.setDisplayedChild(2);
                                }

                                return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        insertStandardTypes();

        adInitializer();
        graphInitializer();
        graphUpdater();

        recview = findViewById(R.id.recview);
        recview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recview.setAdapter(new RecviewAdapter(db));
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
        helper.attachToRecyclerView(recview);

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
                                recview.setAdapter(new RecviewAdapter(db));
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

                for (Coffeetype type : db.coffetypeDao().getAll()) {
                    milliliterstotal += (type.getLiters() * type.getQnt());
                    cupstotal += type.getQnt();
                }

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage("Bevuti in totale " + milliliterstotal + " ml in " + cupstotal + " tazzine.")
                        .setNeutralButton("OK", null);
                dialogbuilder.create();
                dialogbuilder.show();
            }
        });

        final Button addcupdatebtn = findViewById(R.id.addcupdatebtn);
        Calendar cld = Calendar.getInstance();
        final DatePickerDialog StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Coffeetype type = db.coffetypeDao().getAll().get(0);
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
                String Date = sdf.format(newDate.getTime());
                sdf = new SimpleDateFormat("dd/MMM/yyy", Locale.getDefault());
                String Day = sdf.format(newDate.getTime());
                db.cupDAO().insert(new Cup(type.getKey(), Date, Day));
                type.setQnt(type.getQnt() + 1);
                db.coffetypeDao().update(type);
            }
        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
        addcupdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTime.show();
            }
        });
    }


    private void insertStandardTypes() {
        if (db.coffetypeDao().getAll().size() == 0) {
            db.coffetypeDao().insert(new Coffeetype("Caffè espresso", 30, "Tazzina di caffè da bar o da moka.", true, "Caffeina", 0));
            db.coffetypeDao().insert(new Coffeetype("Cappuccino", 150, "Tazza di cappuccino da bar.", true, "Caffeina", 0));
            db.coffetypeDao().insert(new Coffeetype("Caffè ristretto", 16, "Tazzina di caffè ristretto.", true, "Caffeina", 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
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
                        recview.setAdapter(new RecviewAdapter(db));
                        return true;
                    }
                });
                popup.show();
                break;
            case R.id.action_notifs:
                //TODO implementare consigli e notifiche
                break;

            case R.id.action_add:
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
                        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

                        String name = nameedittxt.getText().toString();
                        if (name.isEmpty()) {
                            Snackbar.make(findViewById(R.id.container), "Il nome non può essere vuoto", Snackbar.LENGTH_SHORT).show();
                        } else {
                            int liters = state.getInt("qnt", 0);
                            String desc = descedittxt.getText().toString();
                            String sostanza = sostedittxt.getText().toString();

                            boolean liquid = liquidckbx.isChecked();
                            Coffeetype newtype = new Coffeetype(name, liters, desc, liquid, sostanza, 0);

                            db.coffetypeDao().insert(newtype);

                            recview.setAdapter(new RecviewAdapter(db));
                            Snackbar.make(findViewById(R.id.container), "Tipo " + newtype.getName() + " aggiunto", Snackbar.LENGTH_SHORT).show();

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
                break;
        }
        return true;
    }

    public void adInitializer() {
        MobileAds.initialize(this, "ca-app-pub-9387595638685451~9345692620");
        AdView mAdView = findViewById(R.id.banner1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void graphInitializer() {
        XYPlot plot = findViewById(R.id.plotCount);
        plot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.setBackgroundColor(Color.TRANSPARENT);
        plot.getGraph().getDomainCursorPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getRangeCursorPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.setTitle("Conteggio giornaliero");
        plot.setRangeLabel(null);
        plot.setLegend(null);
        plot.setDomainLabel(null);
        PanZoom.attach(plot);

        PieChart pie = findViewById(R.id.pieTypes);
    }

    public void graphUpdater() {
        // initialize our XYPlot reference:
        XYPlot plot = findViewById(R.id.plotCount);

        // create a couple arrays of y-values to plot:
        final List<String> days = db.cupDAO().getDays();

        List<Integer> cups = db.cupDAO().perDay();

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(cups, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        /*LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));*/

        BarFormatter bf = new BarFormatter(getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorAccentDark));
        bf.getPointLabelFormatter().setTextPaint(new Paint(getResources().getColor(R.color.colorText)));

        // add a new series' to the xyplot:
        plot.clear();
        plot.addSeries(series1, bf);
        BarRenderer renderer = plot.getRenderer(BarRenderer.class);
        renderer.setBarGroupWidth(BarRenderer.BarGroupWidthMode.FIXED_GAP, PixelUtils.dpToPix(5));


        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(days.get(i).split("/")[0] + "/" + days.get(i).split("/")[1]);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        plot.redraw();


        PieChart pie = findViewById(R.id.pieTypes);
        pie.clear();
        List<Coffeetype> types = db.coffetypeDao().getAll();
        for (Coffeetype type : types) {
            Segment segment = new Segment(type.getName(), db.cupDAO().getAll(type.getKey()).size());
            SegmentFormatter formatter = new SegmentFormatter(getResources().getColor(R.color.colorAccent));
            formatter.setRadialInset((float) 1);
            pie.addSegment(segment, formatter);
        }
        pie.redraw();
    }
}
