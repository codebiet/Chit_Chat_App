package com.example.chit_chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ChatFragmentAdapter extends FirebaseRecyclerAdapter<Friends,ChatFragmentAdapter.ChatViewHolder> {

    private Context context;
    private DatabaseReference mChatUsersDatabase;

    public ChatFragmentAdapter(@NonNull FirebaseRecyclerOptions<Friends> options,Context context) {
        super(options);
        this.context = context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Friends model) {

        String listUserId = getRef(position).getKey();

        mChatUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatUsersDatabase.keepSynced(true);

        assert listUserId != null;
        mChatUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String displayName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String displayImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                String displayOnlineIcon = Objects.requireNonNull(snapshot.child("Online").getValue()).toString();

                holder.friendName.setText(displayName);
                Picasso.get().load(displayImage).placeholder(R.drawable.avtar_image3).into(holder.friendImage);

                if (displayOnlineIcon.equals("true")) {
                    holder.friendOnlineIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.friendOnlineIcon.setVisibility(View.INVISIBLE);
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatIntent = new Intent(context, ChatActivity.class);
                        chatIntent.putExtra("userId", listUserId);
                        context.startActivity(chatIntent);
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
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_list_layout, parent, false);

        return new ChatFragmentAdapter.ChatViewHolder(mView);    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView friendName;
        TextView friendshipDate;
        ImageView friendOnlineIcon;
        CircleImageView friendImage;


        public ChatViewHolder(View mView) {
            super(mView);

            friendName = mView.findViewById(R.id.userSingleName);
            friendshipDate = mView.findViewById(R.id.userSingleStatus);
            friendImage = mView.findViewById(R.id.userSingleImage);
            friendOnlineIcon = mView.findViewById(R.id.userSingleOnlineIcon);
        }
    }
}
