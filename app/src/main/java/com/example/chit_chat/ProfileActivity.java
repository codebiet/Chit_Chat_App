package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private int mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = getIntent().getStringExtra("userId");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileDisplayName = findViewById(R.id.profileDisplayName);
        mProfileDisplayImage = findViewById(R.id.profileDisplayImage);
        mProfileDisplayStatus = findViewById(R.id.profileDisplayStatus);
        mProfileFriendsCount = findViewById(R.id.profileTotalFriends);
        mFriendRequestBtn = findViewById(R.id.sentFriendRequestBtn);
        mRequestDeclineBtn = findViewById(R.id.declineFriendRequestBtn);

        mCurrentState = 0;

        mRequestDeclineBtn.setVisibility(View.INVISIBLE);
        mRequestDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String displayName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String displayStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                String displayImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                mProfileDisplayName.setText(displayName);
                mProfileDisplayStatus.setText(displayStatus);
                Picasso.get().load(displayImage).placeholder(R.drawable.avtar_image3).into(mProfileDisplayImage);

                if(mCurrentUser.getUid().equals(userId)){
                    mFriendRequestBtn.setVisibility(View.INVISIBLE);
                    mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                    mFriendRequestBtn.setEnabled(false);
                    mRequestDeclineBtn.setEnabled(false);
                }

//                mProgressDialog.dismiss();

//                ------------------------FRIEND LIST / REQUEST FEATURE-----------------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.hasChild(userId)){

                            String requestType = Objects.requireNonNull(snapshot.child(userId).child("request_type").getValue()).toString();

                            if(requestType.equals("received")){
                                mCurrentState = 2;
                                mFriendRequestBtn.setText("Accept Friend Request");
//                              mFriendRequestBtn.setBackgroundColor(Color.parseColor("#419E14"));

                                mRequestDeclineBtn.setVisibility(View.VISIBLE);
                                mRequestDeclineBtn.setEnabled(true);
                            }
                            else if(requestType.equals("sent")){
                                mCurrentState = 1;
                                mFriendRequestBtn.setText("Cancel Request");
//                              mFriendRequestBtn.setBackgroundColor(Color.parseColor("#FF0000"));
                                mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                                mRequestDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        }else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(userId)){

                                        mCurrentState = 3;
                                        mFriendRequestBtn.setText("UnFriend");
                                        mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                                        mRequestDeclineBtn.setEnabled(false);

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



        mRequestDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRequestDeclineBtn.setEnabled(false);
                mRequestDeclineBtn.setVisibility(View.INVISIBLE);

                Map cancelRequestMap = new HashMap();
                cancelRequestMap.put("Friend_Request/" + mCurrentUser.getUid() + "/" + userId, null);
                cancelRequestMap.put("Friend_Request/" + userId + "/" + mCurrentUser.getUid(), null);

                mRootRef.updateChildren(cancelRequestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                        if (error == null) {

                            mCurrentState = 0;
                            mFriendRequestBtn.setText("Send Friend Request");
//                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));

                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to Cancel Request", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



//        =========================WHEN BUTTON IS CLICKED===========================================

        mFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendRequestBtn.setEnabled(false);

//                ---------------------NOT FRIENDS STATE--------------------------
                if(mCurrentState == 0){

                    DatabaseReference newNotification = mRootRef.child("notification").child(userId).push();
                    String newNotificationId = newNotification.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_Request/" + mCurrentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_Request/" + userId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notification/" + userId +  "/" + newNotificationId , notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){
                                mFriendRequestBtn.setEnabled(true);
                                mCurrentState = 1;
                                mFriendRequestBtn.setText("Cancel Request");
//                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#FF0000"));
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Sent Request",Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }



//                ----------------------CANCEL REQUEST STATE------------------------
                if(mCurrentState == 1){

                    Map cancelRequestMap = new HashMap();
                    cancelRequestMap.put("Friend_Request/" + mCurrentUser.getUid() + "/" + userId, null);
                    cancelRequestMap.put("Friend_Request/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(cancelRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){

                                mCurrentState = 0;
                                mFriendRequestBtn.setText("Send Friend Request");
//                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
                                mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                                mRequestDeclineBtn.setEnabled(false);

                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                            }

                            mFriendRequestBtn.setEnabled(true);

                        }
                    });

//
                }



//            ----------------------------REQUEST RECEIVED STATE---------------------------------
                if(mCurrentState == 2){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendMap.put("Friend_Request/" + mCurrentUser.getUid() + "/" + userId, null);
                    friendMap.put("Friend_Request/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){
                                mCurrentState = 3;
                                mFriendRequestBtn.setText("UnFriend");
//                              mFriendRequestBtn.setBackgroundTintMode();
                                mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                                mRequestDeclineBtn.setEnabled(false);
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
                            }

                            mFriendRequestBtn.setEnabled(true);
                        }
                    });
                }



