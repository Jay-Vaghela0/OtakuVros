package com.jayvaghela.otakucommunitytub.Activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Model.Users;
import com.jayvaghela.otakucommunitytub.R;

import java.util.HashMap;
import java.util.Objects;

public class ViewProfileActivity extends AppCompatActivity {

    String uid;
    DatabaseReference reference;
    TextView username, bio_et;
    ImageView profile_img;

    ImageView fb_img,wa_img,ig_img,gl_img;
    String gmail;
    String fb;
    String ig;
    String wa;
    int number;

    MaterialCardView cardView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();

        cardView = findViewById(R.id.link_card);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cardView.setVisibility(View.VISIBLE);
            }
        },1800);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Links");
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gmail = dataSnapshot.child("gmail").getValue().toString();
                fb = dataSnapshot.child("facebook").getValue().toString();
                ig = dataSnapshot.child("instagram").getValue().toString();
                wa = dataSnapshot.child("whatsapp").getValue().toString();


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ig.length()>3) ig_img.setVisibility(View.VISIBLE);
                if (fb.length()>3) fb_img.setVisibility(View.VISIBLE);
                if (gmail.length()>3) gl_img.setVisibility(View.VISIBLE);
                if (wa.length()>3) wa_img.setVisibility(View.VISIBLE);
            }
        },2000);


        fb_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(fb); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ViewProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        wa_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT,"");
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.whatsapp");
//                startActivity(sendIntent);

                Uri uri = Uri.parse("https://api.whatsapp.com/send?phone="+wa); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        ig_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(ig); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {}
            }
        });

        gl_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:"+gmail));
                    i.putExtra(Intent.EXTRA_SUBJECT, "");
                    startActivity(Intent.createChooser(i, "Send"));
                } catch (Exception ignore){}
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                username.setText(user.getUsername());
                bio_et.setText(user.getBio());

                Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_img);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        hashMap.put("isOnline",true);
        databaseReference.updateChildren(hashMap);


        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        profile_img = findViewById(R.id.view_profile_image);
        username = findViewById(R.id.view_username);
        bio_et = findViewById(R.id.view_bio_et);

        fb_img = findViewById(R.id.facebook_btn);
        wa_img = findViewById(R.id.whatsapp_btn);
        ig_img = findViewById(R.id.instagram_btn);
        gl_img = findViewById(R.id.gmail_btn);
    }
}