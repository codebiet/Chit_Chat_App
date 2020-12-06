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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
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

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mMessageAdapter;

    private static final int TOTAL_TO_ITEM_LOAD = 15;
    private int mCurrentPage = 1;
    private int itemPos = 0;

    private String mLastKey;
    private String mPrevKey;

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
                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime,getApplicationContext());

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

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
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

                itemPos++;
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
                String messageKey = snapshot.getKey();

                itemPos++;
                if(itemPos == 1){
                    mLastKey = mPrevKey = messageKey;
                }

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

            mRootRef.updateChildren(messageUserMap);

        }
    }
}