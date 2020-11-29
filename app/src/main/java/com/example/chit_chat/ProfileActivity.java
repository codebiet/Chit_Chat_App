package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileDisplayImage;
    private TextView mProfileDisplayName, mProfileDisplayStatus, mProfileFriendsCount;
    private Button mSentFriendRequestBtn;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendRequestDatabase;

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

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileDisplayName = findViewById(R.id.profileDisplayName);
        mProfileDisplayImage = findViewById(R.id.profileDisplayImage);
        mProfileDisplayStatus = findViewById(R.id.profileDisplayStatus);
        mProfileFriendsCount = findViewById(R.id.profileTotalFriends);
        mSentFriendRequestBtn = findViewById(R.id.sentFriendRequestBtn);

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

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSentFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                                                Toast.makeText(ProfileActivity.this,"Request Sent Successfully",Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Sent Request",Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }

            }
        });

    }
}