package com.fexed.coffeecounter.ui.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.fexed.coffeecounter.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Federico Matteoni on 19/01/2018.
 */
public class CupRecviewAdapter extends RecyclerView.Adapter<CupRecviewAdapter.ViewHolder> {
    AppDatabase db;
    private List<Cup> mDataset;
    private List<Coffeetype> types;
    private Context context;

    public CupRecviewAdapter(AppDatabase db, int filter) {
        List<Cup> allcups = new ArrayList<>();
        if (filter == -1) {
            this.types = db.coffetypeDao().getAll();
            for (Coffeetype type : this.types) {
                List<Cup> typecups = db.cupDAO().getAll(type.getKey());
                Collections.reverse(typecups);
                allcups.addAll(typecups);
            }
        } else if (filter == -2) {
            this.types = db.coffetypeDao().getAll();
            for (Coffeetype type : this.types) {
                List<Cup> typecups = db.cupDAO().getAll(type.getKey());
                Collections.reverse(typecups);
                if (typecups.size() > 0) allcups.add(typecups.get(0));
            }
        } else {
            this.types = db.coffetypeDao().getAll();
            List<Cup> typecups = db.cupDAO().getAll(types.get(filter).getKey());
            Collections.reverse(typecups);
            allcups.addAll(typecups);
        }
        this.mDataset = allcups;
        this.db = db;
    }

    /*public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }*/

    @Override
    @NonNull
    public CupRecviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.cup_element, parent, false);
        this.context = parent.getContext();
        return new ViewHolder(v);
    }

    public Coffeetype getTypeFromKey(int key) {
        for (Coffeetype type : types) if (type.getKey() == key) return type;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String str;
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.remove:
                        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
                        String str = getTypeFromKey(mDataset.get(position).getTypekey()).getName() + " @ " + mDataset.get(position).toString();
                        dialogbuilder.setMessage(context.getString(R.string.eliminarecup, str))
                                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeAt(holder.getAdapterPosition());
                                    }
                                })
                                .setNegativeButton(R.string.no, null);
                        dialogbuilder.create();
                        dialogbuilder.show();

                        return true;
                    case R.id.cuptextparent:
                        Toast.makeText(context, context.getString(R.string.tienipremutoedittipologia), Toast.LENGTH_SHORT).show();
                        final Cup thiscup = mDataset.get(holder.getAdapterPosition());
                        dialogbuilder = new AlertDialog.Builder(context);
                        View form = LayoutInflater.from(context).inflate(R.layout.dialog_editcup, null, false);
                        final TextView nametxt = form.findViewById(R.id.editcuptype);
                        final TextView datetxt = form.findViewById(R.id.editcupdate);
                        final TextView geotxt = form.findViewById(R.id.editcupgeotag);
                        Button confirmbtn = form.findViewById(R.id.editcupconfirmbtn);
                        Button cancelbtn = form.findViewById(R.id.editcupcancelbtn);

                        View.OnLongClickListener editCupLongClick = new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                switch (v.getId()) {
                                    case R.id.editcuptype:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle(R.string.selecttype);
                                        builder.setAdapter(new ArrayAdapter<>(context, R.layout.type_element, types), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int pos) {
                                                thiscup.setTypekey(types.get(pos).getKey());
                                                nametxt.setText(types.get(pos).getName());
                                            }
                                        });
                                        builder.show();
                                        return true;
                                    case R.id.editcupdate:
                                        final Calendar cld = Calendar.getInstance();
                                        DatePickerDialog newDateDial = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                                            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                                                final Calendar newDate = Calendar.getInstance();
                                                newDate.set(year, monthOfYear, dayOfMonth);
                                                TimePickerDialog newTimeDial = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                        newDate.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
                                                        final String date = sdf.format(newDate.getTime());
                                                        sdf = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
                                                        final String day = sdf.format(newDate.getTime());
                                                        thiscup.setDate(date);
                                                        thiscup.setDay(day);
                                                        datetxt.setText(thiscup.toString());
                                                    }
                                                }, cld.get(Calendar.HOUR_OF_DAY), cld.get(Calendar.MINUTE), true);
                                                newTimeDial.show();
                                            }
                                        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
                                        newDateDial.show();
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        };

                        nametxt.setText(getTypeFromKey(thiscup.getTypekey()).getName());
                        nametxt.setOnLongClickListener(editCupLongClick);
                        datetxt.setText(thiscup.toString());
                        datetxt.setOnLongClickListener(editCupLongClick);
                        if (thiscup.getLongitude() != 0.0) {
                            str = thiscup.getLatitude() + " " + thiscup.getLongitude();
                            geotxt.setText(str);
                        }

                        dialogbuilder.setView(form);
                        dialogbuilder.create();
                        final AlertDialog dialog = dialogbuilder.show();

                        View.OnClickListener editCupClick = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (v.getId()) {
                                    case R.id.editcupconfirmbtn:
                                        mDataset.set(holder.getAdapterPosition(), thiscup);
                                        db.cupDAO().update(thiscup);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    case R.id.editcupcancelbtn:
                                        dialog.dismiss();
                                }
                            }
                        };
                        confirmbtn.setOnClickListener(editCupClick);
                        cancelbtn.setOnClickListener(editCupClick);

                        return false;
                    default:
                        return false;
                }
            }
        };

        holder.typetxtv.setText(getTypeFromKey(mDataset.get(position).getTypekey()).getName());
        holder.timestamptxtv.setText(mDataset.get(position).toString());
        String locString = "";
        if(mDataset.get(position).getLatitude() != 0.0) locString = mDataset.get(position).getLatitude() + " " + mDataset.get(position).getLongitude();
        holder.loctxtv.setText(locString);
        holder.removebtn.setOnLongClickListener(onLongClickListener);
        holder.cuptextparent.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        db.cupDAO().delete(mDataset.get(position));
        for (Coffeetype type : db.coffetypeDao().getAll()) {
            if (type.getKey() == mDataset.get(position).getTypekey()) {
                type.setQnt(type.getQnt() - 1);
                db.coffetypeDao().update(type);
            }
        }
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView typetxtv;
        public TextView timestamptxtv;
        public TextView loctxtv;
        public LinearLayout cuptextparent;
        public ImageButton removebtn;

        public ViewHolder(LinearLayout v) {
            super(v);
            typetxtv = v.findViewById(R.id.typename);
            timestamptxtv = v.findViewById(R.id.timestamp);
            loctxtv = v.findViewById(R.id.locationtxtv);
            cuptextparent = v.findViewById(R.id.cuptextparent);
            removebtn = v.findViewById(R.id.remove);
        }
    }
}
