package com.jayvaghela.otakucommunitytub.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jayvaghela.otakucommunitytub.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageV,uimageV;
    public TextView text,time;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.noti_text);
        uimageV = itemView.findViewById(R.id.noti_userimage);
        imageV = itemView.findViewById(R.id.noti_image);
        time = itemView.findViewById(R.id.noti_time);
    }

    public void setDetail(Context context,String texts,String image,String userimage,String type){
        ImageView imageV,uimageV;
        TextView text = itemView.findViewById(R.id.noti_text);
        uimageV = itemView.findViewById(R.id.noti_userimage);
        imageV = itemView.findViewById(R.id.noti_image);


        text.setText(texts);

        Glide.with(context).load(userimage).into(uimageV);
        Glide.with(context).load(image).into(imageV);
    }
}