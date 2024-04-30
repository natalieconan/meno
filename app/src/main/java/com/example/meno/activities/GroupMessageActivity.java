package com.example.meno.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meno.adapters.GroupChatAdapter;
import com.example.meno.adapters.RecentGroupAdapter;
import com.example.meno.databinding.ActivityGroupMessageBinding;
import com.example.meno.models.Group;
import com.example.meno.models.GroupLastMessageModel;
import com.example.meno.models.GroupMessage;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class GroupMessageActivity extends AppCompatActivity {
    private ActivityGroupMessageBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Group group;
    GroupLastMessageModel lastMessageModel;
    private ArrayList<GroupMessage> groupMessages;
    private GroupChatAdapter groupChatAdapter;
    private String groupMessageId = null;
    private String imageURL = null;
    private ArrayList<HashMap<String, Object>> memberAndImageHashMapList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadGroupDetails();
        setListeners();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        groupMessages = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(
                groupMessages,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(groupChatAdapter);
        database = FirebaseFirestore.getInstance();
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordButton.setListenForRecord(false);
        lastMessageModel = new GroupLastMessageModel();
    }

    private void setListeners() {
        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (binding.inputMessage.getText().length() != 0) {
                    binding.imageCamera.setVisibility(View.GONE);
                    binding.imagePhoto.setVisibility(View.GONE);
                    binding.imageLike.setVisibility(View.GONE);
                    binding.imageShrink.setVisibility(View.VISIBLE);
                    binding.imageSend.setVisibility(View.VISIBLE);
                } else {
                    binding.imageCamera.setVisibility(View.VISIBLE);
                    binding.imagePhoto.setVisibility(View.VISIBLE);
                    binding.imageLike.setVisibility(View.VISIBLE);
                    binding.imageShrink.setVisibility(View.GONE);
                    binding.imageSend.setVisibility(View.GONE);
                }
            }
        });
        binding.imageShrink.setOnClickListener(view -> {
            binding.imageCamera.setVisibility(View.VISIBLE);
            binding.imagePhoto.setVisibility(View.VISIBLE);
            binding.imageShrink.setVisibility(View.GONE);
        });
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageSend.setOnClickListener(v -> sendMessage());
        binding.imagePhoto.setOnClickListener(v -> sendImage());
        binding.imageInfo.setOnClickListener(v -> showInfoGroup());
    }

    private void showInfoGroup() {
        Intent intent = new Intent(getApplicationContext(), GroupInfoActivity.class);
        intent.putExtra(Constants.KEY_COLLECTION_GROUP, group);
        startActivity(intent);
    }

    private void sendImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 438);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Lấy uri từ ảnh, đẩy lên Storage của Firebase, lưu trong thư mục "Image Files"
            Uri imageUri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

            DocumentReference ref = database.collection(Constants.KEY_COLLECTION_GROUP_MESSAGE).document();
            groupMessageId = ref.getId();

            StorageReference imagePath = storageReference.child(groupMessageId + "." + "jpg");
            StorageTask<UploadTask.TaskSnapshot> uploadTask = imagePath.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    showToast(Objects.requireNonNull(task.getException()).getMessage());
                    throw task.getException();
                }
                return imagePath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadURL = task.getResult();
                    imageURL = downloadURL.toString();

                    lastMessageModel.dateObject = new Date();
                    lastMessageModel.message = imageURL;
                    lastMessageModel.senderId = preferenceManager.getString(Constants.KEY_USER_ID);
                    lastMessageModel.type = "image";

                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, lastMessageModel.senderId);
                    message.put(Constants.KEY_MESSAGE, lastMessageModel.message);
                    message.put(Constants.KEY_TYPE, lastMessageModel.type);
                    message.put(Constants.KEY_TIMESTAMP, lastMessageModel.dateObject);

                    database.collection(Constants.KEY_COLLECTION_GROUP)
                            .document(group.id)
                            .collection(Constants.KEY_COLLECTION_LAST_MESSAGE)
                            .document(group.id)
                            .update(message);

                    message.put(Constants.KEY_GROUP_ID, group.id);

                    database.collection(Constants.KEY_COLLECTION_GROUP_MESSAGE)
                            .document(groupMessageId)
                            .set(message)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    showToast("Image sent successfully");
                                } else {
                                    showToast(Objects.requireNonNull(task1.getException()).getMessage());
                                }
                            });
                    binding.inputMessage.setText(null);
                }
            });
        }
    }

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadGroupDetails() {
        group = getIntent().getParcelableExtra(Constants.KEY_COLLECTION_GROUP);
        binding.textName.setText(Objects.requireNonNull(group).name);
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(group.image));
        memberAndImageHashMapList = new ArrayList<>();
        for (User user : group.members) {
            HashMap<String, Object> memberAndImageHashMap = new HashMap<>();
            memberAndImageHashMap.put(Constants.KEY_USER_ID, user.id);
            memberAndImageHashMap.put(Constants.KEY_IMAGE, getBitmapFromEncodedImage(user.image));
            memberAndImageHashMapList.add(memberAndImageHashMap);
        }
    }

    private void sendMessage() {
        lastMessageModel.dateObject = new Date();
        lastMessageModel.message = binding.inputMessage.getText().toString();
        lastMessageModel.senderId = preferenceManager.getString(Constants.KEY_USER_ID);
        lastMessageModel.type = "text";

        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, lastMessageModel.senderId);
        message.put(Constants.KEY_MESSAGE, lastMessageModel.message);
        message.put(Constants.KEY_TYPE, lastMessageModel.type);
        message.put(Constants.KEY_TIMESTAMP, lastMessageModel.dateObject);

        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .collection(Constants.KEY_COLLECTION_LAST_MESSAGE)
                .document(group.id)
                .update(message);

        message.put(Constants.KEY_GROUP_ID, group.id);
        database.collection(Constants.KEY_COLLECTION_GROUP_MESSAGE)
                .add(message);


        group.lastMessageModel = lastMessageModel;
        RecentGroupAdapter.updateLastMessage(group);
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_GROUP_MESSAGE)
                .whereEqualTo(Constants.KEY_GROUP_ID, group.id)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = groupMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    GroupMessage groupMessage = new GroupMessage();
                    groupMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                    for (HashMap<String, Object> hashMap : memberAndImageHashMapList) {
                        if (Objects.equals(hashMap.get(Constants.KEY_USER_ID), groupMessage.senderId)) {
                            groupMessage.imageBitmap = (Bitmap) hashMap.get(Constants.KEY_IMAGE);
                            break;
                        }
                    }
                    groupMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    groupMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                    groupMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    groupMessage.dateTime = getReadableDateTime(groupMessage.dateObject);
                    groupMessages.add(groupMessage);
                }
            }
            groupMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0) {
                groupChatAdapter.notifyDataSetChanged();
            } else {
                groupChatAdapter.notifyItemRangeChanged(groupMessages.size(), groupMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(groupMessages.size() - 1);
                groupChatAdapter.notifyDataSetChanged();
            }

            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM, dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Group changedGroup = intent.getParcelableExtra(Constants.KEY_COLLECTION_GROUP);
            if (changedGroup != null) {
                if (changedGroup.image != null && !changedGroup.image.equals(group.image)) {
                    binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(changedGroup.image));
                    showToast("Changed Image");
                }
                if (changedGroup.name != null && !changedGroup.name.equals(group.name)) {
                    binding.textName.setText(changedGroup.name);
                    showToast("Changed Name");
                }
                group = changedGroup;
                showToast("Changed Group");
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.KEY_COLLECTION_GROUP)
        );
    }

    @Override
    protected void onDestroy() {
        // Unregister when activity finished
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
        super.onDestroy();
    }

}