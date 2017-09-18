package com.apside.faceheroes;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;



public class MaskAdapter extends RecyclerView.Adapter<MaskAdapter.MaskHolder> {

    private List<Mask> mMaskList;

    MaskAdapter(List<Mask> maskList) {
        this.mMaskList = maskList;
    }

    public static class MaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;

        private Mask mMask;

        private static final String MASK_KEY = "MASK";

        public MaskHolder(View v){
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("RecyclerView", "CLICK !");
        }

        public void bindMask(Mask mask){
            mMask = mask;
            mImageView.setImageBitmap(mask.getBitmap());
        }
    }

    @Override
    public MaskAdapter.MaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mask_row, parent, false);
        return new MaskHolder(view);
    }

    @Override
    public void onBindViewHolder(MaskAdapter.MaskHolder holder, int position) {
        Mask mask = mMaskList.get(position);
        holder.bindMask(mask);
    }

    @Override
    public int getItemCount() {
        return mMaskList.size();
    }
}
