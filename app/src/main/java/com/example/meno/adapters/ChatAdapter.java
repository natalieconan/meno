package com.example.meno.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meno.databinding.ItemContainerRecievedMessageBinding;
import com.example.meno.databinding.ItemContainerSentMessageBinding;
import com.example.meno.models.ChatMessage;
import com.example.meno.utilities.ImageDownloaderTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

// Chat Adapter tackle the changes which related to Chat RecyclerView (chat history of users)
// Update and attach data to viewHolder and display to user
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;

    private final String senderId;

    // 2 side of Chat RecyclerView, 1 for sender, other for receiver
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(Bitmap receiverProfileImage, ArrayList<ChatMessage> chatMessages, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerRecievedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    // return the amount of messages between 2 users
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // return the appropriate VIEW_TYPE for sender and receiver
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return  VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        // binding ViewHolder design
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        private void bindImageToImageView(String imageUrl, RoundedImageView imageView)  {
            ImageDownloaderTask downloaderTask = new ImageDownloaderTask(imageView);
            downloaderTask.execute(imageUrl);
        }

        // attach data to viewHolder base on message type
        void setData(ChatMessage chatMessage) {
            switch (chatMessage.type) {
                case "text":
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    break;
                case "image":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.get().load(chatMessage.message).into(binding.imageMessage);
                    break;
                case "audio":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                    break;
            }
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerRecievedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding) {
            super(itemContainerRecievedMessageBinding.getRoot());
            binding = itemContainerRecievedMessageBinding;
        }

        private void bindImageToImageView(String imageUrl, RoundedImageView imageView)  {
            ImageDownloaderTask downloaderTask = new ImageDownloaderTask(imageView);
            downloaderTask.execute(imageUrl);
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            switch (chatMessage.type) {
                case "text":
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    break;
                case "image":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.get().load(chatMessage.message).into(binding.imageMessage);
                    break;
                case "audio":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                    break;
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
