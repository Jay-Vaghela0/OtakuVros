package com.jayvaghela.otakucommunitytub.Fragment;

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
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Activity.HomeActivity;
import com.jayvaghela.otakucommunitytub.Activity.ViewProfileActivity;
import com.jayvaghela.otakucommunitytub.Model.Notification;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.GetTimeAgo;
import com.jayvaghela.otakucommunitytub.Util.Quick;
import com.jayvaghela.otakucommunitytub.ViewHolder.NotificationViewHolder;

import java.util.HashMap;
import java.util.Objects;

public class NotificationsFragment extends Fragment {

    private View view;

    private Quick quick;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;

    private FirebaseRecyclerAdapter<Notification, NotificationViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<Notification> options;

    private String postKey,tempKey,UID;

    private PopupMenu popupMenu;
    private Menu menu;
    SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionButton floatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notifications, container, false);


        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("notifications").setValue(true);

        floatingActionButton = view.findViewById(R.id.fab_clear);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid()).removeValue();
                final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.recycler_anim);
                recyclerView.setLayoutAnimation(controller);
                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();

                firebaseRecyclerAdapter.notifyDataSetChanged();
            }
        });


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_n);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.recycler_anim);
                        recyclerView.setLayoutAnimation(controller);
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                        recyclerView.scheduleLayoutAnimation();

                        firebaseRecyclerAdapter.notifyDataSetChanged();

                        swipeRefreshLayout.setRefreshing(false);
                    }
                },800);
            }
        });

        init();
        setup();
        return view;
    }

    private void init() {
        quick = new Quick();
        recyclerView = view.findViewById(R.id.noti_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Notifications");
        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = Objects.requireNonNull(user).getUid();
        reference.keepSynced(true);


        LottieAnimationView lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setVisibility(View.INVISIBLE);
        lottieAnimationView.playAnimation();
    }

    private void setup() {
        options = new FirebaseRecyclerOptions.Builder<Notification>().setQuery(reference.child(user.getUid()), Notification.class).build();
        tempKey = postKey;
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(options) {

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row,parent,false);
                return new NotificationViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NotificationViewHolder notificationViewHolder, final int i, @NonNull final Notification notification) {
                notificationViewHolder.setDetail(getContext(),notification.getText(),notification.getImage(),notification.getUserimage(),notification.getType());

                GetTimeAgo getTimeAgo = new GetTimeAgo();
                String rtime = getTimeAgo.getTimeAgo(notification.getTime(),getContext());
                notificationViewHolder.time.setText(rtime);

                notificationViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public boolean onLongClick(View v) {
                        getKey(i);
                        showPopup(v);
                        return false;
                    }
                });

                notificationViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), HomeActivity.class));
                    }
                });

                notificationViewHolder.uimageV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewProfileActivity.class);
                        intent.putExtra("uid", notification.getId());
                        startActivity(intent);
                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void showPopup(View view){
        popupMenu = new PopupMenu(getContext(), view, Gravity.CENTER,0,R.style.PopupMenuMoreCentralized);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.Delete) {
                    Delete();
                } else {
                    quick.toast(getContext(), "Something gone wrong");
                }
                return false;
            }
        });
        popupMenu.inflate(R.menu.post_menu);
        popupMenu.getMenu().findItem(R.id.Report).setVisible(false);
        popupMenu.show();
        menu = popupMenu.getMenu();
    }
    private void getKey(int i){
        postKey = firebaseRecyclerAdapter.getRef(i).getKey();
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
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(Objects.requireNonNull(user.getUid())).child(postKey);
                reference.removeValue();
                firebaseRecyclerAdapter.stopListening();
                firebaseRecyclerAdapter.startListening();
            }
        });
        builder.show();
    }
}