package com.example.meno.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meno.activities.GroupMessageActivity;
import com.example.meno.databinding.ItemContainerRecentGroupBinding;
import com.example.meno.models.Group;
import com.example.meno.utilities.Constants;

import java.util.ArrayList;

public class RecentGroupAdapter extends RecyclerView.Adapter<RecentGroupAdapter.GroupViewHolder>{
    // list groups contain user
    private static ArrayList<Group> groups = new ArrayList<>();

    public RecentGroupAdapter(ArrayList<Group> groups) {
        RecentGroupAdapter.groups = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(
                ItemContainerRecentGroupBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.setData(groups.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GroupMessageActivity.class);
            intent.putExtra(Constants.KEY_COLLECTION_GROUP, groups.get(position));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static void updateLastMessage(Group updatedGroup) {
        for (Group group : groups) {
            if (group.id.equals(updatedGroup.id)) {
                groups.remove(group);
                groups.add(updatedGroup);
                break;
            }
        }
        groups.sort((obj1, obj2) -> obj2.lastMessageModel.dateObject.compareTo(obj1.lastMessageModel.dateObject));
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentGroupBinding binding;
        GroupViewHolder(ItemContainerRecentGroupBinding itemContainerRecentGroupBinding) {
            super(itemContainerRecentGroupBinding.getRoot());
            binding = itemContainerRecentGroupBinding;
        }

        void setData(Group group) {
            binding.imageProfile.setImageBitmap(getConversationImage(group.image));
            binding.textName.setText(group.name);
            if (group.lastMessageModel != null) {
                binding.textRecentMessage.setText(group.lastMessageModel.message);
            } else {
                String recentMessage = "Last Message";
                binding.textRecentMessage.setText(recentMessage);
            }
        }
    }

    private Bitmap getConversationImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}