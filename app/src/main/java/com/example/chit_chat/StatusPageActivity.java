package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusPageActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatusText;
    private Button mChangeBtn;

    private DatabaseReference mStatusDatabase;

    private ProgressDialog mStatusProgress;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_page);

        mToolbar = (Toolbar) findViewById(R.id.statusToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Layout
        String statusValue = getIntent().getStringExtra("statusValue");

        mStatusText = (TextInputLayout) findViewById(R.id.statusText);
        mChangeBtn = (Button) findViewById(R.id.statusBtn);

        mStatusText.getEditText().setText(statusValue);


        //Firebase User
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = currentUser.getUid();

        //Database
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);

        //Progress
        mStatusProgress = new ProgressDialog(this);

        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress
                mStatusProgress = new ProgressDialog(StatusPageActivity.this);
                mStatusProgress.setTitle("Saving Changes");
                mStatusProgress.setMessage("Please wait while we save the changes");
                mStatusProgress.show();

                String statusText = mStatusText.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(statusText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There was some error in saving Changes", Toast.LENGTH_LONG);
                        }
                    }
                });

            }
        });

    }
}