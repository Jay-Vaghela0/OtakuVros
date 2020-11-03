package com.jayvaghela.otakucommunitytub.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Adapter.OnItemClick;
import com.jayvaghela.otakucommunitytub.Fragment.AccountFragment;
import com.jayvaghela.otakucommunitytub.Fragment.HomeFragment;
import com.jayvaghela.otakucommunitytub.Fragment.NotificationsFragment;
import com.jayvaghela.otakucommunitytub.Fragment.SearchFragment;
import com.jayvaghela.otakucommunitytub.Model.Chat;
import com.jayvaghela.otakucommunitytub.Model.Notification;
import com.jayvaghela.otakucommunitytub.Model.Users;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.Quick;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigation;
    private LottieAnimationView lottieAnimationView;
    private ConstraintLayout constraintLayout;
    private String time;
    HashMap<String,Object> map = new HashMap<>();
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        messages();
        notification();


    }

    private void init() {
//        this.onItemClick = this;
        navigation = findViewById(R.id.bottom_navigation);
        lottieAnimationView = findViewById(R.id.loading);
        constraintLayout = findViewById(R.id.loading_ly);
        navigation.setOnNavigationItemSelectedListener(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        isban();

        loadFragment(new HomeFragment());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;

            case R.id.navigation_search:
                fragment = new SearchFragment();
                break;

            case R.id.navigation_notifications:
                navigation.removeBadge(R.id.navigation_notifications);
                fragment = new NotificationsFragment();
                break;

            case R.id.navigation_account:
                fragment = new AccountFragment();
                break;
        }
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        animate();
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return true;
    }

    private void playAnim() {
        constraintLayout.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();
    }

    private void stopAnim() {
        constraintLayout.setVisibility(View.INVISIBLE);
        lottieAnimationView.pauseAnimation();
    }

    private void animate() {
        playAnim();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnim();
            }
        }, 760);
    }

    private void exit() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
    }
    public void onBackPressed() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Do you want to Exit?");
        builder.setIcon(R.drawable.ic_warning);
        builder.setMessage("");
        builder.setCancelable(false);
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {exit();}
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.dismiss();}
        });
        builder.show();
    }

    private void getTime() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, EEE, d MMM");
        time = dateFormat.format(Calendar.getInstance().getTime());
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        online();
        status("online");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isOnline",true);

        reference.child("lastSeen").setValue("Online");
        reference.child("timestamp").setValue(ServerValue.TIMESTAMP);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        offline();
        status("offline");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isOnline",false);
        map.put("timestamp", ServerValue.TIMESTAMP);

        getTime();
        reference.child("lastSeen").setValue(""+time);
        reference.child("timestamp").setValue(ServerValue.TIMESTAMP);
        reference.updateChildren(hashMap);
    }

/*
    private void online() {
        getTime();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        map.put("status","online");
        map.put("lastSeen","Online");
        map.put("isOnline",true);
        map.put("username",""+user.getDisplayName());
        map.put("name",""+user.getDisplayName());
        map.put("search",""+user.getDisplayName());
        map.put("bio"," ");
        map.put("imageURL",""+user.getPhotoUrl());
        map.put("userimage",""+user.getPhotoUrl());
        map.put("id",""+user.getUid());
        map.put("email",""+user.getEmail());

        databaseReference.child(""+user.getUid()).setValue(map);
    }
    private void offline() {
        getTime();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        final HashMap<String,Object> map = new HashMap<>();
        map.put("status","offline");
        map.put("lastSeen",time);
        map.put("isOnline",false);
        map.put("username",""+user.getDisplayName());
        map.put("name",""+user.getDisplayName());
        map.put("search",""+user.getDisplayName());
        map.put("userimage",""+user.getPhotoUrl());
        map.put("bio"," ");
        map.put("imageURL",""+user.getPhotoUrl());
        map.put("id",""+user.getUid());
        map.put("email",""+user.getEmail());

        databaseReference.child(""+user.getUid()).setValue(map);
    }
*/

    private void isban(){


        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Ban");
        db.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean s = (boolean) snapshot.getValue();

                if (s) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(HomeActivity.this);
                    builder.setTitle("Ooh oo!");
                    builder.setIcon(R.drawable.emo);
                    builder.setMessage("Your account has been banned. Contact developer for getting unbann UwU");
                    builder.setCancelable(false);
                    builder.create();
                    builder.show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }



    private void messages(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread++;
                    }
                }

                if (unread == 0) {
                    navigation.removeBadge(R.id.navigation_home);
                } else {
                    BadgeDrawable badge = navigation.getOrCreateBadge(R.id.navigation_home);
                    badge.setVisible(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void notification(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());

        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("notifications");

        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean b = (boolean)dataSnapshot.getValue();
                if (!b)
                {
                    reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int unread = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Notification notification = snapshot.getValue(Notification.class);

                                assert notification != null;
                                if (!notification.isSeen()) {
                                    unread++;
                                }
                            }
                            if (unread == 0) {
                                navigation.removeBadge(R.id.navigation_notifications);
                            } else {
                                BadgeDrawable badge = navigation.getOrCreateBadge(R.id.navigation_notifications);
                                badge.setVisible(true);
                                badge.setNumber(unread);
                                dbReference.setValue(false);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}