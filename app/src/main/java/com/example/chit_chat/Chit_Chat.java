package com.example.chit_chat;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class Chit_Chat extends Application {

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mUserAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

//        Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mUserAuth = FirebaseAuth.getInstance();

        if(mUserAuth.getCurrentUser() != null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(mUserAuth.getCurrentUser()).getUid());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mUserDatabase.child("Online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //TODO
                }
            });
        }
    }
}
