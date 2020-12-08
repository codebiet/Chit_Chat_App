package com.example.chit_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;

    private TextView mDisplayName;
    private TextView mLastSeen;
    private CircleImageView mDisplayImage;

    private ImageButton mPickImageBtn;
    private ImageButton mSendBtn;
    private EditText mTypedMessage;

    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private String downloadUrl;

    private List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mMessageAdapter;

    private static final int TOTAL_TO_ITEM_LOAD = 15;
    private int mCurrentPage = 1;
    private int itemPos = 0;

    private String mLastKey;
    private String mPrevKey;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatUser = getIntent().getStringExtra("userId");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        Toolbar mToolbar = findViewById(R.id.chatAppBar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(actionBarView);

        mDisplayName = findViewById(R.id.customBarDisplayName);
        mLastSeen = findViewById(R.id.customBarLastSeenStatus);
        mDisplayImage = findViewById(R.id.customBarImage);

        mPickImageBtn = findViewById(R.id.pickImageBtn);
        mSendBtn = findViewById(R.id.sendMessageBtn);
        mTypedMessage = findViewById(R.id.messageTextView);

        mMessageAdapter = new MessageAdapter(messageList);

        mRefreshLayout = findViewById(R.id.swipeMessageLayout);

        mMessagesList = findViewById(R.id.chatMessagesList);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mMessageAdapter);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        
        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendName = snapshot.child("name").getValue().toString();
                String friendImage = snapshot.child("image").getValue().toString();
                String online = snapshot.child("Online").getValue().toString();

                mDisplayName.setText(friendName);
                Picasso.get().load(friendImage).placeholder(R.drawable.avtar_image3).into(mDisplayImage);

                if (online.equals("true")){
                    mLastSeen.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());

                    mLastSeen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mRootRef.child("chat_messages").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat_messages/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("chat_messages/" + mChatUser + "/" + mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap);
                }
//                else{
//
//                    Map chatAddMap = new HashMap();
//                    chatAddMap.put("seen", true);
//                    chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);
//
//                    Map chatUserMap = new HashMap();
//                    chatUserMap.put("chat_messages/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
//                    chatUserMap.put("chat_messages/" + mChatUser + "/" + mCurrentUserId,chatAddMap);
//
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        --------------SEND BUTTON CLICKED----------------------

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mPickImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);

            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRootRef.child("Users").child(mCurrentUserId).child("Online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();

//        mRootRef.child("Users").child(mCurrentUserId).child("Online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            final String currentUserRef = "messages/" + mCurrentUserId + "/" +mChatUser;
            final String friendUserRef = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String pushId = userMessagePush.getKey();

            final StorageReference filePath = mImageStorage.child("message_image").child(pushId + ".jpg");


            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                              downloadUrl = task.getResult().toString();
                            }
                        });

                        Toast.makeText(ChatActivity.this, downloadUrl, Toast.LENGTH_LONG).show();
                        Map messageMap = new HashMap();
                        messageMap.put("message", downloadUrl);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + pushId,messageMap);
                        messageUserMap.put(friendUserRef + "/" + pushId,messageMap);

                        mTypedMessage.setText("");

                        mRootRef.updateChildren(messageUserMap);

                    }

                }
            });

        }
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef =  mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_TO_ITEM_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages messages = snapshot.getValue(Messages.class);
                String messageKey = snapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messageList.add(itemPos++, messages);
                }else{
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1){
                    mLastKey = messageKey;
                }

                mMessageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(15,0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void loadMessages() {

        DatabaseReference messageRef =  mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(TOTAL_TO_ITEM_LOAD * mCurrentPage);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages messages = snapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos == 1){
                    String messageKey = snapshot.getKey();
                    mLastKey = mPrevKey = messageKey;
                }

//                assert messages != null;
//                if(!messages.getSeen()){
//                    mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).child(Objects.requireNonNull(snapshot.getKey())).child("seen").setValue(true);
////                    mRootRef.child("messages").child(mChatUser).child(mCurrentUserId).child(Objects.requireNonNull(snapshot.getKey())).child("seen").setValue(true);
//                }

                messageList.add(messages);
                mMessageAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messageList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void sendMessage() {

        String message = mTypedMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String currentUserRef = "messages/" + mCurrentUserId + "/" + mChatUser;
            String friendUserRef = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId,messageMap);
            messageUserMap.put(friendUserRef + "/" + pushId,messageMap);

            mTypedMessage.setText("");

            mRootRef.child("chat_messages").child(mCurrentUserId).child(mChatUser).child("timeStamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap);

        }
    }
}