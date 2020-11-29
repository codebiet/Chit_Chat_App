 package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.File;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

 public class SettingsActivity extends AppCompatActivity {

//     private Scanner scan = new Scanner(System.in);

     private DatabaseReference mUserDatabase;
     private FirebaseUser mCurrentUser;
     private StorageReference mImageStorage;

     private Button mChangeStatusBtn;
     private Button mChangeImageBtn;

     private CircleImageView mDisplayImage;
     private TextView mDisplayName;
     private TextView mDisplayStatus;
     private ImageView thumbImage;

     private ProgressDialog mProgressDialog;

     private static final int galleryPicker = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        System.out.println("Entering in the activity");


        Toolbar mToolbar = findViewById(R.id.accountToolBar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Layout Display
        mDisplayImage = (CircleImageView) findViewById(R.id.accountImage);
        mDisplayName = (TextView) findViewById(R.id.accountName);
        mDisplayStatus = (TextView) findViewById(R.id.accountStatus);

        //Buttons
        mChangeStatusBtn = (Button) findViewById(R.id.accountChangeStatusBtn);
        mChangeImageBtn = (Button) findViewById(R.id.accountChangeImageBtn);

        //Firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
//                String thumbImage = snapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);

                if(!image.equals("default")) {
                    Picasso.get().load(image).placeholder(R.drawable.avtar_image3).into(mDisplayImage);
                }
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

        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), galleryPicker);

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);

            }
        });

    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if(requestCode == galleryPicker && resultCode == RESULT_OK){
             Uri imageUri = data.getData();

             CropImage.activity(imageUri)
                     .setAspectRatio(1,1)
                     .start(SettingsActivity.this);
         }
         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK) {

                 mProgressDialog = new ProgressDialog(SettingsActivity.this);
                 mProgressDialog.setTitle("Uploading Image");
                 mProgressDialog.setMessage("Please wait while we upload and process");
                 mProgressDialog.setCanceledOnTouchOutside(false);
                 mProgressDialog.show();

                 Uri resultUri = result.getUri();

//                 File thumbFilePath = new File(resultUri.getPath());

                 String currentUid = mCurrentUser.getUid();

//                 Bitmap thumbBitmap = new Compressor()
//                         .s
//                         .compressToBItmap(this,thumbFilePath);

                 final StorageReference filePath = mImageStorage.child("Profile_Image").child(currentUid + ".jpg");

                 filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if(task.isSuccessful()){
                             filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Uri> task) {
                                     if(task.isSuccessful()){
                                         String download_url = task.getResult().toString();
                                         mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if(task.isSuccessful()){
                                                     mProgressDialog.dismiss();
                                                     Toast.makeText(SettingsActivity.this, "Success Uploading", Toast.LENGTH_LONG).show();
                                                 }else{
                                                     mProgressDialog.dismiss();
                                                     Toast.makeText(SettingsActivity.this, "Error in Uploading", Toast.LENGTH_LONG).show();
                                                 }
                                             }
                                         });
                                     }
                                 }
                             });
                         }else{

                             mProgressDialog.dismiss();
                             Toast.makeText(SettingsActivity.this, "Error in Uploading", Toast.LENGTH_LONG).show();

                         }

                     }
                 });

             } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                 Exception error = result.getError();
             }

         }
     }
 }