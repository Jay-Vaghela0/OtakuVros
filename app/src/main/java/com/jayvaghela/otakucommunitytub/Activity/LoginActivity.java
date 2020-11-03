package com.jayvaghela.otakucommunitytub.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jayvaghela.otakucommunitytub.R;
import com.jayvaghela.otakucommunitytub.Util.Quick;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private View view;
    private Quick quick;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    private SignInButton signInButton;

    protected SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String shP1 = "UserLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Ha vai kiski? badhiya sab?");
        builder.setIcon(R.drawable.emo);
        builder.setMessage("");
        builder.setCancelable(false);
        builder.setPositiveButton("Ha vro", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
        });
        builder.setNegativeButton("Nu.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.dismiss();}
        });
        builder.show();




        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void init() {
//        Objects.requireNonNull(getSupportActionBar()).hide();
        signInButton = findViewById(R.id.signIn_btn);
        TextView textView = findViewById(R.id.footer_tv);
        TextView textView2 = findViewById(R.id.textView2);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView2.setMovementMethod(LinkMovementMethod.getInstance());

        MaterialCardView cardView = findViewById(R.id.card);

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.card_up);
        cardView.setAnimation(anim);


        ImageView imageView = findViewById(R.id.image);
        Glide.with(getApplicationContext()).load(R.drawable.hi_anime).into(imageView);

        mAuth = FirebaseAuth.getInstance();

        quick = new Quick();

        view = getWindow().getDecorView().getRootView();

        sharedPreferences= getSharedPreferences(shP1, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
        sharedPreferences.getBoolean(shP1,false);

        if (sharedPreferences.contains(shP1)){
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

/*
    private void firebaseAuthWithGoogle(final AuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            editor.putBoolean(shP1,true);
                            editor.commit();
                            editor.apply();
+
                            quick.snackbar(view,"Login successful");

                            // TODO: 2/24/2020
                            HashMap<String,Object> linkmap =  new HashMap<>();
                            linkmap.put("facebook","o");
                            linkmap.put("instagram","o");
                            linkmap.put("gmail","o");
                            linkmap.put("whatsapp","o");
                            FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(linkmap);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            HashMap<String,Object> map = new HashMap<>();
                            map.put("status","online");
                            map.put("lastSeen","Online");
                            map.put("isOnline",true);
                            map.put("notifications",false);
                            map.put("username",""+user.getDisplayName());
                            map.put("name",""+user.getDisplayName());
                            map.put("search",""+user.getDisplayName().toLowerCase().trim());
                            map.put("bio","I'm Otaku!");
                            map.put("imageURL",""+user.getPhotoUrl());
                            map.put("userimage",""+user.getPhotoUrl());
                            map.put("id",""+user.getUid());
                            map.put("email",""+user.getEmail());

                            databaseReference.child(""+user.getUid()).setValue(map);

                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        }else{
                            quick.snackbar(view,"Authentication failed");
                        }
                    }
                });
    }*/

//    private void handleSignInResult(GoogleSignInResult result){
//        try {
//            if(result.isSuccess()){
//                GoogleSignInAccount account = result.getSignInAccount();
//                String idToken = Objects.requireNonNull(account).getIdToken();
//                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//                firebaseAuthWithGoogle(credential);
//            }else{
//                quick.snackbar(view,"Login Unsuccessful");
//            }
//        } catch (Exception e) {quick.toast(getApplicationContext(),e.getMessage());}
//    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            editor.putBoolean(shP1,true);
                            editor.commit();
                            editor.apply();

                            quick.snackbar(view,"Login successful");

                            // TODO: 2/24/2020
                            HashMap<String,Object> linkmap =  new HashMap<>();
                            linkmap.put("facebook","o");
                            linkmap.put("instagram","o");
                            linkmap.put("gmail","o");
                            linkmap.put("whatsapp","o");
                            FirebaseDatabase.getInstance().getReference("Links").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(linkmap);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            HashMap<String,Object> map = new HashMap<>();
                            map.put("status","online");
                            map.put("lastSeen","Online");
                            map.put("isOnline",true);
                            map.put("notifications",false);
                            map.put("username",""+user.getDisplayName());
                            map.put("name",""+user.getDisplayName());
                            map.put("search",""+user.getDisplayName().toLowerCase().trim());
                            map.put("bio","I'm Otaku!");
                            map.put("imageURL",""+user.getPhotoUrl());
                            map.put("userimage",""+user.getPhotoUrl());
                            map.put("id",""+user.getUid());
                            map.put("email",""+user.getEmail());



                            FirebaseDatabase.getInstance().getReference("Ban").child(user.getUid()).setValue(false);


                            databaseReference.child(""+user.getUid()).setValue(map);

                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        } else {
                            quick.snackbar(view,"Authentication failed");
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
            }
        }
    }
}