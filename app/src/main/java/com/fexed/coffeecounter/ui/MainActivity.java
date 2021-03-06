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
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import androidx.viewpager.widget.ViewPager;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.fexed.coffeecounter.db.DBAccess;
import com.fexed.coffeecounter.db.DBDownloader;
import com.fexed.coffeecounter.sys.SaveImageToInternalTask;
import com.fexed.coffeecounter.sys.widget.AddWidgetDialog;
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

import java.io.FileNotFoundException;
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
 * Main activity of the app. Initializes the AppDatabase, SharedPreferences and the ViewPager
 * Created by Federico Matteoni on 22/06/2020
 */
public class MainActivity extends AppCompatActivity {
    /**
     * App state, for keeping tracks of user's preferences
     */
    public static SharedPreferences state;

    /**
     * Used to access the AppDatabase trough AsyncTasks and Threads
     */
    public static DBAccess db;

    /**
     * Stores the path where the typedb.db file is stored, for backup
     */
    public static String dbpath;

    /**
     * The app's ViewPager
     */
    private ViewPager viewPager;

    /**
     * ImageView where to show the choosen image
     */
    public ImageView currentimageview;

    /**
     * The current choosen image
     */
    public Bitmap currentbitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences
        state = this.getSharedPreferences(getString(R.string.apppkg), MODE_PRIVATE);
        state.edit().putBoolean("isintypes", false).apply();

        //Database
        db = new DBAccess(getApplication());
        dbpath = getDatabasePath("typedb").getPath();
        updateDefaultDatabase();

