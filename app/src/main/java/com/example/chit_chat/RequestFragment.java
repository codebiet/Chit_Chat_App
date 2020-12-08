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

import java.util.Objects;


public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;

    private RequestFragmentAdapter mAdapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestList = mView.findViewById(R.id.requestRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(mCurrentUserId);
        mDatabase.keepSynced(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<Friends> options
                = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mDatabase, Friends.class)
                .build();


        mAdapter = new RequestFragmentAdapter(options,getContext());
        mRequestList.setAdapter(mAdapter);

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