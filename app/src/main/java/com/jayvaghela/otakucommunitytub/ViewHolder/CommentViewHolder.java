package com.jayvaghela.otakucommunitytub.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.jayvaghela.otakucommunitytub.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public MaterialCardView cardView;
    public TextView comment,name,time;
    public ImageView imageView;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.c_card);
        comment = itemView.findViewById(R.id.c_comment);
        name = itemView.findViewById(R.id.c_userName);
        time = itemView.findViewById(R.id.c_time);
        imageView = itemView.findViewById(R.id.c_userImg);
    }

    public void setComment(Context context,String comment, String userName, String userImg){
        TextView tv_comment = itemView.findViewById(R.id.c_comment);
        TextView tv_userName = itemView.findViewById(R.id.c_userName);
        ImageView iv_userImage = itemView.findViewById(R.id.c_userImg);

        tv_comment.setText(comment);
        tv_userName.setText(userName);
        Glide.with(context).load(userImg).into(iv_userImage);
    }

}
