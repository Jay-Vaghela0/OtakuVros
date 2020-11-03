package com.jayvaghela.otakucommunitytub.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jayvaghela.otakucommunitytub.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
    EditText title;
    ImageView imageView;
    LottieAnimationView btn;

    FirebaseDatabase database;
    DatabaseReference post;
    FirebaseAuth auth;
    FirebaseUser user;

    private String userName;
    private String userImg;
    private String url = null;
    private String time;
    Uri pickedImgUri = null;

    HashMap<String,Object> map;
    ProgressDialog builder;
    String postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        hashMap.put("isOnline",true);
        databaseReference.updateChildren(hashMap);


        userDataInit();
        getTime();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                permission();
                submitIfValid();
            }
        });
        btn.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            }
        });
    }

    private void submitIfValid() {
        String a;
        a = title.getText().toString();
        if (a.isEmpty() || pickedImgUri == null){
           if (a.isEmpty()){
               title.setError("Enter Something!");
           } else {
               View view = getWindow().getDecorView().getRootView();
               Snackbar.make(view, "Choose Image",Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
           }
        } else {
            submit();
        }
    }

    private void init() {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
//        Objects.requireNonNull(getSupportActionBar()).hide();
        title = findViewById(R.id.editText);
        imageView = findViewById(R.id.img_sub);
        btn = findViewById(R.id.submit_btn);
    }
    private void userDataInit() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        post = FirebaseDatabase.getInstance().getReference().child("Post");

        userName = user.getDisplayName();
        userImg = String.valueOf(user.getPhotoUrl());
    }

    private void submit() {
        dialog();

        FirebaseDatabase.getInstance().getReference("Users Profile").child(user.getUid()).child("Likes").setValue(0);
        uploadImage(pickedImgUri);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        getTime();
        map = new HashMap<>();
        map.put("text",""+title.getText().toString());
        map.put("username",""+userName);
        map.put("userimage",""+userImg);
        map.put("time",""+time);
        map.put("timestamp",ServerValue.TIMESTAMP);
        map.put("UID",""+uid);
    }

    private void getTime() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, EEE, d MMM");
        time = dateFormat.format(Calendar.getInstance().getTime());
    }

    private void openGallery() {
        CropImage.activity().start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            pickedImgUri = result.getUri();
            imageView.setImageURI(pickedImgUri);
        }
    }

    private void uploadImage(Uri pickedImgUri) {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("Posts");
        final StorageReference imageFilePath = mStorage.child(Objects.requireNonNull(pickedImgUri.getLastPathSegment()));
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        url = uri.toString();

                        map.put("image",url);
                        postKey = post.push().getKey();
                        map.put("postkey",""+postKey);
                        post.child(postKey).setValue(map);

                        if (builder.isShowing()) {
                            builder.dismiss();
                            btn.playAnimation();
                        }
                    }
                });
            }
        });
    }

    private void dialog(){
        builder = new ProgressDialog(this);
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permission() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);


        } else {
            submitIfValid();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
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

}