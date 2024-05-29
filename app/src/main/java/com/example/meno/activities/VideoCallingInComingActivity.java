package com.example.meno.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meno.api.HttpClient;
import com.example.meno.api.HttpService;
import com.example.meno.databinding.ActivityVideoCallingInComingBinding;
import com.example.meno.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoCallingInComingActivity extends BaseActivity {
    ActivityVideoCallingInComingBinding binding;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallingInComingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadSenderDetails();
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        // accept the calling
        binding.fabAccept.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        // decline
        binding.fabDecline.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));
    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_CAMERA_STATE, getIntent().getStringExtra(Constants.REMOTE_CAMERA_STATE));
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        } catch (Exception e) {
            showToast(e.getMessage());
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        HttpClient.getClient().create(HttpService.class).sendMessage(
                Constants.getRemoteMsgHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        showToast("Invitation Accepted");

                        try {
                            String userID = getIntent().getStringExtra(Constants.KEY_USER_ID);
                            String userName = getIntent().getStringExtra(Constants.KEY_NAME);
                            Intent intentAcceptCalling = new Intent(VideoCallingInComingActivity.this, ConferenceActivity.class);
                            intentAcceptCalling.putExtra(Constants.REMOTE_CAMERA_STATE,
                                    getIntent().getStringExtra(Constants.REMOTE_CAMERA_STATE));
                            intentAcceptCalling.putExtra(Constants.KEY_USER_ID, userID);
                            intentAcceptCalling.putExtra(Constants.KEY_NAME, userName);
                            startActivity(intentAcceptCalling);
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            finish();
                        }

                    } else {
                        showToast("Invitation Rejected");
                        finish();
                    }
                } else {
                    showToast(response.message());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
                finish();
            }
        });
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (Objects.equals(type, Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                showToast("Invitation Cancelled");
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }

    private void loadSenderDetails() {
        binding.textName.setText(getIntent().getStringExtra(Constants.KEY_NAME));
        String senderId = getIntent().getStringExtra(Constants.KEY_USER_ID);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(Objects.requireNonNull(senderId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(documentSnapshot.getString(Constants.KEY_IMAGE)));
                    }
                });
    }

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(VideoCallingInComingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}