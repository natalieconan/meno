package com.example.meno.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class GroupLastMessage implements Parcelable {
    public String senderId, message, type, dateTime;
    public Date dateObject;
    public GroupLastMessage() {}

    protected GroupLastMessage(Parcel in) {
        senderId = in.readString();
        message = in.readString();
        type = in.readString();
        dateTime = in.readString();
        dateObject = new Date(in.readLong());
    }

    public static final Creator<GroupLastMessage> CREATOR = new Creator<GroupLastMessage>() {
        @Override
        public GroupLastMessage createFromParcel(Parcel in) {
            return new GroupLastMessage(in);
        }

        @Override
        public GroupLastMessage[] newArray(int size) {
            return new GroupLastMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(senderId);
        parcel.writeString(message);
        parcel.writeString(type);
        parcel.writeString(dateTime);
        parcel.writeLong(dateObject.getTime());
    }
}