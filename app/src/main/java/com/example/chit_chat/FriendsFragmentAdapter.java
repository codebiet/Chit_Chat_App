package com.example.chit_chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragmentAdapter extends FirebaseRecyclerAdapter <Friends, FriendsFragmentAdapter.FriendsViewHolder> {

//    private Context context;

    public FriendsFragmentAdapter(@NonNull FirebaseRecyclerOptions<Friends> options){
        super(options);
//        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {
        holder.friendshipDate.setText(model.getDate());
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {

        View mView = LayoutInflater.from(container.getContext()).inflate(R.layout.users_single_list_layout, container, false);

        return new FriendsViewHolder(mView);
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder  {

        TextView friendshipDate;

        public FriendsViewHolder(View mView) {
            super(mView);

             friendshipDate = mView.findViewById(R.id.userSingleStatus);
        }
    }
}
