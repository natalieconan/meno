package com.example.meno.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meno.databinding.ItemContainerGroupReceivedMessageBinding;
import com.example.meno.databinding.ItemContainerGroupSendMessageBinding;
import com.example.meno.models.GroupMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<GroupMessage> groupMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    public GroupChatAdapter(ArrayList<GroupMessage> groupMessages, String senderId) {
        this.groupMessages = groupMessages;
        this.senderId = senderId;
    }

    // Tạo viewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerGroupSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerGroupReceivedMessageBinding.inflate(
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
            ((SentMessageViewHolder) holder).setData(groupMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(groupMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (groupMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            receiverProfileImage = groupMessages.get(position).imageBitmap;
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerGroupSendMessageBinding binding;

        SentMessageViewHolder(ItemContainerGroupSendMessageBinding itemContainerGroupSendMessageBinding) {
            super(itemContainerGroupSendMessageBinding.getRoot());
            binding = itemContainerGroupSendMessageBinding;
        }

        void setData(GroupMessage groupMessage) {
            switch (groupMessage.type) {
                case "text":
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);
                    binding.textMessage.setText(groupMessage.message);
                    break;
                case "image":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.get().load(groupMessage.message).into(binding.imageMessage);
                    break;
                case "audio":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                    break;
            }
            binding.textDateTime.setText(groupMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerGroupReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerGroupReceivedMessageBinding itemContainerGroupReceivedMessageBinding) {
            super(itemContainerGroupReceivedMessageBinding.getRoot());
            binding = itemContainerGroupReceivedMessageBinding;
        }

        void setData(GroupMessage groupMessage, Bitmap receiverProfileImage) {
            switch (groupMessage.type) {
                case "text":
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);
                    binding.textMessage.setText(groupMessage.message);
                    break;
                case "image":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.get().load(groupMessage.message).into(binding.imageMessage);
                    break;
                case "audio":
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                    break;
            }
            binding.textDateTime.setText(groupMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }

}
