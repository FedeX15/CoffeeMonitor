package com.fexed.coffeecounter;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fedex on 19/01/2018.
 */

public class CupRecviewAdapter extends RecyclerView.Adapter<CupRecviewAdapter.ViewHolder> {
    AppDatabase db;
    private List<Cup> mDataset;
    private List<Coffeetype> types;

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
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public Coffeetype getTypeFromKey(int key) {
        for (Coffeetype type : types) if (type.getKey() == key) return type;
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String str = getTypeFromKey(mDataset.get(position).getTypekey()).getName() + " @ " + mDataset.get(position).toString();
        Log.d("CUP", str);
        holder.typetxtv.setText(getTypeFromKey(mDataset.get(position).getTypekey()).getName());
        holder.timestamptxtv.setText(mDataset.get(position).toString());
        holder.removebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(view.getContext());
                dialogbuilder.setMessage("Eliminare \"" + str + "?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeAt(position);
                            }
                        })
                        .setNegativeButton("No", null);
                dialogbuilder.create();
                dialogbuilder.show();

                return true;
            }
        });
        holder.cuptextparent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "Edit to be implemented", Snackbar.LENGTH_SHORT).show();
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
