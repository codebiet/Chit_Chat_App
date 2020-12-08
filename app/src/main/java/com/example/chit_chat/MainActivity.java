package com.example.chit_chat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseOnlineStatus;

    private ViewPager mViewPager;
    private SecctionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Toolbar mToolbar = findViewById(R.id.mainToolBar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Chit Chat");

        if(mAuth.getCurrentUser() != null){
            mDatabaseOnlineStatus = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        }
        mViewPager = (ViewPager) findViewById(R.id.mainPager);
        mSectionPagerAdapter = new SecctionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.mainTab);
        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser == null){
            sendToStartActivity();
        }else{
            mDatabaseOnlineStatus.child("Online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null){
//            mDatabaseOnlineStatus.child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }


    private void sendToStartActivity() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.mainLogoutBtn){
            FirebaseAuth.getInstance().signOut();
            sendToStartActivity();
        }
        if(item.getItemId() == R.id.mainAccountSettingsBtn){
            Intent accountSettingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(accountSettingIntent);
        }
        if(item.getItemId() == R.id.mainAllUserBtn){
            Intent allUsersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(allUsersIntent);
        }

        return true;
    }
}