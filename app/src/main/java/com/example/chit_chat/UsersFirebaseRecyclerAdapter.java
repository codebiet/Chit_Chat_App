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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<Users, UsersFirebaseRecyclerAdapter.UsersViewHolder> {

    private Context context;

    public UsersFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Users> options, Context context){
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
        holder.userName.setText(model.getName());
        holder.userStatus.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.avtar_image3).into(holder.userImage);

        String userId = getRef(position).getKey();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtra("userId",userId);
                context.startActivity(profileIntent);

            }
        });
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_list_layout, parent, false);

        return new UsersViewHolder(view);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        TextView userStatus;
        CircleImageView userImage;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userSingleName);
            userStatus = itemView.findViewById(R.id.userSingleStatus);
            userImage = itemView.findViewById(R.id.userSingleImage);

        }
    }

//    public void IntentCaller(String userId){
//        Intent profileIntent = new Intent(,ProfileActivity.class);
//        profileIntent.putExtra("userId",userId);
//        startActivity(profileIntent);
//    }
}
