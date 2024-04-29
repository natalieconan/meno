package com.example.meno.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meno.adapters.UsersAdapter;
import com.example.meno.databinding.ActivityGroupBinding;
import com.example.meno.listeners.UserListener;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GroupActivity extends AppCompatActivity implements UserListener {

    private static ActivityGroupBinding binding;
    private String encodeImage;
    private PreferenceManager preferenceManager;
    private String groupId = null;
    private static ArrayList<User> selectedUser;
    private FirebaseFirestore database;
    private CollectionReference memberRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    private void init() {
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        selectedUser = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        memberRef = database.collection(Constants.KEY_COLLECTION_GROUP);
    }

    private void createGroup() {
        HashMap<String, Object> group = new HashMap<>();
        group.put(Constants.KEY_OWNER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        group.put(Constants.KEY_OWNER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        group.put(Constants.KEY_GROUP_IMAGE, encodeImage);
        group.put(Constants.KEY_GROUP_NAME, binding.inputGroupName.getText().toString());
        group.put(Constants.KEY_TIMESTAMP, new Date());
        DocumentReference ref = database.collection(Constants.KEY_COLLECTION_GROUP).document();
        groupId = ref.getId();
        addGroup(group);
        addMemberToGroup();
        onBackPressed();
    }

    private void addGroup(HashMap<String, Object> group) {
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(groupId)
                .set(group);
    }

    private void addMemberToGroup() {
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        user.put(Constants.KEY_ROLE, "owner");
        memberRef.document(groupId).collection(Constants.KEY_COLLECTIONS_MEMBERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .set(user);

        for(User user1 : selectedUser) {
            HashMap<String, Object> user2 = new HashMap<>();
            user2.put(Constants.KEY_USER_ID, user1.id);
            user2.put(Constants.KEY_ROLE, "member");
            memberRef.document(groupId).collection(Constants.KEY_COLLECTIONS_MEMBERS)
                    .document(user1.id)
                    .set(user2);
        }
        showToast("Group Created");
    }

    private void setListeners() {
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonCreate.setOnClickListener(v -> {
            if (!selectedUser.isEmpty()) {
                createGroup();
            }
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<User> users = new ArrayList<>();
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
                            UsersAdapter usersAdapter = new UsersAdapter(users, null);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
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

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    public static void onCheckedChangeListener(User user, Boolean isSelect) {
        if (isSelect) {
            selectedUser.add(user);
        } else {
            selectedUser.remove(user);
        }
        loadingButton(!selectedUser.isEmpty());
    }

    public static void loadingButton(Boolean isLoading) {
        if (isLoading) {
            binding.buttonCreate.setVisibility(View.VISIBLE);
        } else {
            binding.buttonCreate.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicked(View v, User user) {}
}