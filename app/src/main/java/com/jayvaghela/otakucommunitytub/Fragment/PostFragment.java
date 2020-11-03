package com.jayvaghela.otakucommunitytub.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ablanco.zoomy.DoubleTapListener;
import com.ablanco.zoomy.LongPressListener;
import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Activity.CommentActivity;
import com.jayvaghela.otakucommunitytub.Activity.FullImageActivity;
import com.jayvaghela.otakucommunitytub.Activity.PostActivity;
import com.jayvaghela.otakucommunitytub.Activity.ViewProfileActivity;
import com.jayvaghela.otakucommunitytub.Model.APIService;
import com.jayvaghela.otakucommunitytub.Model.Post;
import com.jayvaghela.otakucommunitytub.Notifications.Client;
import com.jayvaghela.otakucommunitytub.Notifications.Data;
import com.jayvaghela.otakucommunitytub.Notifications.MyResponse;
import com.jayvaghela.otakucommunitytub.Notifications.Sender;
import com.jayvaghela.otakucommunitytub.Notifications.Token;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.GetTimeAgo;
import com.jayvaghela.otakucommunitytub.Util.Quick;
import com.jayvaghela.otakucommunitytub.ViewHolder.PostViewHolder;

import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostFragment extends Fragment {

    private View view;
    private RecyclerView postRecyclerView;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseRecyclerOptions<Post> options;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;

    private String UID,KEY,url;

    private FloatingActionButton fab;
    private Quick quick;

    private boolean isLiked = false;
    private boolean temp = false;;
    private boolean b = false;
    private String x,y;
    private PopupMenu popupMenu;
    private Menu menu;

    String tempPKey;

    APIService apiService;
    private boolean notifyIsSpam=false;

    LottieAnimationView like_popup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_post, container, false);


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        init();
        postSetUp();
        return view;
    }

    private void init() {
        postRecyclerView = view.findViewById(R.id.post_recycler_view);
//        storyRecyclerView = view.findViewById(R.id.story_recycler_view);


        postRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == 2){fab.hide();}
                else if (newState == 1){fab.hide();}
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.show();
                        }
                    },3400);}
            }
        });


        like_popup = view.findViewById(R.id.like_popup);


        quick = new Quick();

        LinearLayoutManager postLm;
        postLm = new LinearLayoutManager(getContext());
        postLm.setStackFromEnd(true);
        postLm.setReverseLayout(true);
//        storyLm = new LinearLayoutManager(getContext());
//        storyLm.setOrientation(RecyclerView.HORIZONTAL);
//        storyRecyclerView.setLayoutManager(storyLm);
        postRecyclerView.setLayoutManager(postLm);
        fab=view.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PostActivity.class));
            }
        });

