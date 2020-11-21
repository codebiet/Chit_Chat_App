 package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

 public class SettingsActivity extends AppCompatActivity {

     private DatabaseReference mUserDatabase;
     private FirebaseUser mCurrentUser;

     private Button mChangeStatusBtn;

     private CircleImageView mDisplayImage;
     private TextView mDisplayName;
     private TextView mDisplayStatus;
     private ImageView thumbImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar mToolbar = findViewById(R.id.accountToolBar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisplayImage = (CircleImageView) findViewById(R.id.accountImage);
        mDisplayName = (TextView) findViewById(R.id.accountName);
        mDisplayStatus = (TextView) findViewById(R.id.accountStatus);
        mChangeStatusBtn = (Button) findViewById(R.id.accountChangeStatusBtn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String thumbImage = snapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String statusValue = mDisplayStatus.getText().toString();
                Intent statusPageIntent = new Intent(SettingsActivity.this, StatusPageActivity.class);
                statusPageIntent.putExtra("statusValue", statusValue);
                startActivity(statusPageIntent);

            }
        });

    }
}