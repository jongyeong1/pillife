package com.crontiers.pillife.Adapter;

import android.annotation.SuppressLint;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import com.crontiers.pillife.Dialog.PopupDialog;
import com.crontiers.pillife.Listener.DataClickEventListener;
import com.crontiers.pillife.Listener.FinishClickEventListener;
import com.crontiers.pillife.Model.DataRank;
import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapterDrug extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MvConfig, FinishClickEventListener {

    private final DataClickEventListener dataClickEventListener;
    public List<DataRank> mItemList = new ArrayList<>();
    private PopupDialog popupDialog;
    private int curSelectIdx = -1;
    private RadioButton curSelectButton = null;
    private boolean disableSelect = false;
    private boolean loading = false;

    public RecyclerAdapterDrug(DataClickEventListener dataClickEventListener) {
        this.dataClickEventListener = dataClickEventListener;
    }

    public void clearSelectResult() {
        this.curSelectIdx = -1;
        if(this.curSelectButton != null)
            this.curSelectButton.setChecked(false);
        this.curSelectButton = null;
    }

    public void disableSelect() {
        this.disableSelect = true;
        this.notifyDataSetChanged();
    }

    public void setLoding(boolean set) {
        this.loading = set;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        popupDialog = new PopupDialog(parent.getContext());
        popupDialog.setOnFinishClickEvent(this);

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_drug, parent, false);
        return new RecyclerItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, final int position) {
        final DataRank item = mItemList.get(position);
        final RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;

        // 정확도
//        holder.itemTextView1.setText(((int) Math.floor(item.getAccuracy() * 100)) + "%");

        if(!item.getImage_link().equals("")) {
            // 이미지
            Picasso.get()
                    .load(item.getImage_link())
                    .centerInside()
                    .resize(BOARD_WIDTH, BOARD_HEIGHT)
                    .into(holder.imageView1);
        }
        // 약이름
        SpannableString content = new SpannableString(item.getPill_name());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.itemTextView2.setText(content);

        // 효능
        holder.itemTextView3.setText(item.getCls_name());

        // click
        holder.cardView1.setEnabled(!this.loading);
        holder.cardView1.setOnClickListener(view -> {
            view.setEnabled(false);
            popupDialog.drug_url = item.getHref();
            popupDialog.showDialog(POPUP_TYPE.DRUG, true);
            view.setEnabled(true);
        });

        holder.imageView1.setEnabled(!this.loading);
        holder.imageView1.setOnClickListener(view ->    holder.cardView1.callOnClick());
//        holder.itemTextView1.setOnClickListener(view -> holder.cardView1.callOnClick());

        holder.radioButton.setEnabled(!this.disableSelect);
        holder.radioButton.setOnClickListener(view -> {
            if(this.curSelectButton != null)
                this.curSelectButton.setChecked(false);
            holder.radioButton.setChecked(true);
            this.curSelectIdx = position;
            this.curSelectButton = holder.radioButton;
            if(dataClickEventListener != null)
                dataClickEventListener.onClickEvent(position, true);
        });
        holder.itemTextView2.setEnabled(!this.loading);
        holder.itemTextView2.setOnClickListener(view -> holder.cardView1.callOnClick());
        holder.itemTextView3.setEnabled(!this.loading);
        holder.itemTextView3.setOnClickListener(view -> holder.cardView1.callOnClick());

    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    @Override
    public void onFinishEvent(boolean b) {

    }
}
