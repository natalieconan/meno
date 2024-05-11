package com.example.meno.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meno.R;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

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


        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
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
    }
}