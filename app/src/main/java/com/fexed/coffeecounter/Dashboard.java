package com.fexed.coffeecounter;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.List;
import java.util.Vector;

public class Dashboard extends AppCompatActivity {
    public SharedPreferences state;
    public SharedPreferences.Editor editor;
    public AppDatabase db;
    public RecyclerView recview;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ViewFlipper vf = findViewById(R.id.viewflipper);

            switch (item.getItemId()) {
                case R.id.navigation_statistics:

                    if (vf.getDisplayedChild() != 0) {
                        setTitle(R.string.title_statistics);
                        graphUpdater();
                        vf.setDisplayedChild(0);
                    }

                    return true;
                case R.id.navigation_dashboard:

                    if (vf.getDisplayedChild() != 1) {
                        setTitle(R.string.title_dashboard);
                        vf.setDisplayedChild(1);
                    }

                    return true;
                case R.id.navigation_notifications:

                    if (vf.getDisplayedChild() != 2) {
                        setTitle(R.string.title_preferences);
                        vf.setDisplayedChild(2);
                    }

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        state = this.getSharedPreferences(getString(R.string.apppkg), MODE_PRIVATE);
        editor = state.edit();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        insertStandardTypes();

        adInitializer();
        graphInitializer();
        graphUpdater();

        recview = findViewById(R.id.recview);
        recview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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

        FloatingActionButton addfab = findViewById(R.id.addFab);
        addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                final List<Coffeetype> list = db.coffetypeDao().getAll();
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
            }
        });
        addfab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                final View form = getLayoutInflater().inflate(R.layout.addtypedialog, null);
                dialogbuilder.setView(form)
                        .setTitle(getString(R.string.addtype))
                        .setPositiveButton(getString(R.string.aggiungitxt), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText nameedittxt = form.findViewById(R.id.nametxt);
                                EditText descedittxt = form.findViewById(R.id.desctxt);
                                EditText litersedittxt = form.findViewById(R.id.literstxt);
                                EditText sostedittxt = form.findViewById(R.id.sosttxt);
                                CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

                                String name = nameedittxt.getText().toString();
                                double liters;
                                try {
                                    liters = Double.parseDouble(litersedittxt.getText().toString());
                                } catch (Exception ex) {
                                    liters = 0;
                                }
                                String desc = descedittxt.getText().toString();
                                String sostanza = sostedittxt.getText().toString();

                                boolean liquid = liquidckbx.isChecked();
                                Coffeetype newtype = new Coffeetype(name, liters, desc, liquid, sostanza);

                                db.coffetypeDao().insert(newtype);

                                recview.setAdapter(new RecviewAdapter(db));
                                Snackbar.make(findViewById(R.id.container), "Tipo " + newtype.getName() + " aggiunto", Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Annulla", null);
                dialogbuilder.create();
                dialogbuilder.show();
                return true;
            }
        });

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
                double literstotal = 0.0;
                int cupstotal = 0;

                for (Coffeetype type : db.coffetypeDao().getAll()) {
                    literstotal += (type.getLiters() * type.getQnt());
                    cupstotal += type.getQnt();
                }

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage("Bevuti in totale " + literstotal + " l in " + cupstotal + " tazzine.")
                        .setNeutralButton("OK", null);
                dialogbuilder.create();
                dialogbuilder.show();
            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void insertStandardTypes() {
        if (db.coffetypeDao().getAll().size() == 0) {
            db.coffetypeDao().insert(new Coffeetype("Caffè espresso", 0.03, "Tazzina di caffè da bar o da moka.", true, "Caffeina"));
            db.coffetypeDao().insert(new Coffeetype("Cappuccino", 0.15, "Tazza di cappuccino da bar.", true, "Caffeina"));
            db.coffetypeDao().insert(new Coffeetype("Caffè ristretto", 0.016, "Tazzina di caffè ristretto.", true, "Caffeina"));
        }
    }

    public void adInitializer() {
        MobileAds.initialize(this, "ca-app-pub-9387595638685451~9345692620");
        AdView mAdView = findViewById(R.id.banner1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void graphInitializer() {
        GraphView grapha = findViewById(R.id.grapha);
        grapha.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.cardview_light_background));
        grapha.getGridLabelRenderer().setLabelsSpace(0);
        grapha.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.cardview_light_background));
        grapha.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.cardview_light_background));

        GraphView graphc = findViewById(R.id.graphc);
        graphc.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.cardview_light_background));
        graphc.getGridLabelRenderer().setLabelsSpace(0);
        graphc.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.cardview_light_background));
        graphc.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.cardview_light_background));
    }

    public void graphUpdater() {
        GraphView grapha = findViewById(R.id.grapha);
        GraphView graphc = findViewById(R.id.graphc);
        grapha.removeAllSeries();
        graphc.removeAllSeries();

        List<Coffeetype> types = db.coffetypeDao().getAll();
        Vector<String> names = new Vector<>(1, 1);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>();
        for (int i = 0; i < types.size(); i++) {
            names.add(types.get(i).getName());
            series.appendData(new DataPoint(i, types.get(i).getQnt()), true, 5, false);
        }
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(grapha);
        String[] strings = names.toArray(new String[names.size()]);
        staticLabelsFormatter.setHorizontalLabels(strings);
        grapha.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        series.setSpacing(5);
        grapha.addSeries(series);

        /*Vector<String> days = new Vector<>(db.cupDAO().getDays());
        Log.d("Days", days.size() + "");
        BarGraphSeries<DataPoint> seriesb = new BarGraphSeries<>();
        for (int i = 0; i < days.size(); i++) seriesb.appendData(new DataPoint(i, db.cupDAO().perDay(days.get(i))), true, 7, false);
        StaticLabelsFormatter staticLabelsFormatterb = new StaticLabelsFormatter(graphc);
        String[] stringsb = days.toArray(new String[days.size()]);
        staticLabelsFormatterb.setHorizontalLabels(stringsb);
        graphc.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatterb);
        seriesb.setSpacing(5);
        graphc.addSeries(seriesb);*/

    }

}
