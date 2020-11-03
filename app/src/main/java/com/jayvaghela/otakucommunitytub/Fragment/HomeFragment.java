package com.jayvaghela.otakucommunitytub.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Adapter.ViewPagerAdapter;
import com.jayvaghela.otakucommunitytub.Model.Chat;
import com.jayvaghela.otakucommunitytub.R;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private FloatingActionButton fab;

    private int[] tabIcons = {
            R.drawable.ic_team,
            R.drawable.ic_speech_bubble,

    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        messages();
        return view;
    }

    private void init() {
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        fab = view.findViewById(R.id.fab_add);
        setupTab();
    }

    private void setupTab() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(tabIcons[0]);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(new PostFragment(),"");
        adapter.addFrag(new ChatFragment(),"");
        viewPager.setAdapter(adapter);
    }

    FirebaseUser firebaseUser;
    DatabaseReference reference;

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

                if (unread == 0){
                    BadgeDrawable badge = tabLayout.getTabAt(1).getOrCreateBadge();
                    badge.setVisible(false);
                } else {
                    BadgeDrawable badge = tabLayout.getTabAt(1).getOrCreateBadge();
                    badge.setVisible(true);
                    badge.setNumber(unread);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}