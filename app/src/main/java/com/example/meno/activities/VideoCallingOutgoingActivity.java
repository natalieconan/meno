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
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meno.api.HttpClient;
import com.example.meno.api.HttpService;
import com.example.meno.databinding.ActivityVideoCallingOutgoingBinding;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoCallingOutgoingActivity extends AppCompatActivity {
    ActivityVideoCallingOutgoingBinding binding;
    private User receivedUser;
    private PreferenceManager preferenceManager;
    // meetingRoom ID
    String meetingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallingOutgoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadReceiverDetails();
        setListeners();
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        if (meetingType != null && receivedUser != null) {
            initiateMeeting(meetingType, receivedUser.token);
        }
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setListeners() {
        binding.fabDecline.setOnClickListener(v -> {
            if (receivedUser != null) {
                cancelInvitation(receivedUser.token);
            }
        });
    }

    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                    UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

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
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        showToast("Invitation send successfully");
                    } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        showToast("Invitation cancelled");
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

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        } catch (Exception e) {
            showToast(e.getMessage());
            finish();
        }
    }
    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (Objects.equals(type, Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                showToast("Invitation Accepted");
                try {
                    URL serverURL = new URL("https://meet.jit.si");
                    JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setFeatureFlag("welcomepage.enabled", false)
                            .setRoom(meetingRoom)
                            .build();
                    JitsiMeetActivity.launch(VideoCallingOutgoingActivity.this, conferenceOptions);
                    finish();
                } catch (Exception e) {
                    showToast(e.getMessage());
                    finish();
                }
            } else if (Objects.equals(type, Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                showToast("Invitation Rejected");
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

    private void loadReceiverDetails() {
        receivedUser = getIntent().getParcelableExtra(Constants.KEY_USER);
        binding.textName.setText(Objects.requireNonNull(receivedUser).name);
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedImage(receivedUser.image));
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
        Toast.makeText(VideoCallingOutgoingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}