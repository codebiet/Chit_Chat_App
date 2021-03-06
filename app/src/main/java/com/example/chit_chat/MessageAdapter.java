package com.example.chit_chat;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessagesList;

    private FirebaseAuth mAuth;


    public MessageAdapter(List<Messages> mMessagesList) {
        this.mMessagesList = mMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_message_layout_left, parent, false);
            return new MessageViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_message_layout_right, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Messages c = mMessagesList.get(position);
        String messageType = c.getType();

        if(messageType.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else{
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.avtar_image3).into(holder.messageImage);
        }
////        mAuth = FirebaseAuth.getInstance();
////        String currentUser = mAuth.getCurrentUser().getUid();
////        String fromUser = c.getFrom();
//
//        if(currentUser.equals(fromUser)){
//
//            holder.messageText.setBackgroundResource(R.drawable.sender_message_background);
//
//        }else{
//
//            holder.messageText.setBackgroundResource(R.drawable.receiver_message_background);
//        }


    }


    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.textMessageId);
            messageImage = itemView.findViewById(R.id.imageMessageId);
        }
    }

    @Override
    public int getItemViewType(int position) {

        mAuth = FirebaseAuth.getInstance();

        if(mMessagesList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())){
            return 0;
        }else {
            return 1;
        }

    }
}