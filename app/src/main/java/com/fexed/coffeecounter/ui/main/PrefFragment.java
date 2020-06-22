package com.fexed.coffeecounter.ui.main;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fexed.coffeecounter.Coffeetype;
import com.fexed.coffeecounter.Cup;
import com.fexed.coffeecounter.FileProvider;
import com.fexed.coffeecounter.MainActivity;
import com.fexed.coffeecounter.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrefFragment extends Fragment implements View.OnClickListener {
    public static PrefFragment newInstance(int index) {
        PrefFragment fragment = new PrefFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancestate) {
        View root = inflater.inflate(R.layout.activity_pref, container, false);
        Button resettutorialbtn = root.findViewById(R.id.resettutorialbtn);
        resettutorialbtn.setOnClickListener(this);
        Button backupbtn = root.findViewById(R.id.backupbtn);
        backupbtn.setOnClickListener(this);
        Button exportdatabtn = root.findViewById(R.id.exportdatabtn);
        exportdatabtn.setOnClickListener(this);
        Button restorebtn = root.findViewById(R.id.restorebtn);
        restorebtn.setOnClickListener(this);
        Button statbtn = root.findViewById(R.id.statbtn);
        statbtn.setOnClickListener(this);
        Button resetdbbtn = root.findViewById(R.id.resetdbbtn);
        resetdbbtn.setOnClickListener(this);

        final TextView notiftimetxtv = root.findViewById(R.id.notiftimetxt);
        if (MainActivity.state.getBoolean("notifonoff", true))
            notiftimetxtv.setText(String.format(Locale.getDefault(), "%d:%d", MainActivity.state.getInt("notifhour", 20), MainActivity.state.getInt("notifmin", 30)));
        else notiftimetxtv.setText("--:--");
        notiftimetxtv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (MainActivity.state.getBoolean("notifonoff", true)) {
                    // Get Current Time
                    final Calendar c = Calendar.getInstance();
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            MainActivity.state.edit().putInt("notifhour", hourOfDay).apply();
                            MainActivity.state.edit().putInt("notifmin", minute).apply();
                            notiftimetxtv.setText(String.format(Locale.getDefault(), "%02d:%02d", MainActivity.state.getInt("notifhour", 20), MainActivity.state.getInt("notifmin", 30)));
                            //startAlarmBroadcastReceiver(getApplicationContext()); //TODO
                        }
                    }, mHour, mMinute, true);
                    timePickerDialog.show();
                    return true;
                } else {
                    Toast.makeText(getContext(), getString(R.string.notifica_giornaliera) + " OFF", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });

        final Switch dailynotifswitch = root.findViewById(R.id.dailynotifswitch);
        dailynotifswitch.setChecked(MainActivity.state.getBoolean("notifonoff", true));
        dailynotifswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.state.edit().putBoolean("notifonoff", isChecked).apply();

                if (isChecked) { //TODO
                    //startAlarmBroadcastReceiver(getApplicationContext());
                    notiftimetxtv.setText(String.format(Locale.getDefault(), "%d:%d", MainActivity.state.getInt("notifhour", 20), MainActivity.state.getInt("notifmin", 30)));
                } else {
                    //stopAlarmBroadcastReceiver(getApplicationContext());
                    notiftimetxtv.setText("--:--");
                }
            }
        });

        final Switch historyswitch = root.findViewById(R.id.historybarswitch);
        historyswitch.setChecked(MainActivity.state.getBoolean("historyline", false));
        historyswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MainActivity.state.edit().putBoolean("historyline", b).apply();
                //graphUpdater();
            }
        });

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resettutorialbtn:
                MainActivity.state.edit().putBoolean("typestutorial", true).apply();
                MainActivity.state.edit().putBoolean("cupstutorial", true).apply();
                MainActivity.state.edit().putBoolean("qrtutorial", true).apply();
                MainActivity.state.edit().putBoolean("addtypetutorial", true).apply();
                MainActivity.state.edit().putBoolean("statstutorial", true).apply();
                break;
            case R.id.backupbtn:
            case R.id.exportdatabtn:
                Intent sharefile = new Intent(Intent.ACTION_SEND);
                try {
                    File file = saveDbToExternalStorage();
                    if (file != null && file.exists()) { //If not null should always exists
                        String type = "application/octet-stream"; //generic file

                        sharefile.setType(type);
                        sharefile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(), "com.fexed.coffeecounter.fileprovider", file));
                        startActivity(Intent.createChooser(sharefile, "Share File"));
                    } else {
                        Toast.makeText(getContext(), "Database not found", Toast.LENGTH_SHORT).show(); //TODO error message
                    }
                } catch (IOException ex) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show(); //TODO error message
                }
                break;
            case R.id.restorebtn:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("file/*");
                startActivityForResult(i, 10);
                break;
            case R.id.statbtn:
                int milliliterstotal = 0;
                int cupstotal = 0;
                StringBuilder cupsstat = new StringBuilder();

                for (Coffeetype type : MainActivity.db.coffetypeDao().getAll()) {
                    cupsstat.append(type.getName()).append("\n");
                    milliliterstotal += (type.getLiters() * type.getQnt());
                    cupstotal += type.getQnt();
                    for (Cup cup : MainActivity.db.cupDAO().getAll(type.getKey()))
                        cupsstat.append("\t[").append(cup.toString()).append("]\n");
                }

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(getContext());
                dialogbuilder.setMessage("Bevuti in totale " + milliliterstotal + " ml in " + cupstotal + " tazzine.\n\n" + cupsstat)
                        .setNeutralButton("OK", null);
                dialogbuilder.create();
                dialogbuilder.show();
                break;
            case R.id.resetdbbtn:
                dialogbuilder = new AlertDialog.Builder(getContext());
                dialogbuilder.setMessage(getString(R.string.resetdb) + "?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.db.cupDAO().nuke();
                                MainActivity.db.coffetypeDao().nuke();
                                /*updateDefaultDatabase();
                                cupsRecview.setAdapter(new CupRecviewAdapter(db, 0));
                                typesRecview.setAdapter(new TypeRecviewAdapter(db, typesRecview, state));*/
                                //Snackbar.make(findViewById(R.id.container), "Database resettato", Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Snackbar.make(findViewById(R.id.container), "Annullato", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                dialogbuilder.create();
                dialogbuilder.show();
                break;

        }
    }

    public File saveDbToExternalStorage() throws IOException {
        String currentDBPath = MainActivity.dbpath;
        Log.e("DB", currentDBPath);
        File src = new File(currentDBPath);
        File savepathfile = new File(getContext().getExternalFilesDir(null) + File.separator + "coffeemonitor");
        if (!savepathfile.exists()) savepathfile.mkdir();
        String dstpath = savepathfile.getPath() + File.separator + "coffeemonitordb_" + new SimpleDateFormat("yyyMMddHHmmss").format(new Date()) + ".db";
        File savefile = new File(dstpath);
        savefile.createNewFile();
        try (FileChannel inch = new FileInputStream(src).getChannel(); FileChannel outch = new FileOutputStream(dstpath).getChannel()) {
            inch.transferTo(0, inch.size(), outch);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }

        return savefile;
    }
}
