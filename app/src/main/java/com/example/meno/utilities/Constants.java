package com.example.meno.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmtoken";

    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTIONS_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String KEY_COLLECTION_GROUP = "group";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_OWNER_ID = "ownerId";
    public static final String KEY_OWNER_NAME = "ownerName";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_GROUP_IMAGE = "groupImage";
    public static final String KEY_ROLE = "role";
    public static final String KEY_COLLECTIONS_MEMBERS = "members";
    public static final String KEY_COLLECTION_LAST_MESSAGE = "lastMessage";
    public static final String KEY_COLLECTION_GROUP_MESSAGE = "groupMessage";

    // Constants for notification
    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String SERVER_KEY = "key=AAAA3IxhYgg:APA91bFbSiyBXIkyJc0RYYsgCLrzh5iga9pfbZ6wBl1VtQ1k9jE2ODSmfMhUVhmkkA4IWIPsZ0JlDgL0bS55-uMLP7xZMz20KKIaZ8lEEqJGK_FlZpuIZtS__bIs9F2jk-S_ZMSHpQB6";

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    SERVER_KEY
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
