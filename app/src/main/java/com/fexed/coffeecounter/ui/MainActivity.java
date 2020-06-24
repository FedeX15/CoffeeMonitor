package com.fexed.coffeecounter.ui;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.viewpager.widget.ViewPager;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.fexed.coffeecounter.db.AppDatabase;
import com.fexed.coffeecounter.db.DBDownloader;
import com.fexed.coffeecounter.db.DBMigrations;
import com.fexed.coffeecounter.ui.adapters.CupRecviewAdapter;
import com.fexed.coffeecounter.ui.adapters.TypeRecviewAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonOutsideTouchListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class MainActivity extends AppCompatActivity {
    public static SharedPreferences state;
    public static AppDatabase db;
    public static String dbpath;

    private ViewPager viewPager;

    public ImageView currentimageview;
    public Bitmap currentbitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        state = this.getSharedPreferences(getString(R.string.apppkg), MODE_PRIVATE);
        state.edit().putBoolean("isintypes", false).apply();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "typedb")
                .allowMainThreadQueries() //FIXME
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .addMigrations(DBMigrations.MIGRATION_19_20,
                        DBMigrations.MIGRATION_20_21,
                        DBMigrations.MIGRATION_21_22,
                        DBMigrations.MIGRATION_22_23,
                        DBMigrations.MIGRATION_23_24,
                        DBMigrations.MIGRATION_24_25)
                .build();
        dbpath = getDatabasePath("typedb").getPath();
        updateDefaultDatabase();

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter =
                new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        //Tutorial balloons
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        state.edit().putBoolean("isintypes", false).apply();
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
                            balloon.showAlignBottom(findViewById(R.id.action_add));
                            state.edit().putBoolean("statstutorial", false).apply();
                        }
                        break;
                    case 1:
                        state.edit().putBoolean("isintypes", true).apply();
                        if (state.getBoolean("typestutorial", true)) {
                            final Balloon balloon = new Balloon.Builder(getApplicationContext())
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
                            state.edit().putBoolean("typestutorial", false).apply();
                        }
                        break;
                    case 2:
                        state.edit().putBoolean("isintypes", false).apply();
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
                            state.edit().putBoolean("cupstutorial", false).apply();
                        }
                        break;
                    case 3:
                        state.edit().putBoolean("isintypes", false).apply();
                        break;
                }
            }
        });

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                        Cup cup = new Cup(elem.getKey());
                        cup = geoTag(cup);
                        db.cupDAO().insert(cup);
                        graphUpdater();
                        RecyclerView cupsRecView = viewPager.findViewById(R.id.cupsrecview);
                        if (cupsRecView != null) cupsRecView.setAdapter(new CupRecviewAdapter(db, 0));
                        RecyclerView typesRecView = viewPager.findViewById(R.id.recview);
                        if (typesRecView != null) typesRecView.setAdapter(new TypeRecviewAdapter(db, typesRecView, state));

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
                if (state.getBoolean("isintypes", false)) {
                    addNewType();
                } else {
                    addCup();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
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
                                final Balloon balloon = new Balloon.Builder(MainActivity.this)
                                        .setText(MainActivity.this.getString(R.string.nameemptyalert))
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
                                //cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                                //typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview, state));
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
                } catch (Exception e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
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

    public void addNewType() {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(findViewById(R.id.action_favs).getContext());
        final View form = getLayoutInflater().inflate(R.layout.addtypedialog, null);
        final TextView literstxt = form.findViewById(R.id.ltrsmgtext);
        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);
        final ImageView typeimage = form.findViewById(R.id.typeimage);

        state.edit().putInt("qnt", 0).apply();
        state.edit().putString("suffix", (liquidckbx.isChecked()) ? " ml" : " mg").apply();
        String str = state.getInt("qnt", 0) + state.getString("suffix", " ml");
        literstxt.setText(str);

        ImageButton addbtn = form.findViewById(R.id.incrbtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qnt = state.getInt("qnt", 0);
                qnt += 5;
                state.edit().putInt("qnt", qnt).apply();
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
                state.edit().putInt("qnt", qnt).apply();
                String str = qnt + state.getString("suffix", " ml");
                literstxt.setText(str);
            }
        });

        liquidckbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                state.edit().putString("suffix", (isChecked) ? " ml" : " mg").apply();
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

        if (state.getBoolean("addtypetutorial", true)) {
            final Balloon qrballoon = new Balloon.Builder(this)
                    .setText(getString(R.string.scanqr))
                    .setWidthRatio(0.25f)
                    .setBackgroundColorResource(R.color.colorBgDark)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setArrowVisible(true)
                    .setArrowOrientation(ArrowOrientation.TOP)
                    .setArrowPosition(0.4f)
                    .build();
            qrballoon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                @Override
                public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    qrballoon.dismiss();
                }
            });
            qrballoon.showAlignBottom(form.findViewById(R.id.scanqrbtn));
            final Balloon defballoon = new Balloon.Builder(this)
                    .setText(getString(R.string.choosedefault))
                    .setWidthRatio(0.25f)
                    .setBackgroundColorResource(R.color.colorBgDark)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setArrowVisible(true)
                    .setArrowOrientation(ArrowOrientation.TOP)
                    .setArrowPosition(0.6f)
                    .build();
            defballoon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                @Override
                public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    defballoon.dismiss();
                }
            });
            defballoon.showAlignBottom(form.findViewById(R.id.defaultbtn));
            final Balloon databalloon = new Balloon.Builder(this)
                    .setText(getString(R.string.fillfields))
                    .setWidthRatio(0.65f)
                    .setBackgroundColorResource(R.color.colorBgDark)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setArrowVisible(true)
                    .setArrowOrientation(ArrowOrientation.BOTTOM)
                    .build();
            databalloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                @Override
                public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    databalloon.dismiss();
                }
            });
            databalloon.showAlignTop(form.findViewById(R.id.nametxt));
            //editor.putBoolean("addtypetutorial", false).apply();
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
                    final Balloon balloon = new Balloon.Builder(MainActivity.this)
                            .setText(MainActivity.this.getString(R.string.nameemptyalert))
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

                    RecyclerView typesRecView = viewPager.findViewById(R.id.recview);
                    if (typesRecView != null) typesRecView.setAdapter(new TypeRecviewAdapter(db, typesRecView, state));

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
                for (Coffeetype type : db.coffetypeDao().getAll()) {
                    if (type.isDefaulttype()) defaultindb.add(type.getName());
                }
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.defaultdbtitle);
                    builder.setItems(namelist.toArray(new CharSequence[namelist.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.coffetypeDao().insert(defaultlist.get(i));
                            RecyclerView typesRecView = viewPager.findViewById(R.id.recview);
                            if (typesRecView != null) typesRecView.setAdapter(new TypeRecviewAdapter(db, typesRecView, state));
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
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt(getString(R.string.scanqr));
                integrator.setOrientationLocked(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.initiateScan();
                dialog.dismiss();
            }
        });
    }

    private void addCup() {
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
                        Cup cup = new Cup(list.get(pos).getKey(), date, day);
                        cup = geoTag(cup);
                        db.cupDAO().insert(cup);
                        graphUpdater();
                        RecyclerView cupsRecView = viewPager.findViewById(R.id.cupsrecview);
                        if (cupsRecView != null) cupsRecView.setAdapter(new CupRecviewAdapter(db, 0));
                    }
                });
                builder.show();
            }
        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
        StartTime.show();
    }

    public Cup geoTag(Cup cup) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setCostAllowed(false);
            String provider = locationManager.getBestProvider(criteria, true);
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                cup.setLongitude(location.getLongitude());
                cup.setLatitude(location.getLatitude());
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Errore location", Toast.LENGTH_SHORT).show(); //TODO error message
            }
            return cup;
        }
        return cup;
    }

    public void graphUpdater() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 0);
        if (fragment instanceof StatFragment) ((StatFragment) fragment).graphUpdater();
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

    public String getStringFromLocalDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    private void updateDefaultDatabase() {
        try {
            Locale locale = Locale.getDefault();
            String dbtxt;
            if (locale.getLanguage().equals("it"))
                dbtxt = new DBDownloader(state).execute("https://fexed.github.io/db/it/defaultcoffeetypes").get();
            else
                dbtxt = new DBDownloader(state).execute("https://fexed.github.io/db/en/defaultcoffeetypes").get();

            if (dbtxt != null) {
                if (db.coffetypeDao().getAll().size() == 0) {
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        db.coffetypeDao().insert(new Coffeetype(strtype[0], Integer.parseInt(strtype[2]), strtype[1], Boolean.parseBoolean(strtype[3]), strtype[4], Float.parseFloat(strtype[5]), null, true));
                    }
                    Toast.makeText(getApplicationContext(), R.string.dbupdated, Toast.LENGTH_SHORT).show();
                } else {
                    List<Coffeetype> coffeelist = db.coffetypeDao().getAll();
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        Coffeetype type = null;
                        for (Coffeetype listtype : coffeelist) {
                            if (listtype.getName().equals(strtype[0])) {
                                type = listtype;
                                break;
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
                    Toast.makeText(getApplicationContext(), R.string.dbupdated, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.dbupdatefailed, Toast.LENGTH_SHORT).show();
        }
    }
}