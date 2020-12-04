package com.example.chit_chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class FriendsFragmentAdapter extends FirebaseRecyclerAdapter <Friends, FriendsFragmentAdapter.FriendsViewHolder> {

    private Context context;
    private DatabaseReference mFriendUsersDatabase;

    public FriendsFragmentAdapter(@NonNull FirebaseRecyclerOptions<Friends> options, Context context){
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendsViewHolder friendsHolder, int position, @NonNull Friends model) {
        friendsHolder.friendshipDate.setText(model.getDate());

        String listUserId = getRef(position).getKey();

        mFriendUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendUsersDatabase.keepSynced(true);

        assert listUserId != null;
        mFriendUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String displayName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String displayImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                Boolean displayOnlineIcon = (Boolean) Objects.requireNonNull(snapshot.child("Online").getValue());

                friendsHolder.friendName.setText(displayName);
                Picasso.get().load(displayImage).placeholder(R.drawable.avtar_image3).into(friendsHolder.friendImage);

                if (displayOnlineIcon){
                    friendsHolder.friendOnlineIcon.setVisibility(View.VISIBLE);
                }else{
                    friendsHolder.friendOnlineIcon.setVisibility(View.INVISIBLE);
                }

                friendsHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CharSequence options[] = new CharSequence[]{"Open Profile" , "Send Messages"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

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
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {

        View mView = LayoutInflater.from(container.getContext()).inflate(R.layout.users_single_list_layout, container, false);

        return new FriendsViewHolder(mView);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder  {

        TextView friendName;
        TextView friendshipDate;
        ImageView friendOnlineIcon;
        CircleImageView friendImage;


        public FriendsViewHolder(View mView) {
            super(mView);

            friendName = mView.findViewById(R.id.userSingleName);
            friendshipDate = mView.findViewById(R.id.userSingleStatus);
            friendImage = mView.findViewById(R.id.userSingleImage);
            friendOnlineIcon = mView.findViewById(R.id.userSingleOnlineIcon);
        }
    }
}
