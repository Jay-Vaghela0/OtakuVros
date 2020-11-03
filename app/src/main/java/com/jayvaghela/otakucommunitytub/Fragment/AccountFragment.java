package com.jayvaghela.otakucommunitytub.Fragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayvaghela.otakucommunitytub.Model.Users;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.Quick;


public class AccountFragment extends Fragment {

    View view;
    TextView profile_tv;
    EditText username, bio_et;
    ImageView edit_img;
    Button save;
    DatabaseReference reference;
    FirebaseUser fuser;
    Typeface MR, MRR;

    ImageView fb_img,wa_img,ig_img,gl_img;
    String gmail,fb,ig, wa,text,isWhat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        init();

        fb_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "Enter your Facebook profile link";
                isWhat = "f";
                EditText();
            }
        });

        wa_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "Enter your whatsapp number with country code (+12)";
                isWhat = "w";
                EditText();
            }
        });

        ig_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "Enter your Instagram profile link";
                isWhat = "i";
                EditText();
            }
        });

        gl_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "Enter your email";
                isWhat = "g";
                EditText();
            }
        });




        edit_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setVisibility(View.VISIBLE);
                username.setEnabled(true);
                bio_et.setEnabled(true);
                username.setSelection(username.getText().length());
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                username.setEnabled(false);
                bio_et.setEnabled(false);


                reference.child("bio").setValue(bio_et.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //   Toast.makeText(getContext(),"Profile Updated...", Toast.LENGTH_SHORT);
                        } else {
                            //   Toast.makeText(getContext(),"Unable to Save...", Toast.LENGTH_SHORT);

                        }
                    }
                });

                reference.child("name").setValue(username.getText().toString());
                reference.child("search").setValue(username.getText().toString().toLowerCase());

                reference.child("username").setValue(username.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username.getText().toString()).build();
                                user.updateProfile(profileUpdates);
                            }

                            Snackbar.make(view,"Profile Updated...",BaseTransientBottomBar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.black)).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).setAnchorView(R.id.bottom_navigation).show();
                        } else {
                            Snackbar.make(view,"Unable to Save...",BaseTransientBottomBar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.black)).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).setAnchorView(R.id.bottom_navigation).show();
                        }
                    }
                });

                save.setVisibility(View.GONE);
            }

        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    username.setText(user.getUsername());
                    bio_et.setText(user.getBio());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }


    public void EditText() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(text);

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (isWhat){
                    case "f":
                        FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("facebook").setValue(input.getText().toString());
                        break;
                    case "i":
                        FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("instagram").setValue(input.getText().toString());
                        break;
                    case "g":
                        FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("gmail").setValue(input.getText().toString());
                        break;
                    case "w":
                        FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("whatsapp").setValue(input.getText().toString());
                        break;
                    default:
                        Toast.makeText(getContext(), "Error Nani!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }


    private void init() {

        fb_img = view.findViewById(R.id.facebook_btn);
        wa_img = view.findViewById(R.id.whatsapp_btn);
        ig_img = view.findViewById(R.id.instagram_btn);
        gl_img = view.findViewById(R.id.gmail_btn);

        // Inflate the layout for this fragment
        MRR = Typeface.createFromAsset(getContext().getAssets(), "fonts/myriadregular.ttf");
        MR = Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad.ttf");



        ImageView imageView = view.findViewById(R.id.profile_image);

        username = view.findViewById(R.id.username);
        profile_tv = view.findViewById(R.id.profile_tv);
        bio_et = view.findViewById(R.id.bio_et);
        edit_img = view.findViewById(R.id.edit_image);
        save = view.findViewById(R.id.save_btn);


        username.setTypeface(MR);
        profile_tv.setTypeface(MR);
        bio_et.setTypeface(MRR);
        save.setTypeface(MR);


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        Glide.with(view.getContext()).load(fuser.getPhotoUrl()).into(imageView);
    }
}