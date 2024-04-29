package com.example.meno.listeners;

import android.view.View;

import com.example.meno.models.User;

public interface UserListener {
    void onUserClicked(View v, User user);
}
