package com.crontiers.pillife.Adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.R;

public class RecyclerItemViewHolder extends RecyclerView.ViewHolder implements MvConfig {

    public ImageView imageView1;
    public TextView itemTextView2, itemTextView3;
    public RadioButton radioButton;
    public CardView cardView1;

    public RecyclerItemViewHolder(final View parent) {
        super(parent);
        imageView1          = parent.findViewById(R.id.imageView1);
//        itemTextView1       = parent.findViewById(R.id.itemTextView1);
        radioButton         = parent.findViewById(R.id.radioButton);
        itemTextView2       = parent.findViewById(R.id.itemTextView2);
        itemTextView3       = parent.findViewById(R.id.itemTextView3);
        cardView1           = parent.findViewById(R.id.cardView1);
    }

}