//        List<String> image = new ArrayList<>();
//        image.add("okk");
//        image.add("okk");
//        image.add("okk");
//        image.add("okk");
//
//        List<Story> list = new ArrayList<>();
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//        list.add(new Story("John Doe",image));
//
//        StoryRecyclerAdapter storyRecyclerAdapter = new StoryRecyclerAdapter(list);
//        storyRecyclerView.setAdapter(storyRecyclerAdapter);
    }


    /********* post setup*******/


    private void postSetUp() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        UID = user.getUid();

        options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(reference,Post.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder postViewHolder, final int i, @NonNull final Post post) {
                postViewHolder.setDetail(getContext(), post.getText(), post.getUsername(), post.getUserimage(), post.getImage());

                GetTimeAgo getTimeAgo = new GetTimeAgo();
                final String rtime = getTimeAgo.getTimeAgo(post.getTimestamp(),getContext());
                postViewHolder.tv_time.setText(rtime);

                DatabaseReference references = database.getReference("Users");
                references.child(post.getUID()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean b = (boolean) dataSnapshot.child("isOnline").getValue();
                        if (b){
                            postViewHolder.online.setVisibility(View.VISIBLE);
                        } else postViewHolder.online.setVisibility(View.INVISIBLE);
                        if (!b) postViewHolder.online.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                DatabaseReference likeReference = FirebaseDatabase.getInstance().getReference("Likes");
                likeReference.child(post.getPostkey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild(user.getUid()))
                            {
                                postViewHolder.iv_like.setImageResource(R.drawable.ic_favorite);
                            }
                            long i1 = dataSnapshot.getChildrenCount();
                            postViewHolder.tv_likes.setText(""+i1);
                        }
                        if (temp)
                        {
                            long i1 = dataSnapshot.getChildrenCount();
                            postViewHolder.tv_likes.setText(""+i1);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });


                // TODO: 2/23/2020 Tap/Double Tap/Zooming

                Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(postViewHolder.image);

                builder.doubleTapListener(new DoubleTapListener() {
                    @Override
                    public void onDoubleTap(View v) {

                        like_popup.setVisibility(View.VISIBLE);
                        like_popup.playAnimation();
                        like_popup.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                like_popup.setVisibility(View.GONE);
                            }
                        });

                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference notiDatabaseReference = FirebaseDatabase.getInstance().getReference("Notifications");
                        final HashMap<String,Object> map = new HashMap<>();


                        try {

                        if (!isLiked) {
                            postViewHolder.iv_like.setImageResource(R.drawable.ic_favorite);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes");
                            databaseReference.child(post.getPostkey()).child(user.getUid()).setValue(true);
                            isLiked = true;

                            // TODO: 2/21/2020 Notifications Badge

                            if (!user.getUid().equals(post.getUID())) {
                                setKey(i);
                                map.put("id", "" + user.getUid());
                                map.put("image", "" + post.getImage());
                                map.put("userimage", "" + user.getPhotoUrl());
                                map.put("type", "like");
                                map.put("isSeen", false);
                                map.put("time", ServerValue.TIMESTAMP);
                                map.put("text", "" + user.getDisplayName() + " liked your post!");
                                tempPKey = notiDatabaseReference.child(post.getUID()).push().getKey();

                                notiDatabaseReference.child(post.getUID()).child(tempPKey).setValue(map);


                                FirebaseDatabase.getInstance().getReference("Users").child(post.getUID()).child("notifications").setValue(true);

                            }

                            // TODO: 2/22/2020 Notification Mobile


                            if (!user.getUid().equals(post.getUID())) {
                                if (!notifyIsSpam) {
                                    setKey(i);
                                    DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                                    Query query = tokens.orderByKey().equalTo(post.getUID());
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Token token = snapshot.getValue(Token.class);
                                                String s = post.getUsername();
                                                Data data = new Data(UID, R.drawable.icon, "", "" + s + " liked your post!", post.getUID());

                                                Sender sender = new Sender(data, token.getToken());

                                                apiService.sendNotification(sender);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });

                                    notifyIsSpam = true;
                                } else {
                                    notifyIsSpam = true;
                                }
                            }
                        } else
                        {
                            postViewHolder.iv_like.setImageResource(R.drawable.ic_favorite_border);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostkey()).child(user.getUid());
                            databaseReference.removeValue();
                            isLiked=false;
                            temp=true;

                            try {
                                FirebaseDatabase.getInstance().getReference("Notifications").child(post.getUID()).child(tempPKey).removeValue().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e){}
                        }
                    } catch (Exception e){}
                    }
                });

                builder.longPressListener(new LongPressListener() {
                    @Override
                    public void onLongPress(View v) {
                        url = post.getImage();
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),postViewHolder.image,"image");
                        Intent intent = new Intent(getContext(), FullImageActivity.class);
                        intent.putExtra("Fullimage",url);
                        startActivity(intent,options.toBundle());
                    }
                });

                builder.tapListener(new TapListener() {
                    @Override
                    public void onTap(View v) {
                        setKey(i);
                        reference.child(Objects.requireNonNull(KEY)).child("image").addValueEventListener(new ValueEventListener() {
                            @SuppressLint("CheckResult")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String imgUrl = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                                Glide.with(getContext()).load(imgUrl);

                                Intent intent = new Intent(getContext(), CommentActivity.class);
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),postViewHolder.image,"image");

                                intent.putExtra("image",post.getImage());
                                intent.putExtra("postKey", KEY);
                                intent.putExtra("userimage", ""+post.getUserimage());
                                intent.putExtra("UID", ""+post.getUID());

                                startActivity(intent,options.toBundle());
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });
                builder.register();




                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference notiDatabaseReference = FirebaseDatabase.getInstance().getReference("Notifications");
                final HashMap<String,Object> map = new HashMap<>();

                postViewHolder.iv_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference notiDatabaseReference = FirebaseDatabase.getInstance().getReference("Notifications");
                        final HashMap<String,Object> map = new HashMap<>();


                        if (!isLiked) {
                            postViewHolder.iv_like.setImageResource(R.drawable.ic_favorite);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes");
                            databaseReference.child(post.getPostkey()).child(user.getUid()).setValue(true);
                            isLiked = true;

                            // TODO: 2/21/2020 Notifications Badge

                            if (!user.getUid().equals(post.getUID())) {
                                setKey(i);
                                map.put("id", "" + user.getUid());
                                map.put("image", "" + post.getImage());
                                map.put("userimage", "" + user.getPhotoUrl());
                                map.put("type", "like");
                                map.put("isSeen", false);
                                map.put("time", ServerValue.TIMESTAMP);
                                map.put("text", "" + user.getDisplayName() + " liked your post!");
                                tempPKey = notiDatabaseReference.child(post.getUID()).push().getKey();

                                notiDatabaseReference.child(post.getUID()).child(tempPKey).setValue(map);


                                FirebaseDatabase.getInstance().getReference("Users").child(post.getUID()).child("notifications").setValue(true);

                            }

                            // TODO: 2/22/2020 Notification Mobile


                            if (!user.getUid().equals(post.getUID())) {
                                if (!notifyIsSpam) {
                                    setKey(i);
                                    DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                                    Query query = tokens.orderByKey().equalTo(post.getUID());
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Token token = snapshot.getValue(Token.class);
                                                String s = post.getUsername();
                                                Data data = new Data(UID, R.drawable.icon, "", "" + s + " liked your post!", post.getUID());

                                                Sender sender = new Sender(data, token.getToken());

                                                apiService.sendNotification(sender);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });

                                    notifyIsSpam = true;
                                } else {
                                    notifyIsSpam = true;
                                }
                            }
                        } else
                        {
                            postViewHolder.iv_like.setImageResource(R.drawable.ic_favorite_border);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostkey()).child(user.getUid());
                            databaseReference.removeValue();
                            isLiked=false;
                            temp=true;

                            FirebaseDatabase.getInstance().getReference("Notifications").child(post.getUID()).child(tempPKey).removeValue();
                        }
                    }
                });

                postViewHolder.user.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewProfileActivity.class);
                        intent.putExtra("uid", post.getUID());
                        startActivity(intent);
                    }
                });

                postViewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onClick(View v) {
                        setKey(i);
                        x = post.getUID().toUpperCase();
                        y = Objects.requireNonNull(user.getUid()).toUpperCase();
                        if (x.equals(y)){
                            showPopup(v);
                            menu = popupMenu.getMenu();
                            menu.removeItem(R.id.Report);
                        } else {
                            setKey(i);
                            showPopup(v);
                            menu.removeItem(R.id.Delete);
                        }

                    }
                });

                postViewHolder.comment_read.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        setKey(i);
                        reference.child(Objects.requireNonNull(KEY)).child("image").addValueEventListener(new ValueEventListener() {
                            @SuppressLint("CheckResult")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String imgUrl = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                                Glide.with(getContext()).load(imgUrl);

                                Intent intent = new Intent(getContext(), CommentActivity.class);
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),postViewHolder.image,"image");

                                intent.putExtra("image",post.getImage());
                                intent.putExtra("postKey", KEY);
                                intent.putExtra("userimage", ""+post.getUserimage());
                                intent.putExtra("UID", ""+post.getUID());

                                startActivity(intent,options.toBundle());
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });

                postViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        url = post.getImage();
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),postViewHolder.image,"image");
                        Intent intent = new Intent(getContext(), FullImageActivity.class);
                        intent.putExtra("Fullimage",url);
                        startActivity(intent,options.toBundle());
                        return true;
                    }
                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row,parent,false);
                return new PostViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        postRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }
    private void setKey (int i){
        KEY = firebaseRecyclerAdapter.getRef(i).getKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void showPopup(View view){
        popupMenu = new PopupMenu(getContext(), view, Gravity.CENTER,0,R.style.PopupMenuMoreCentralized);
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
                        quick.toast(getActivity(),"Something gone wrong");
                }
                return false;
            }
        });
        popupMenu.inflate(R.menu.post_menu);
        popupMenu.show();
        menu = popupMenu.getMenu();
    }

    private void Report() {
        FirebaseDatabase.getInstance().getReference("Reports").child(KEY).child("Reporter").setValue(UID);
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Reporting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
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
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
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
                try {
                    try {
                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Deleting...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();

                            }
                        },900);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Post").child(KEY);
                        reference.removeValue();
                        firebaseRecyclerAdapter.stopListening();
                        firebaseRecyclerAdapter.startListening();
                    } catch (Exception ignored){}
                } catch (Exception ignored){}
            }
        });
        builder.show();
    }
}