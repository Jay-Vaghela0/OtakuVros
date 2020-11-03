package com.jayvaghela.otakucommunitytub.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ablanco.zoomy.Zoomy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.jayvaghela.otakucommunitytub.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class FullImageActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
    ImageView imageView;
    FloatingActionButton fab1,fab2;
    String url;
    private String time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);
        imageView = findViewById(R.id.fullImage);
        Zoomy.Builder builder = new Zoomy.Builder(this).target(imageView);
        builder.register();



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        databaseReference.updateChildren(hashMap);


        Intent intent = getIntent();
        url = intent.getStringExtra("Fullimage");
        Glide.with(getApplicationContext()).load(url).into(imageView);


        fab1=findViewById(R.id.fab_close);
        fab2=findViewById(R.id.fab_download);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
               permission();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permission() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Picasso.get().load(url).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
//                            final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
//                            progressDialog.setMessage("Downloading..");
//                            progressDialog.setCancelable(false);
//                            progressDialog.create();
//                            progressDialog.show();

                            String root = Environment.getExternalStorageDirectory().toString();
                            File myDir = new File(root + "/Otakus");

                            if (!myDir.exists()) {
                                myDir.mkdirs();
                            }

//                            String name = UUID.randomUUID().toString();
//                            name = name + ".png";

                            Long tsLong = System.currentTimeMillis() / 1000;
                            String name = tsLong.toString() + ".png";

                            myDir = new File(myDir, name);

                            FileOutputStream out = new FileOutputStream(myDir);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();

                            Snackbar.make(imageView,"Download Completed!",Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.black)).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();

                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(FullImageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

            }

            else {
                permission();
            }
        }
    }

    private void anim() {
        final ConstraintLayout root_layout;
        root_layout = findViewById(R.id.constraintLayoutm);
        int cx = root_layout.getWidth()/2;
        int cy = root_layout.getHeight()/2;
        float finalRadius = Math.max(root_layout.getWidth(),root_layout.getHeight());

        Animator circularReveal = ViewAnimationUtils.createCircularReveal(root_layout, cx, cy, finalRadius,0 );
//        new CountDownTimer(1000, 1) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                setContentView(R.layout.fragment_home);
//            }
//        };
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                imageView.setVisibility(View.INVISIBLE);
                fab1.hide();
                fab2.hide();
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(FullImageActivity.this,root_layout,"ok");
                startActivity(intent,options.toBundle());
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                setContentView(R.layout.activity_home);
            }
        });
        circularReveal.setDuration(1800);
        root_layout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        status("offline");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isOnline",false);
        hashMap.put("timestamp", ServerValue.TIMESTAMP);

        getTime();
        reference.child("lastSeen").setValue(""+time);
        reference.child("timestamp").setValue(ServerValue.TIMESTAMP);
        reference.updateChildren(hashMap);
    }

    private void status(String status){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    private void getTime() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, EEE, d MMM");
        time = dateFormat.format(Calendar.getInstance().getTime());
    }
}