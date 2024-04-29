package com.example.meno.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meno.activities.GroupActivity;
import com.example.meno.databinding.ItemContainerUserBinding;
import com.example.meno.databinding.ItemContainerUserGroupBinding;
import com.example.meno.listeners.UserListener;
import com.example.meno.models.User;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final ArrayList<User> users;
    private final UserListener userListener;
    public static final int VIEW_TYPE_FRAGMENT_USER = 1;
    public static final int VIEW_TYPE_FRAGMENT_GROUP = 2;
    private final int VIEW_TYPE;

    public UsersAdapter(ArrayList<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
        VIEW_TYPE = getViewType(userListener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.VIEW_TYPE == VIEW_TYPE_FRAGMENT_USER) {
            ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new UserViewHolder(itemContainerUserBinding);
        } else{
            ItemContainerUserGroupBinding itemContainerUserGroupBinding = ItemContainerUserGroupBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new UserGroupViewHolder(itemContainerUserGroupBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (this.VIEW_TYPE == VIEW_TYPE_FRAGMENT_USER) {
            ((UserViewHolder)holder).setUserData(users.get(position));
        } else {
            ((UserGroupViewHolder)holder).setUserData(users.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public int getViewType(UserListener userListener) {
        if (userListener != null) {
            return VIEW_TYPE_FRAGMENT_USER;
        }
        return VIEW_TYPE_FRAGMENT_GROUP;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(v, user));
        }
    }

    class UserGroupViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserGroupBinding binding;

        UserGroupViewHolder(ItemContainerUserGroupBinding itemContainerUserGroupBinding) {
            super(itemContainerUserGroupBinding.getRoot());
            binding = itemContainerUserGroupBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.cbSelectUser.setOnCheckedChangeListener((compoundButton, b) -> GroupActivity.onCheckedChangeListener(user, compoundButton.isChecked()));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}