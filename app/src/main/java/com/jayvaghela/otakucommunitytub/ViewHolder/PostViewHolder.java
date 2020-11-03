package com.jayvaghela.otakucommunitytub.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jayvaghela.otakucommunitytub.R;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public ImageView iv_like,image,more,comment_read,user,online;
    public TextView tv_title,tv_likes,tv_username,tv_time;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_title = itemView.findViewById(R.id.title);
        online = itemView.findViewById(R.id.online);
        iv_like = itemView.findViewById(R.id.like_img);
        tv_username = itemView.findViewById(R.id.username);
        tv_time = itemView.findViewById(R.id.time);
        image = itemView.findViewById(R.id.image);
        tv_likes = itemView.findViewById(R.id.likes);
        more = itemView.findViewById(R.id.moreOp);
        comment_read = itemView.findViewById(R.id.comment_read);
        user = itemView.findViewById(R.id.userimage);
    }

    public void setDetail(Context context, String title, String userName, String userImg, String image){
        TextView tv_title = itemView.findViewById(R.id.title);
        TextView tv_userName = itemView.findViewById(R.id.username);
        ImageView iv_image = itemView.findViewById(R.id.image);
        ImageView iv_userImage = itemView.findViewById(R.id.userimage);

        tv_title.setText(title);
        tv_userName.setText(userName);
        Glide.with(context).load(image).into(iv_image);
        Glide.with(context).load(userImg).into(iv_userImage);
    }

}