//            ----------------------------UN FRIEND---------------------------------
                if(mCurrentState == 3){

                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unFriendMap.put("Friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    unFriendMap.put("messages/" + mCurrentUser.getUid() + "/" + userId, null);
                    unFriendMap.put("messages/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if(error == null){
                                mCurrentState = 0;
                                mFriendRequestBtn.setText("Send Friend Request");
//                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
                                mRequestDeclineBtn.setVisibility(View.INVISIBLE);
                                mRequestDeclineBtn.setEnabled(false);
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to UnFriend",Toast.LENGTH_LONG).show();
                            }
                            mFriendRequestBtn.setEnabled(true);

                        }
                    });

                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mRootRef.child("Users").child(mCurrentUser.getUid()).child("Online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();

//        mRootRef.child("Users").child(mCurrentUser.getUid()).child("Online").setValue(ServerValue.TIMESTAMP);
    }
}


//mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId).child("requestType")
//        .setValue("sent")
//        .addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//
//        if(task.isSuccessful()){
//
//        mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid()).child("requestType")
//        .setValue("received")
//        .addOnSuccessListener(new OnSuccessListener<Void>() {
//@Override
//public void onSuccess(Void aVoid) {
//

//        });
//
//
//
//        }
//        });
//
//        }else{
//        Toast.makeText(ProfileActivity.this,"Failed to Sent Request",Toast.LENGTH_LONG).show();
//        }
//
//        mFriendRequestBtn.setEnabled(true);
//        }
//        });






//                      mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
//                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//
//                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
//                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        if(task.isSuccessful()){
//
//                                            mCurrentState = 0;
//                                            mFriendRequestBtn.setText("Send Friend Request");
////                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
//                                            mRequestDeclineBtn.setVisibility(View.INVISIBLE);
//                                            mRequestDeclineBtn.setEnabled(false);
//
//                                        }else{
//                                            Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
//                                        }
//
//                                        mFriendRequestBtn.setEnabled(true);
//                                    }
//                                });
//
//                            }else{
//                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });










//                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
//                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//
//                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
//                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        if(task.isSuccessful()){
//
//                                            mCurrentState = 0;
//                                            mFriendRequestBtn.setText("Send Friend Request");
////                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
//                                            mRequestDeclineBtn.setVisibility(View.INVISIBLE);
//                                            mRequestDeclineBtn.setEnabled(false);
//
//                                        }else{
//                                            Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
//                                        }
//
//                                        mFriendRequestBtn.setEnabled(true);
//                                    }
//                                });
//
//                            }else{
//                                Toast.makeText(ProfileActivity.this,"Failed to Cancel Request",Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });














//                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId)
//                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//
//                                mFriendDatabase.child(userId).child(mCurrentUser.getUid())
//                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        if(task.isSuccessful()){
//                                            mCurrentState = 0;
//                                            mFriendRequestBtn.setText("Send Friend Request");
////                                            mFriendRequestBtn.setBackgroundColor(Color.parseColor("#1261A0"));
//                                            mRequestDeclineBtn.setVisibility(View.INVISIBLE);
//                                            mRequestDeclineBtn.setEnabled(false);
//                                        }else{
//                                            Toast.makeText(ProfileActivity.this,"Failed to Unfriend",Toast.LENGTH_LONG).show();
//                                        }
//                                        mFriendRequestBtn.setEnabled(true);
//                                    }
//                                });
//                            }else{
//                                Toast.makeText(ProfileActivity.this,"Failed to Unfriend",Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });