package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

/*
        0 == Not Friend
        1 == Request send
        2 == Request Received
        3 == Already Friend
*/

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileDisplayImage;
    private TextView mProfileDisplayName, mProfileDisplayStatus, mProfileFriendsCount;
    private Button mFriendRequestBtn, mRequestDeclineBtn;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private int mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = getIntent().getStringExtra("userId");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileDisplayName = findViewById(R.id.profileDisplayName);
        mProfileDisplayImage = findViewById(R.id.profileDisplayImage);
        mProfileDisplayStatus = findViewById(R.id.profileDisplayStatus);
        mProfileFriendsCount = findViewById(R.id.profileTotalFriends);
        mFriendRequestBtn = findViewById(R.id.sentFriendRequestBtn);
        mRequestDeclineBtn = findViewById(R.id.declineFriendRequestBtn);

        mCurrentState = 0;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String displayName = snapshot.child("name").getValue().toString();
                String displayStatus = snapshot.child("status").getValue().toString();
                String displayImage = snapshot.child("image").getValue().toString();

                mProfileDisplayName.setText(displayName);
                mProfileDisplayStatus.setText(displayStatus);
                Picasso.get().load(displayImage).placeholder(R.drawable.avtar_image3).into(mProfileDisplayImage);

                if(mCurrentUser.getUid().equals(userId)){
                    mFriendRequestBtn.setEnabled(false);
                    mRequestDeclineBtn.setEnabled(false);
                }else{
                    mFriendRequestBtn.setEnabled(true);
                    mRequestDeclineBtn.setEnabled(true);
                }

//                mProgressDialog.dismiss();

//                ---------------------FRIEND LIST / REQUEST FEATURE-----------------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.hasChild(userId)){

                            String requestType = snapshot.child(userId).child("requestType").getValue().toString();

                            if(requestType.equals("received")){
                                mCurrentState = 2;
                                mFriendRequestBtn.setText("Accept Friend Request");
//                              mFriendRequestBtn.setBackgroundColor(Color.parseColor("#419E14"));
                            }
                            else if(requestType.equals("sent")){
                                mCurrentState = 1;
                                mFriendRequestBtn.setText("Cancel Request");
//                              mFriendRequestBtn.setBackgroundColor(Color.parseColor("#FF0000"));
                            }

                            mProgressDialog.dismiss();

                        }else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(userId)){

                                        mCurrentState = 3;
                                        mFriendRequestBtn.setText("Unfriend");

                                        mProgressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mProgressDialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


//        =========================WHEN BUTTON IS CLICKED===========================================

        mFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendRequestBtn.setEnabled(false);

//                ---------------------NOT FRIENDS STATE--------------------------
                if(mCurrentState == 0){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId).child("requestType")
                            .setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid()).child("requestType")
                                        .setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mCurrentState = 1;
                                                mFriendRequestBtn.setText("Cancel Request");
                                                mFriendRequestBtn.setBackgroundColor(Color.parseColor("#FF0000"));

                                            }
                                        });

                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Sent Request",Toast.LENGTH_LONG).show();
                            }

                            mFriendRequestBtn.setEnabled(true);
                        }
                    });

                }



//                ----------------------CANCEL REQUEST STATE------------------------
                if(mCurrentState == 1){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            mCurrentState = 0;
                                            mFriendRequestBtn.setText("Send Friend Request");
//                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));

                                        }else{
                                            Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                                        }

                                        mFriendRequestBtn.setEnabled(true);
                                    }
                                });

                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }



//            ----------------------------REQUEST RECEIVED STATE---------------------------------
                if(mCurrentState == 2){

                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(userId).child(mCurrentUser.getUid()).setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            mCurrentState = 3;
                                                            mFriendRequestBtn.setText("Unfriend");
//                                                            mFriendRequestBtn.setBackgroundTintMode();

                                                        }else{
                                                            Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                                                        }

                                                        mFriendRequestBtn.setEnabled(true);
                                                    }
                                                });

                                            }else{
                                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });

                }


//            ----------------------------REQUEST RECEIVED STATE---------------------------------
                if(mCurrentState == 3){

                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendDatabase.child(userId).child(mCurrentUser.getUid())
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            mCurrentState = 0;
                                            mFriendRequestBtn.setText("Send Friend Request");
//                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
                                        }else{
                                            Toast.makeText(ProfileActivity.this,"Failed to Unfriend",Toast.LENGTH_LONG).show();
                                        }
                                        mFriendRequestBtn.setEnabled(true);
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Unfriend",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

            }
        });

    }
}