        createShortcuts();

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
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.geoasktitle));
            builder.setIcon(R.drawable.ic_info);

            builder.setMessage(R.string.geoask);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            });
            builder.create();
            builder.show();
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
                try {
                    final List<Coffeetype> list = db.getFavs().get();
                    for (Coffeetype type : list)
                        popup.getMenu().add(1, list.indexOf(type), 0, type.getName());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int pos = item.getItemId();
                            Coffeetype elem = list.get(pos);
                            Cup cup = new Cup(elem.getKey());
                            cup = geoTag(cup);
                            db.insertCup(cup);
                            graphUpdater();
                            RecyclerView cupsRecView = viewPager.findViewById(R.id.cupsrecview);
                            if (cupsRecView != null)
                                try {
                                    cupsRecView.setAdapter(new CupRecviewAdapter(db, -1));
                                } catch (Exception ignored) {}
                            RecyclerView typesRecView = viewPager.findViewById(R.id.recview);
                            if (typesRecView != null)
                                try {
                                    typesRecView.setAdapter(new TypeRecviewAdapter(MainActivity.this, db, typesRecView, state));
                                } catch (Exception ignored) {}

                            return true;
                        }
                    });
                    popup.show();
                } catch (ExecutionException | InterruptedException ignored) {}
                break;
            case R.id.action_notifs:
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
        if (result != null) { //QR
            if (result.getContents() == null) {
                Toast.makeText(this, R.string.annulla, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String str = result.getContents();
                    String[] strtype = str.split("::");
                    final Coffeetype coffeetype = new Coffeetype(
                            strtype[0],
                            Integer.parseInt(strtype[2]),
                            strtype[1],
                            Boolean.parseBoolean(strtype[3]),
                            strtype[4],
                            Float.parseFloat(strtype[5]),
                            null,
                            false);
                    final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
                    final View form = View.inflate(this, R.layout.dialog_addtype, null);
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
                                    SaveImageToInternalTask saveTask = (SaveImageToInternalTask) new SaveImageToInternalTask(getApplicationContext()).execute(currentbitmap);
                                    String uri = new ContextWrapper(getApplicationContext()).getDir("images", Context.MODE_PRIVATE).getAbsolutePath() + "/" + currentbitmap.hashCode() + ".png";
                                    coffeetype.setImg(uri);
                                    currentbitmap = null;
                                }
                                coffeetype.setDefaulttype(false);

                                db.insertType(coffeetype);
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
            if (requestCode == 9) { //Image selected
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
                        Toast.makeText(this, R.string.fileopenerror, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /**
     * Prompts the user with a custom {@code Dialog} for adding a new {@link Coffeetype} into the
     * database. The user can fill the dialog with custom informations, scan a QR code or pick a
     * {@link Coffeetype} from the default list
     */
    public void addNewType() {
        final AlertDialog.Builder dialogbuilder =
                new AlertDialog.Builder(this);

        final View form = getLayoutInflater().inflate(R.layout.dialog_addtype, null);
        final TextView literstxt = form.findViewById(R.id.ltrsmgtext);
        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);
        final ImageView typeimage = form.findViewById(R.id.typeimage);
        ImageButton rmvbtn = form.findViewById(R.id.decrbtn);
        ImageButton addbtn = form.findViewById(R.id.incrbtn);
        Button positive = form.findViewById(R.id.confirmbtn);
        Button negative = form.findViewById(R.id.cancelbtn);
        ImageButton fromdefaultbtn = form.findViewById(R.id.defaultbtn);
        ImageButton qrscan = form.findViewById(R.id.scanqrbtn);

        //Cleaning the state
        state.edit().putInt("qnt", 0).apply();
        state.edit().putString("suffix", (liquidckbx.isChecked()) ? " ml" : " mg").apply();
        String str = state.getInt("qnt", 0) + state.getString("suffix", " ml");
        literstxt.setText(str);

        dialogbuilder.setView(form);
        dialogbuilder.create();
        final AlertDialog dialog = dialogbuilder.show();
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.incrbtn:
                        int qnt = state.getInt("qnt", 0);
                        qnt += 5;
                        state.edit().putInt("qnt", qnt).apply();
                        String str = qnt + state.getString("suffix", " ml");
                        literstxt.setText(str);
                        break;
                    case R.id.decrbtn:
                        qnt = state.getInt("qnt", 0);
                        qnt = (qnt == 0) ? 0 : qnt - 5;
                        state.edit().putInt("qnt", qnt).apply();
                        str = qnt + state.getString("suffix", " ml");
                        literstxt.setText(str);
                        break;
                    case R.id.defaultbtn:
                        String dbtxt = state.getString("defaultdb", null);
                        final ArrayList<Coffeetype> defaultlist = new ArrayList<>();
                        ArrayList<String> defaultindb = new ArrayList<>();
                        try {
                            List<Coffeetype> list = db.getTypes().get();
                            for (Coffeetype type : list) {
                                if (type.isDefaulttype()) defaultindb.add(type.getName());
                            }
                            ArrayList<String> namelist = new ArrayList<>();
                            if (dbtxt != null) {
                                for (String str2 : dbtxt.split("\n")) {
                                    String[] strtype = str2.split("::");
                                    if (strtype.length == 6) {
                                        Coffeetype type = new Coffeetype(
                                                strtype[0],
                                                Integer.parseInt(strtype[2]),
                                                strtype[1],
                                                Boolean.parseBoolean(strtype[3]),
                                                strtype[4],
                                                Float.parseFloat(strtype[5]),
                                                null,
                                                true
                                        );
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
                                        db.insertType(defaultlist.get(i));
                                        RecyclerView typesRecView = null;
                                        Fragment fragment = getSupportFragmentManager()
                                                .findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 1);
                                        if (fragment instanceof TypesFragment) typesRecView = fragment.getView().findViewById(R.id.recview);
                                        if (typesRecView != null) {
                                            try {
                                                typesRecView.setAdapter(new TypeRecviewAdapter(MainActivity.this, db, typesRecView, state));
                                            } catch (InterruptedException | ExecutionException ignored) {
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.defaultdbnotavailalert, Toast.LENGTH_LONG).show();
                            }
                        } catch (ExecutionException | InterruptedException ignored) {}
                        break;
                    case R.id.scanqrbtn:
                        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                        integrator.setPrompt(getString(R.string.scanqr));
                        integrator.setOrientationLocked(true);
                        integrator.setCaptureActivity(CaptureActivityPortrait.class);
                        integrator.initiateScan();
                        dialog.dismiss();
                        break;
                    case R.id.cancelbtn:
                        dialog.dismiss();
                        break;
                    case R.id.confirmbtn:
                        EditText nameedittxt = form.findViewById(R.id.nametxt);
                        EditText descedittxt = form.findViewById(R.id.desctxt);
                        EditText sostedittxt = form.findViewById(R.id.sosttxt);
                        EditText pricetedittxt = form.findViewById(R.id.pricetxt);
                        CheckBox liquidckbx = form.findViewById(R.id.liquidcheck);

                        String name = nameedittxt.getText().toString();
                        if (name.isEmpty()) { //Error if the type name is empty
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
                            //Retrieves informations and saves the new type
                            int liters = state.getInt("qnt", 0);
                            String desc = descedittxt.getText().toString();
                            String sostanza = sostedittxt.getText().toString();
                            float price = Float.parseFloat(pricetedittxt.getText().toString());
                            boolean liquid = liquidckbx.isChecked();
                            String bmpuri = "";
                            if (currentbitmap != null) {
                                SaveImageToInternalTask saveTask = (SaveImageToInternalTask) new SaveImageToInternalTask(getApplicationContext()).execute(currentbitmap);
                                bmpuri = new ContextWrapper(getApplicationContext())
                                        .getDir("images", Context.MODE_PRIVATE)
                                        .getAbsolutePath() + "/" + currentbitmap.hashCode() + ".png";
                                currentbitmap = null;
                            }
                            Coffeetype newtype = new Coffeetype(
                                    name,
                                    liters,
                                    desc,
                                    liquid,
                                    sostanza,
                                    price,
                                    bmpuri);

                            db.insertType(newtype);

                            RecyclerView typesRecView = null;
                            Fragment fragment = getSupportFragmentManager()
                                    .findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 1);
                            if (fragment instanceof TypesFragment) typesRecView = fragment.getView().findViewById(R.id.recview);
                            if (typesRecView != null) {
                                try {
                                    typesRecView.setAdapter(new TypeRecviewAdapter(MainActivity.this, db, typesRecView, state));
                                } catch (InterruptedException | ExecutionException ignored) {
                                }
                            }
                            dialog.dismiss();
                        }
                        break;

                }
            }
        };

        addbtn.setOnClickListener(clickListener);
        rmvbtn.setOnClickListener(clickListener);
        positive.setOnClickListener(clickListener);
        negative.setOnClickListener(clickListener);
        fromdefaultbtn.setOnClickListener(clickListener);
        qrscan.setOnClickListener(clickListener);

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

        //Tutorial balloons
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
    }

    /**
     * Prompts the user with a {@code Dialog} to pick a date and then a type. Then, it adds a
     * {@link Cup} of the chosen {@link Coffeetype} and the chosen date into the database.
     */
    private void addCup() {
        AsyncTask<Void, Void, List<Coffeetype>> typeslist = db.getTypes();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selecttype);
        Calendar cld = Calendar.getInstance();
        try {
            final List<Coffeetype> list = typeslist.get();
            final DatePickerDialog StartTime = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf =new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
                    final String date = sdf.format(newDate.getTime());
                    sdf = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
                    final String day = sdf.format(newDate.getTime());
                    builder.setAdapter(
                            new ArrayAdapter<>(getApplicationContext(), R.layout.type_element, list),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            Cup cup = new Cup(list.get(pos).getKey(), date, day);
                            cup = geoTag(cup);
                            db.insertCup(cup);
                            graphUpdater();
                            RecyclerView cupsRecView = viewPager.findViewById(R.id.cupsrecview);
                            if (cupsRecView != null)
                                try {
                                    cupsRecView.setAdapter(new CupRecviewAdapter(db, -1));
                                } catch (InterruptedException | ExecutionException ignored) {}
                        }
                    });
                    builder.show();
                }
            }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
            StartTime.show();
        } catch (InterruptedException | ExecutionException ignored) {}
    }

    /**
     * If the user has granted permission, retrieves the last location of the device and applies it
     * to the {@link Cup}. Otherwise, it does nothing.
     * @param cup the {@link Cup} to be geotagged
     * @return the geotagged {@link Cup} if the user granted permission and no exception occurred,
     * otherwise does nothing and simply returns the parameter.
     */
    public Cup geoTag(Cup cup) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                Toast.makeText(this,
                        R.string.locationerror, Toast.LENGTH_SHORT).show();
            }
            return cup;
        }
        return cup;
    }

    /**
     * Looks for the {@link StatFragment} and updates its graphs
     */
    public void graphUpdater() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 0);
        if (fragment instanceof StatFragment) ((StatFragment) fragment).graphUpdater();
    }

    /**
     * Generates a {@code String} with some quick stats and a tip. The tip will be a "be careful"
     * message if the user registered more than 5 cups today, otherwise it will be a fun fact
     *
     * {@code www.fda.gov/consumers/consumer-updates/spilling-beans-how-much-caffeine-too-much}
     * @return the generated {@code String}
     */
    public String generateTip() {
        int maxCupsPerDay = 5;
        //int maxCaffeinePerDay = 400;
        AsyncTask<String, Void, List<Cup>> cupsTodayList =
                db.getCups(getStringFromLocalDate(Calendar.getInstance().getTime()));
        int cupsToday = 0;
        int mlToday = 0;
        String tip = getString(R.string.tipsplaceholder);

        try {
            cupsToday = cupsTodayList.get().size();
            for (Cup cup : cupsTodayList.get()) {
                mlToday += db.getType(cup.getTypekey()).get().getLiters();
            }
        } catch (Exception ignored) {}

        if (cupsToday > maxCupsPerDay)
            tip = getString(R.string.summary, cupsToday, mlToday) + " ml\n\n" +
                    getString(R.string.toomuchcupstip);
        else {
            String[] funfacts = getResources().getStringArray(R.array.funfacts);
            Random rnd = new Random();
            int i = rnd.nextInt(funfacts.length);
            tip = getString(R.string.allisgood) + "\n" +
                    getString(R.string.summary, cupsToday, mlToday) + " ml\n\n" +
                    funfacts[i];
        }

        return tip;
    }

    public String getStringFromLocalDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    /**
     * Retrieves the default database from the internet if it's been updated, then stores in the
     * {@code state} of the app
     */
    private void updateDefaultDatabase() {
        AsyncTask<Void, Void, List<Coffeetype>> typeslist = db.getTypes();
        try {
            Locale locale = Locale.getDefault();
            String dbtxt;
            if (locale.getLanguage().equals("it")) {
                dbtxt = new DBDownloader(state)
                        .execute("https://fexed.github.io/db/it/defaultcoffeetypes").get();
            } else {
                dbtxt = new DBDownloader(state)
                        .execute("https://fexed.github.io/db/en/defaultcoffeetypes").get();
            }

            if (dbtxt != null) {
                if (typeslist.get().size() == 0) {
                    for (String str : dbtxt.split("\n")) {
                        String[] strtype = str.split("::");
                        db.insertType(new Coffeetype(
                                strtype[0], //Name
                                Integer.parseInt(strtype[2]), //Liters
                                strtype[1], //Description
                                Boolean.parseBoolean(strtype[3]), //Is liquid
                                strtype[4], //Substance
                                Float.parseFloat(strtype[5]), //Price
                                null, //Image
                                true) //Is default
                        );
                    }
                    Toast.makeText(
                            getApplicationContext(),R.string.dbupdated, Toast.LENGTH_SHORT).show();
                } else {
                    List<Coffeetype> coffeelist = typeslist.get();
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
                            db.updateTypes(type);
                        }
                    }
                    Toast.makeText(getApplicationContext(),
                            R.string.dbupdated, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(
                    getApplicationContext(), R.string.dbupdatefailed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates the app shortcuts (quick actions accessible by long pressing the app icon on the home
     * screen.
     * As per guidelines, adds a max number of 4 shortcuts from the favourite types of the user
     */
    private void createShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= 26) { //Only available on SDK >= 26
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            try {
                List<Coffeetype> favs = db.getFavs().get();
                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
                ShortcutInfo newShortcut;
                for (Coffeetype type : favs) {
                    newShortcut = new ShortcutInfo.Builder(this, type.getKey() + "")
                            .setShortLabel(type.getName())
                            .setIcon(Icon.createWithResource(this, R.drawable.ic_favstar))
                            .setIntent(new Intent(this, AddWidgetDialog.class)
                                    .setAction(Intent.ACTION_VIEW)
                                    .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                    .putExtra("TYPENAME", type.getName()))
                            .build();
                    shortcuts.add(newShortcut);
                    if (shortcuts.size() == 4) break;
                }
                shortcutManager.setDynamicShortcuts(shortcuts);
            } catch (ExecutionException | InterruptedException ignored) {}
        }
    }
}