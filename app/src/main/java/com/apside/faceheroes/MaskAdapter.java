package com.apside.faceheroes;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;



public class MaskAdapter extends RecyclerView.Adapter<MaskAdapter.ViewHolder> {

    private List<Mask> mMaskList;

    MaskAdapter(List<Mask> maskList) {
        this.mMaskList = maskList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text;
        ViewHolder(View v){
            super(v);
            //image = (ImageView) v.findViewById(R.id.image1);
            text = (TextView) v.findViewById(android.R.id.text1);
            Log.i("FaceHero", "Yeah");
        }
    }

    @Override
    public MaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(android.R.layout.simple_selectable_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MaskAdapter.ViewHolder holder, int position) {
        //holder.image.setImageBitmap(mMaskList.get(position).getBitmap());
        holder.text.setText(mMaskList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mMaskList.size();
    }
}
