package com.jayvaghela.otakucommunitytub.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Model.APIService;
import com.jayvaghela.otakucommunitytub.Model.Comments;
import com.jayvaghela.otakucommunitytub.Notifications.Client;
import com.jayvaghela.otakucommunitytub.Notifications.Data;
import com.jayvaghela.otakucommunitytub.Notifications.MyResponse;
import com.jayvaghela.otakucommunitytub.Notifications.Sender;
import com.jayvaghela.otakucommunitytub.Notifications.Token;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.GetTimeAgo;
import com.jayvaghela.otakucommunitytub.Util.Quick;
import com.jayvaghela.otakucommunitytub.ViewHolder.CommentViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {

    private Quick quick;
    private TextView title;
    private EditText et_comment;
    private ImageView imageView,imageViewUser,postBtn;
    private String url;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;

    private FirebaseRecyclerAdapter<Comments, CommentViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<Comments> options;

    private String postKey,time,a,b,tempKey,UID,getUID;

    private PopupMenu popupMenu;
    private Menu menu;
    private boolean isAdded = false;
    private boolean notifyIsSpam=false;



    APIService apiService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();



        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);




        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        hashMap.put("isOnline",true);
        databaseReference.updateChildren(hashMap);

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_comment.getText().length()<1) et_comment.setError("Enter Something");
                else post();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(CommentActivity.this,imageView,"image");
                Intent intent = new Intent(getApplicationContext(),FullImageActivity.class);
                intent.putExtra("Fullimage",url);
                startActivity(intent,options.toBundle());
            }
        });


        imageViewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ViewProfileActivity.class);
                i.putExtra("uid",getUID);
                startActivity(i);

//                Toast.makeText(CommentActivity.this, "f", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView imageView = findViewById(R.id.imgUser1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ViewProfileActivity.class);
                i.putExtra("uid",getUID);
                startActivity(i);

            }
        });

    }

    private void init() {
        quick = new Quick();
        imageView = findViewById(R.id.img);
        imageViewUser = findViewById(R.id.imgUser);
        et_comment = findViewById(R.id.comment);
        title = findViewById(R.id.title_get);
        postBtn = findViewById(R.id.c_btn);
        recyclerView = findViewById(R.id.comment_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        Intent intent = getIntent();
        String image = intent.getStringExtra("image");
        url = image;
        String imguser = intent.getStringExtra("userimage");
        postKey = intent.getStringExtra("postKey");
        getUID = intent.getStringExtra("UID");
        Glide.with(getApplicationContext()).load(image).into(imageView);
        Glide.with(getApplicationContext()).load(imguser).into(imageViewUser);



        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Comments").child(postKey);
        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = Objects.requireNonNull(user).getUid();
        reference.keepSynced(true);


        DatabaseReference referenceE = database.getReference("Post").child(postKey);
        referenceE.child("text").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    title.setText(Objects.requireNonNull(dataSnapshot.getValue()).toString());
                } catch (Exception ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        YoYo.with(Techniques.FadeIn).duration(1180).playOn(imageViewUser);
        setup();
    }

    private void post() {
        getTime();
        try {
            HashMap<String,Object> map = new HashMap<>();
            map.put("comment",et_comment.getText().toString());
            map.put("username",user.getDisplayName());
            map.put("userimage",""+user.getPhotoUrl());
            map.put("time"," ◦  "+time);
            map.put("timestamp", ServerValue.TIMESTAMP);
            map.put("UID",UID);
            reference.push().setValue(map);
            et_comment.getText().clear();
            isAdded=true;



        } catch (Exception e){
            quick.toast(getApplicationContext(),e.getMessage());
        }

    }

    private void setup() {
        options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(reference, Comments.class).build();
        tempKey = postKey;
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(options) {

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row,parent,false);
                return new CommentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder commentViewHolder, final int i, @NonNull final Comments comments) {
                commentViewHolder.setComment(getApplicationContext(),comments.getComment(),comments.getUsername(),comments.getUserimage());

                if (isAdded){
                    getKey(i);
                    commentViewHolder.time.setText("just now");
                    isAdded=false;


                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference notiDatabaseReference = FirebaseDatabase.getInstance().getReference("Notifications");

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("id",""+user.getUid());
                    map.put("image",""+url);
                    map.put("isSeen",false);
                    map.put("userimage",""+user.getPhotoUrl());
                    map.put("type","comment");
                    map.put("time",ServerValue.TIMESTAMP);
                    map.put("text",""+user.getDisplayName()+" Commented on your post!");

                    notiDatabaseReference.child(getUID).push().setValue(map);

                    FirebaseDatabase.getInstance().getReference("Users").child(getUID).child("notifications").setValue(false);


                    // TODO: 2/22/2020 Notification

                    if (!user.getUid().equals(getUID)) {
                        if (!notifyIsSpam) {
                            getKey(i);
                            DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                            Query query = tokens.orderByKey().equalTo(getUID);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                                        Token token = snapshot.getValue(Token.class);
                                        Data data = new Data(user.getUid(), R.drawable.icon, "", ""+comments.getUsername()+" commented on your post!", getUID);
                                        Sender sender = new Sender(data, token.getToken());
                                        apiService.sendNotification(sender);


                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            notifyIsSpam=true;
                        } else {
                            notifyIsSpam = true;
                        }
                    }


                }

                GetTimeAgo getTimeAgo = new GetTimeAgo();
                final String rtime = getTimeAgo.getTimeAgo(comments.getTimestamp(),getApplicationContext());
                commentViewHolder.time.setText(String.format(" ◦  %s", rtime));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                },800);

                imageViewUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getKey(i);
