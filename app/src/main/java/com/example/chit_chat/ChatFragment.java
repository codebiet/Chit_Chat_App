package com.example.chit_chat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

public class ChatFragment extends Fragment {


    private RecyclerView mList;

    private ChatFragmentAdapter mAdapter;

    private DatabaseReference mDatabase;
//    private DatabaseReference mChatDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chat, container, false);

        mList = mView.findViewById(R.id.chatRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

//        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
//        mChatDatabase.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chat_messages").child(mCurrentUserId);
        mDatabase.keepSynced(true);

        Query conservationQuery = mDatabase.orderByChild("timeStamp");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);

        FirebaseRecyclerOptions<Friends> options
                = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(conservationQuery, Friends.class)
                .build();


        mAdapter = new ChatFragmentAdapter(options,getContext());
        mList.setAdapter(mAdapter);

        return  mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }


    @Override
    public void onStop()
    {
        super.onStop();
        mAdapter.stopListening();
    }
}