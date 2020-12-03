package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mRegDisplayName;
    private TextInputLayout mRegEmail;
    private TextInputLayout mRegPassword;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;

    private ProgressDialog mRegProgress;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mRegProgress = new ProgressDialog(this);

        mRegDisplayName = (TextInputLayout) findViewById(R.id.regName);
        mRegEmail = (TextInputLayout) findViewById(R.id.regEmail);
        mRegPassword = (TextInputLayout) findViewById(R.id.regPassword);
        mCreateBtn = (Button) findViewById(R.id.createBtn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String displayName = mRegDisplayName.getEditText().getText().toString();
                String email = mRegEmail.getEditText().getText().toString();
                String password = mRegPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) ){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your Account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(displayName, email, password);
                }

            }
        });

        Toolbar mToolbar = findViewById(R.id.registerToolBar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void registerUser(String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            assert currentUser != null;
                            String Uid = currentUser.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);

                            HashMap <String, String> usersMap = new HashMap<>();
                            usersMap.put("deviceToken",deviceToken);
                            usersMap.put("name",displayName);
                            usersMap.put("status","Hi there, I'm Using Chit Chat App");
                            usersMap.put("image","https://firebasestorage.googleapis.com/v0/b/chit-chat-8862a.appspot.com/o/Profile_Image%2FA2wnzbydrscA4C1079qvbN1LJLE3.jpg?alt=media&token=08e9d083-f59f-46c8-8a29-f6611a72ce25");
                            usersMap.put("thumb_image","default");

                            mDatabase.setValue(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }

                                }
                            });

                        } else {
                            mRegProgress.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed. Please check the form and try again", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}