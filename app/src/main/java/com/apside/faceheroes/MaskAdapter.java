package com.apside.faceheroes;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    private final List<HeroFacetracker> mFaceTrackerList;

    MaskAdapter(List<Mask> maskList, List<HeroFacetracker> faceTrackerList) {
        this.mMaskList = maskList;
        this.mFaceTrackerList = faceTrackerList;
    }

    public static class MaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;

        private Mask mMask;
        private List<HeroFacetracker> mFaceTrackerList;

        private static final String MASK_KEY = "MASK";

        public MaskHolder(View v, List<HeroFacetracker> faceTrackerList){
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image1);
            mFaceTrackerList = faceTrackerList;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("RecyclerView", "CLICK !");
            for (HeroFacetracker tracker : mFaceTrackerList) {
                tracker.setMask(mMask.getDrawable());
            }
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
        return new MaskHolder(view, mFaceTrackerList);
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
