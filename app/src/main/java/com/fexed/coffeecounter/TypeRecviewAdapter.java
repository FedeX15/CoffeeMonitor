package com.fexed.coffeecounter;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fedex on 01/12/2017.
 */

public class TypeRecviewAdapter extends RecyclerView.Adapter<TypeRecviewAdapter.ViewHolder> {
    AppDatabase db;
    private List<Coffeetype> mDataset;

    public TypeRecviewAdapter(AppDatabase db) {
        this.mDataset = db.coffetypeDao().getAll();
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
    public TypeRecviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.coffee_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.nameTextView.setText(mDataset.get(position).getName());

        final TextView cupstxtv = holder.cupsTextView;
        cupstxtv.setText("" + mDataset.get(position).getQnt());

        TextView desctxtv = holder.descTextView;
        desctxtv.setText(mDataset.get(position).toBigString()/* + "\n\n\n" + db.cupDAO().getAll(mDataset.get(position).getKey()).toString()*/);

        Button addbtn = holder.mCardView.findViewById(R.id.addbtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataset.get(position).setQnt(mDataset.get(position).getQnt() + 1);
                db.coffetypeDao().update(mDataset.get(position));
                db.cupDAO().insert(new Cup(mDataset.get(position).getKey()));
                cupstxtv.setText("" + mDataset.get(position).getQnt());
            }
        });
        addbtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDataset.get(position).setQnt(mDataset.get(position).getQnt() + 5);
                db.coffetypeDao().update(mDataset.get(position));
                for (int i = 0; i < 5; i++) db.cupDAO().insert(new Cup(mDataset.get(position).getKey()));
                cupstxtv.setText("" + mDataset.get(position).getQnt());
                return true;
            }
        });

        Button removebtn = holder.mCardView.findViewById(R.id.removebtn);
        removebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = mDataset.get(position).getQnt();
                mDataset.get(position).setQnt((n == 0) ? 0 : n-1);
                db.coffetypeDao().update(mDataset.get(position));
                db.cupDAO().deleteMostRecent(mDataset.get(position).getKey());
                cupstxtv.setText("" + mDataset.get(position).getQnt());
            }
        });
        removebtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDataset.get(position).setQnt(0);
                db.coffetypeDao().update(mDataset.get(position));
                db.cupDAO().deleteAll(mDataset.get(position).getKey());
                cupstxtv.setText("" + mDataset.get(position).getQnt());
                return true;
            }
        });

        holder.nameTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(v.getContext());
                dialogbuilder.setMessage("Eliminare il tipo \"" + mDataset.get(position).getName() + "?")
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

        holder.favbtn.setImageResource((mDataset.get(position).isFav()) ? R.drawable.ic_favstarfull : R.drawable.ic_favstarempty);
        holder.favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataset.get(position).setFav(!(mDataset.get(position).isFav()));
                db.coffetypeDao().update(mDataset.get(position));
                ImageButton btn = (ImageButton) v;
                btn.setImageResource((mDataset.get(position).isFav()) ? R.drawable.ic_favstarfull : R.drawable.ic_favstarempty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeAt(int position) {
        db.cupDAO().deleteAll(mDataset.get(position).getKey());
        db.coffetypeDao().delete(mDataset.get(position));
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView nameTextView;
        public TextView cupsTextView;
        public TextView descTextView;
        public ImageButton favbtn;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            nameTextView = mCardView.findViewById(R.id.nameTxtV);
            cupsTextView = mCardView.findViewById(R.id.cups_textv);
            descTextView = mCardView.findViewById(R.id.desctxtv);
            favbtn = mCardView.findViewById(R.id.favbtn);
        }
    }


}
