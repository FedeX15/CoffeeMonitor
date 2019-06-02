package com.fexed.coffeecounter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by fexed on 19/01/2018.
 */

public class CupRecviewAdapter extends RecyclerView.Adapter<CupRecviewAdapter.ViewHolder> {
    AppDatabase db;
    private List<Cup> mDataset;
    private List<Coffeetype> types;
    private Context context;

    public CupRecviewAdapter(AppDatabase db) {
        List<Cup> allcups = new ArrayList<>();
        this.types = db.coffetypeDao().getAll();
        for (Coffeetype type : this.types) {
            List<Cup> typecups = db.cupDAO().getAll(type.getKey());
            Collections.reverse(typecups);
            allcups.addAll(typecups);
        }
        this.mDataset = allcups;
        this.db = db;
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    @Override
    public CupRecviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.cup_element, parent, false);
        this.context = parent.getContext();
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public Coffeetype getTypeFromKey(int key) {
        for (Coffeetype type : types) if (type.getKey() == key) return type;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String str = getTypeFromKey(mDataset.get(position).getTypekey()).getName() + " @ " + mDataset.get(position).toString();

        holder.typetxtv.setText(getTypeFromKey(mDataset.get(position).getTypekey()).getName());
        holder.timestamptxtv.setText(mDataset.get(position).toString());
        holder.removebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(view.getContext());
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
            }
        });
        holder.cuptextparent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context, context.getString(R.string.tienipremutoedittipologia), Toast.LENGTH_SHORT).show();
                final Cup thiscup = mDataset.get(holder.getAdapterPosition());
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
                View form = LayoutInflater.from(context).inflate(R.layout.editcupdialog, null, false);
                final TextView nametxt = form.findViewById(R.id.editcuptype);
                final TextView datetxt = form.findViewById(R.id.editcupdate);
                Button confirmbtn = form.findViewById(R.id.editcupconfirmbtn);
                Button cancelbtn = form.findViewById(R.id.editcupcancelbtn);

                nametxt.setText(getTypeFromKey(thiscup.getTypekey()).getName());
                nametxt.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
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
                    }
                });

                datetxt.setText(thiscup.toString());
                datetxt.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Calendar cld = Calendar.getInstance();
                        DatePickerDialog StartTime = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
                                final String date = sdf.format(newDate.getTime());
                                sdf = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
                                final String day = sdf.format(newDate.getTime());
                                thiscup.setDate(date);
                                thiscup.setDay(day);
                                datetxt.setText(thiscup.toString());

                            }
                        }, cld.get(Calendar.YEAR), cld.get(Calendar.MONTH), cld.get(Calendar.DAY_OF_MONTH));
                        StartTime.show();
                        return true;
                    }
                });

                dialogbuilder.setView(form);
                dialogbuilder.create();
                final AlertDialog dialog = dialogbuilder.show();

                confirmbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDataset.set(holder.getAdapterPosition(), thiscup);
                        db.cupDAO().update(thiscup);
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

                cancelbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        db.cupDAO().delete(mDataset.get(position));
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView typetxtv;
        public TextView timestamptxtv;
        public LinearLayout cuptextparent;
        public Button removebtn;

        public ViewHolder(LinearLayout v) {
            super(v);
            typetxtv = v.findViewById(R.id.typename);
            timestamptxtv = v.findViewById(R.id.timestamp);
            cuptextparent = v.findViewById(R.id.cuptextparent);
            removebtn = v.findViewById(R.id.remove);
        }
    }
}
