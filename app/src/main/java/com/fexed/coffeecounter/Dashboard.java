package com.fexed.coffeecounter;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
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
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;
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
                // TODO icone nel menù di navigazione
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

                                    SurfaceView sf = findViewById(R.id.surfaceView);

                                    Canvas canvas = new Canvas();
                                    Paint paint = new Paint();
                                    paint.setStyle(Paint.Style.FILL);

                                    paint.setColor(getResources().getColor(R.color.colorBgDark));
                                    canvas.drawPaint(paint);

                                    paint.setColor(getResources().getColor(R.color.colorPrimary));
                                    canvas.drawCircle(20, 20, 15, paint);

                                    sf.draw(canvas);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        /*MobileAds.initialize(this, "ca-app-pub-9387595638685451~9345692620");
        AdView mAdView = findViewById(R.id.banner1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }

    public void graphInitializer() {
    }

    public void graphUpdater() {

    }
}
