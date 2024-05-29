package com.example.meno.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.meno.adapters.UsersAdapter;
import com.example.meno.databinding.ActivitySearchBinding;
import com.example.meno.listeners.UserListener;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends BaseActivity implements UserListener {
    ActivitySearchBinding binding;
    PreferenceManager preferenceManager;
    FirebaseFirestore database;
    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        getUsers();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        binding.inputSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageCancel.setOnClickListener(v -> binding.inputSearch.setText(""));
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (binding.inputSearch.getText().length() != 0) {
                    binding.imageCancel.setVisibility(View.VISIBLE);
                } else {
                    binding.imageCancel.setVisibility(View.GONE);
                    binding.textErrorMessage.setVisibility(View.GONE);
                    binding.suggested.setVisibility(View.VISIBLE);
                    getUsers();
                }
            }
        });

        binding.inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    users.clear();
                    String text = binding.inputSearch.getText().toString().toLowerCase().trim();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .get()
                            .addOnCompleteListener(task -> {
                                loading(false);
                                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                                if (task.isSuccessful() && task.getResult() != null) {
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                        if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                            continue;
                                        }
                                        if (Objects.requireNonNull(queryDocumentSnapshot.getString(Constants.KEY_NAME)).toLowerCase().contains(text)) {
                                            User user = new User();
                                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                            user.id = queryDocumentSnapshot.getId();
                                            users.add(user);
                                        }
                                    }
                                    if (users.size() > 0) {

                                        UsersAdapter usersAdaptor = new UsersAdapter(users, SearchActivity.this::onUserClicked);
                                        binding.usersRecyclerView.setAdapter(usersAdaptor);
                                        binding.textErrorMessage.setVisibility(View.GONE);
                                        binding.usersRecyclerView.setVisibility(View.VISIBLE);
                                        usersAdaptor.notifyDataSetChanged();
                                    } else {
                                        binding.usersRecyclerView.setVisibility(View.GONE);
                                        showErrorMessage();
                                    }
                                    binding.suggested.setVisibility(View.GONE);
                                } else {
                                    showErrorMessage();
                                    binding.suggested.setVisibility(View.GONE);
                                }
                            });
                    return true;
                }
                return false;
            }
        });
    }

    private void getUsers() {
        users.clear();
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdaptor = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdaptor);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                            usersAdaptor.notifyDataSetChanged();
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(View v, User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}
