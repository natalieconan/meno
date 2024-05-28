package com.example.meno.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meno.MainActivity;
import com.example.meno.R;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;
import com.zegocloud.uikit.prebuilt.videoconference.config.ZegoLeaveConfirmDialogInfo;

import java.util.Objects;

public class ConferenceActivity extends AppCompatActivity {
    protected PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        addFragment();
    }

    public void addFragment() {
        long appID = Constants.ZEGOCLOUD_APP_ID;
        String appSign = Constants.ZEGOCLOUD_APP_SIGN;

        String conferenceID = "conferenceID";
        String userID = getIntent().getStringExtra(Constants.KEY_USER_ID);
        String userName = getIntent().getStringExtra(Constants.KEY_NAME);
        String turnOnCamera = getIntent().getStringExtra(Constants.REMOTE_CAMERA_STATE);


        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();

        config.turnOnCameraWhenJoining = Objects.equals(turnOnCamera, Constants.REMOTE_CAMERA_OPEN);

        // confirm out of conversation
        ZegoLeaveConfirmDialogInfo confirmDialogInfo = new ZegoLeaveConfirmDialogInfo();
        confirmDialogInfo.title = "Leave confirm";
        confirmDialogInfo.message = "Do you want to leave?";
        confirmDialogInfo.cancelButtonName = "Cancel";
        confirmDialogInfo.confirmButtonName = "Confirm";
        config.leaveConfirmDialogInfo = confirmDialogInfo;

        if (userName != null && userID != null) {
            ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
                    appID,
                    appSign,
                    userID,
                    userName,
                    conferenceID,
                    config
            );

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();
        } else {
            showToast();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showToast() {
        Toast.makeText(ConferenceActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
    }
}