package com.example.chit_chat;

import android.app.AlertDialog;
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

import java.util.Objects;


public class FriendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView mFriendsList;

    private FriendsFragmentAdapter mAdapter;

    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_friends,container, false);

        mFriendsList = mView.findViewById(R.id.friendRecyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<Friends> options
                = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsDatabase, Friends.class)
                .build();


        mAdapter = new FriendsFragmentAdapter(options,getContext());
        mFriendsList.setAdapter(mAdapter);
        return mView;
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