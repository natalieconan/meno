package com.example.meno.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meno.databinding.ItemContainerRecentConversationBinding;
import com.example.meno.listeners.ConversationListener;
import com.example.meno.models.ChatMessage;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder> {

    private final ArrayList<ChatMessage> chatMessages;
    private final String senderId;
    private final ConversationListener conversationListener;
    // Lưu context
    private final Context mcontext;
    private final FirebaseFirestore database;
    private Boolean isReceiverAvailaible = false;

    public RecentConversationsAdapter(ArrayList<ChatMessage> chatMessages, String senderId, ConversationListener conversationListener, Context context) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.conversationListener = conversationListener;
        this.mcontext = context;
        this.database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversationBinding binding;

        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        private String truncateMessage(String message) {
            if (message.length() > 25) {
                return message.substring(0, 25);
            }
            return message;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversationImage));
            binding.textName.setText(chatMessage.conversationName);

            if (chatMessage.senderId != null && chatMessage.type != null) {
                if (chatMessage.senderId.equals(senderId)) {
                    switch (chatMessage.type) {
                        case "text":
                            binding.textRecentMessage.setText(String.format("You: %s", truncateMessage(chatMessage.message)));
                            break;
                        case "image":
                            binding.textRecentMessage.setText(String.format("You %s", "just sent an image"));
                            break;
                        case "audio":
                            binding.textRecentMessage.setText(String.format("You %s", "just send an audio"));
                            break;
                        default:
                            break;
                    }
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(chatMessage.receiverId)
                            .addSnapshotListener((Activity) mcontext, ((value, error) -> {
                                if (error != null) {
                                    return;
                                }
                                if (value != null) {
                                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                                        int availability = Objects.requireNonNull(
                                                value.getLong(Constants.KEY_AVAILABILITY)
                                        ).intValue();
                                        isReceiverAvailaible = availability == 1;
                                    }
                                }
                                if (isReceiverAvailaible) {
                                    binding.userAvailability.setVisibility(View.VISIBLE);
                                } else {
                                    binding.userAvailability.setVisibility(View.GONE);
                                }
                            }));
                } else {
                    switch (chatMessage.type) {
                        case "text":
                            binding.textRecentMessage.setText(String.format("%s : %s", binding.textName.getText().toString(), truncateMessage(chatMessage.message)));
                            break;
                        case "image":
                            binding.textRecentMessage.setText(String.format("%s %s", binding.textName.getText().toString(), "sent you an image"));
                            break;
                        case "audio":
                            binding.textRecentMessage.setText(String.format("%s %s", binding.textName.getText().toString(), "sent you an audio"));
                            break;
                        default:
                            break;
                    }
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(chatMessage.senderId)
                            .addSnapshotListener((Activity) mcontext, ((value, error) -> {
                                if (error != null) {
                                    return;
                                }
                                if (value != null) {
                                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                                        int availability = Objects.requireNonNull(
                                                value.getLong(Constants.KEY_AVAILABILITY)
                                        ).intValue();
                                        isReceiverAvailaible = availability == 1;
                                    }
                                }
                                if (isReceiverAvailaible) {
                                    binding.userAvailability.setVisibility(View.VISIBLE);
                                } else {
                                    binding.userAvailability.setVisibility(View.GONE);
                                }
                            }));
                }
            }
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversationId;
                user.name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                conversationListener.onConversationClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
