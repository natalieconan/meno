package com.example.meno;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meno.activities.ChatActivity;
import com.example.meno.activities.SearchActivity;
import com.example.meno.activities.UsersActivity;
import com.example.meno.adapters.RecentConversationsAdapter;
import com.example.meno.databinding.FragmentChatBinding;
import com.example.meno.listeners.ConversationListener;
import com.example.meno.models.ChatMessage;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatFragment extends Fragment implements ConversationListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    protected String mParam1;
    protected String mParam2;

    private FragmentChatBinding binding;
    private PreferenceManager preferenceManager;
    private ArrayList<ChatMessage> conversations;
    private RecentConversationsAdapter recentConversationsAdapter;
    private FirebaseFirestore database;
    public ChatFragment() {}

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void setListeners() {
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getActivity().getApplicationContext(), UsersActivity.class)));
        binding.inputSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        init();
        setListeners();
        listenConversations();
        return binding.getRoot();
    }

    private void init() {
        binding.imageProfile.setImageBitmap(
                getConversationImage(preferenceManager.getString(Constants.KEY_IMAGE))
        );
        conversations = new ArrayList<>();
        recentConversationsAdapter = new RecentConversationsAdapter(
                conversations,
                preferenceManager.getString(Constants.KEY_USER_ID),
                this,
                getActivity()
        );
        binding.conversationsRecyclerView.setAdapter(recentConversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTIONS_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);

                        for (ChatMessage conversation : conversations) {
                            if (conversation.conversationId.equals(chatMessage.receiverId)) {
                                conversations.remove(conversation);
                                break;
                            }
                        }
                    } else {
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);

                        for (ChatMessage conversation : conversations) {
                            if (conversation.conversationId.equals(chatMessage.senderId)) {
                                conversations.remove(conversation);
                                break;
                            }
                        }
                    }

                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }

            conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    });

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    // Decode ảnh từ String sang Bitmap
    private Bitmap getConversationImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onResume() {
        super.onResume();
        recentConversationsAdapter.notifyDataSetChanged();
    }

}