package com.example.meno.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meno.MainActivity;
import com.example.meno.R;
import com.example.meno.adapters.UsersAdapter;
import com.example.meno.databinding.ActivityGroupInfoBinding;
import com.example.meno.databinding.DialogEditGroupnameBinding;
import com.example.meno.listeners.UserListener;
import com.example.meno.models.Group;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity implements UserListener {

    ActivityGroupInfoBinding binding;
    private Group group;
    private String encodeImage;
    FirebaseFirestore database;
    private Boolean isGroupChange;
    private PreferenceManager preferenceManager;
    private String role;
    private UsersAdapter usersAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        loadGroupInfo();
        getAllUsersOfGroup();
        getRole();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        isGroupChange = false;
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(GroupInfoActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_group, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.changeGroupPhoto) {
                    changeGroupPhoto();

                } else if(menuItem.getItemId() == R.id.changeName) {
                    changeGroupName();

                } else if (menuItem.getItemId() == R.id.deleteGroup) {
                    deleteGroup();
                }
                return true;
            });
            popupMenu.show();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    getAllUsersOfGroup();
                }
                else {
                    getAdminOfGroup();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void getRole() {
        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(group.ownerId)) {
            role = "owner";
            return;
        }
        for (User member : group.members) {
            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(member.id)) {
                role = member.role;
                return;
            }
        }
    }

    private void changeGroupPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);

    }

    private void updateGroupPhoto() {
        group.image = encodeImage;
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(encodeImage));
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .update(Constants.KEY_GROUP_IMAGE, encodeImage);
        isGroupChange = true;
    }

    private void changeGroupName() {
        DialogEditGroupnameBinding dialogEditGroupnameBinding = DialogEditGroupnameBinding.inflate(getLayoutInflater());
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogEditGroupnameBinding.getRoot());

        Window window = dialog.getWindow();

        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAtrributes = window.getAttributes();
        windowAtrributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAtrributes);

        dialogEditGroupnameBinding.edtGroupName.setText(binding.textName.getText().toString());
        dialogEditGroupnameBinding.edtGroupName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        setDialogListeners(dialogEditGroupnameBinding, dialog);
        dialog.show();

    }

    private void setDialogListeners(DialogEditGroupnameBinding dialogEditGroupnameBinding, Dialog dialog) {
        dialogEditGroupnameBinding.removeButton.setOnClickListener(v -> dialogEditGroupnameBinding.edtGroupName.setText(""));

        dialogEditGroupnameBinding.cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialogEditGroupnameBinding.saveButton.setOnClickListener(v -> {
            if (!binding.textName.equals(dialogEditGroupnameBinding.edtGroupName)) {
                updateGroupName(dialogEditGroupnameBinding.edtGroupName.getText().toString());
            }
            dialog.dismiss();
        });
    }

    private void updateGroupName(String newGroupName) {
        group.name = newGroupName;
        binding.textName.setText(newGroupName);
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .update(Constants.KEY_GROUP_NAME, newGroupName);
        showToast("Updated Group Name");
        isGroupChange = true;
    }

    private void deleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Group")
                .setMessage("Are you sure to delete this group?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    database.collection(Constants.KEY_COLLECTION_GROUP)
                            .document(group.id)
                            .delete();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("deleteGroup", "deleteGroup");
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .show();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = Objects.requireNonNull(result.getData()).getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        encodeImage = encodeImage(bitmap);
                        updateGroupPhoto();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void getAllUsersOfGroup() {
        loading(true);
        if (group.members.size() > 0) {
            usersAdapter = new UsersAdapter(group.members, this);
            binding.usersRecyclerView.setAdapter(usersAdapter);
            binding.usersRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showErrorMessage();
        }
        loading(false);
    }

    private void getAdminOfGroup() {
        loading(true);
        if (group.members.size() > 0) {
            ArrayList<User> adminList = new ArrayList<>();
            for (User user : group.members) {
                if (user.role.equals("owner") || user.role.equals("admin")) {
                    adminList.add(user);
                }
            }
            usersAdapter = new UsersAdapter(adminList, this);
            binding.usersRecyclerView.setAdapter(usersAdapter);
            binding.usersRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showErrorMessage();
        }
        loading(false);
    }

    private void loadGroupInfo() {
        group = getIntent().getParcelableExtra(Constants.KEY_COLLECTION_GROUP);
        binding.textName.setText(Objects.requireNonNull(group).name);
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(group.image));
    }

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
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

        if (user.id.equals(group.ownerId)) {
            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(user.id)) {
                return;
            }
            messageWithMember(user);
            return;
        }

        if (role.equals("owner") || role.equals("admin")) {
            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(user.id)) {
                return;
            }
            PopupMenu popupMenu = new PopupMenu(GroupInfoActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_member_group_for_admin, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.message) {
                    messageWithMember(user);

                } else if (menuItem.getItemId() == R.id.setAdmin) {
                    setAdminToMember(user);

                } else if (menuItem.getItemId() == R.id.removeMember) {
                    removeMember(user);
                }
                return true;
            });
            popupMenu.show();
        } else if (role.equals("member")) {
            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(user.id)) {
                return;
            }
            messageWithMember(user);
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeMember(User user) {
        CollectionReference groupRef = database.collection(Constants.KEY_COLLECTION_GROUP);
        groupRef.document(group.id)
                .collection(Constants.KEY_COLLECTIONS_MEMBERS)
                .document(user.id)
                .delete();
        showToast("Deleted Member");
        for (User member : group.members) {
            if (member.id.equals(user.id)) {
                group.members.remove(member);
                break;
            }
        }
        usersAdapter.notifyDataSetChanged();
        isGroupChange = true;

    }

    private void messageWithMember(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    private void setAdminToMember(User user) {
        CollectionReference groupRef = database.collection(Constants.KEY_COLLECTION_GROUP);
        groupRef.document(group.id)
                .collection(Constants.KEY_COLLECTIONS_MEMBERS)
                .document(user.id)
                .update(Constants.KEY_ROLE, "admin");

        for (User member : group.members) {
            if (member.id.equals(user.id)) {
                member.role = "admin";
                break;
            }
        }
        showToast("Set Admin Successfully");
    }

    private void showToast(String message) {
        Toast.makeText(GroupInfoActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Constants.KEY_COLLECTION_GROUP);
        if (isGroupChange) {
            intent.putExtra(
                    Constants.KEY_COLLECTION_GROUP,
                    group
            );
        }
        if (isGroupChange) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}