//                        Intent intent = new Intent(getApplicationContext(),UserProfileActivity.class);
//                        intent.putExtra("name",""+comments.getUsername());
//                        intent.putExtra("img",""+comments.getUserimage());
//                        intent.putExtra("UID", ""+comments.getUID());
//                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(CommentActivity.this,imageViewUser,"user");
//                        startActivity(intent,options.toBundle());
                    }
                });

                commentViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ViewProfileActivity.class);
                        intent.putExtra("uid", comments.getUID());
                        startActivity(intent);
                    }
                });


                commentViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public boolean onLongClick(View v) {
                        getKey(i);
                        a = comments.getUID().toUpperCase();
                        b = Objects.requireNonNull(user.getUid()).toUpperCase();
                        if (a.equals(b)){
                            showPopup(v);
                            menu = popupMenu.getMenu();
                            menu.removeItem(R.id.Report);
                        } else {
                            showPopup(v);
                            menu.removeItem(R.id.Delete);
                        }
                        return false;
                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
    private void getTime() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, EEE, d MMM");
        time = dateFormat.format(Calendar.getInstance().getTime());
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void showPopup(View view){
        popupMenu = new PopupMenu(this, view, Gravity.CENTER,0,R.style.PopupMenuMoreCentralized);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.Delete:
                        Delete();
                        break;
                    case R.id.Report:
                        Report();
                        break;
                    default:
                        quick.toast(getApplicationContext(),"Something gone wrong");
                }
                return false;
            }
        });
        popupMenu.inflate(R.menu.post_menu);
        popupMenu.show();
        menu = popupMenu.getMenu();
    }
    private void getKey(int i){
        postKey = firebaseRecyclerAdapter.getRef(i).getKey();
    }
    private void Report() {
        FirebaseDatabase.getInstance().getReference("Reports Comments").child(postKey).child("Reporter").setValue(UID);
        final ProgressDialog progressDialog = new ProgressDialog(CommentActivity.this);
        progressDialog.setMessage("Reporting...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CommentActivity.this);
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builder.setTitle("Post Reported!");
                builder.show();
            }
        },3000);
    }
    private void Delete() {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to Delete this post?");
        builder.setIcon(R.drawable.ic_warning);
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(Objects.requireNonNull(tempKey)).child(postKey);
                reference.removeValue();
                firebaseRecyclerAdapter.stopListening();
                firebaseRecyclerAdapter.startListening();
            }
        });
        builder.show();
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