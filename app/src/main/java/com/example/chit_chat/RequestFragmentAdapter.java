package com.example.chit_chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragmentAdapter extends FirebaseRecyclerAdapter<Friends,RequestFragmentAdapter.RequestViewHolder> {

    private Context context;
    private DatabaseReference mRequestUsersDatabase;

    public RequestFragmentAdapter(@NonNull FirebaseRecyclerOptions<Friends> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Friends model) {

        String listUserId = getRef(position).getKey();

        mRequestUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestUsersDatabase.keepSynced(true);

        assert listUserId != null;
        mRequestUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String displayName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String displayImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                String displayStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                holder.friendName.setText(displayName);
                holder.friendStatus.setText(displayStatus);
                Picasso.get().load(displayImage).placeholder(R.drawable.avtar_image3).into(holder.friendImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        profileIntent.putExtra("userId", listUserId);
                        context.startActivity(profileIntent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_list_layout, parent, false);

        return new RequestFragmentAdapter.RequestViewHolder(mView);    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView friendName;
        TextView friendStatus;
        CircleImageView friendImage;

        public RequestViewHolder(@NonNull View mView) {
            super(mView);

            friendName = mView.findViewById(R.id.userSingleName);
            friendStatus = mView.findViewById(R.id.userSingleStatus);
            friendImage = mView.findViewById(R.id.userSingleImage);
        }
    }
}
