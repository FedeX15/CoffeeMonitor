package com.fexed.coffeecounter;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
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
        for (Coffeetype type : db.coffetypeDao().getAll())
            allcups.addAll(db.cupDAO().getAll(type.getKey()));
        this.mDataset = allcups;
        this.types = db.coffetypeDao().getAll();
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
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
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
        holder.txtv.setText(str);
        holder.txtv.setOnLongClickListener(new View.OnLongClickListener() {
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
        public TextView txtv;

        public ViewHolder(TextView v) {
            super(v);
            txtv = v;
        }
    }
}
