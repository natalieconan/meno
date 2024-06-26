package com.example.meno.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public String name, image, email, token, id, role;

    protected User(Parcel in) {
        name = in.readString();
        image = in.readString();
        email = in.readString();
        token = in.readString();
        id = in.readString();
        role = in.readString();
    }

    public User() {}

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(email);
        parcel.writeString(token);
        parcel.writeString(id);
        parcel.writeString(role);
    }
}
