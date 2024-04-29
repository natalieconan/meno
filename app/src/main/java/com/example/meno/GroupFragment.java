package com.example.meno;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meno.activities.GroupActivity;
import com.example.meno.adapters.RecentGroupAdapter;
import com.example.meno.databinding.FragmentGroupBinding;
import com.example.meno.models.Group;
import com.example.meno.models.GroupLastMessageModel;
import com.example.meno.models.User;
import com.example.meno.utilities.Constants;
import com.example.meno.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class GroupFragment extends Fragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    protected String mParam1;
    protected String mParam2;
    private FragmentGroupBinding binding;
    private PreferenceManager preferenceManager;
    private RecentGroupAdapter recentGroupAdapter;
    private ArrayList<Group> groups;
    private FirebaseFirestore database;
    private CollectionReference groupRef;

    public GroupFragment() {}

    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
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
        if (groups != null) {
            groups.clear();
        }
    }

    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        init();
        setListeners();
        listenConversations();
        return binding.getRoot();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        groups = new ArrayList<>();
        recentGroupAdapter = new RecentGroupAdapter(groups);
        binding.conversationsRecyclerView.setAdapter(recentGroupAdapter);
        database = FirebaseFirestore.getInstance();
        groupRef = database.collection(Constants.KEY_COLLECTION_GROUP);
    }

    private void setListeners() {
        binding.fabNewGroup.setOnClickListener(v ->
                startActivity(new Intent(getActivity().getApplicationContext(), GroupActivity.class)));
    }


    private void listenConversations() {
        groupRef.addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String groupId = documentChange.getDocument().getId();
                    groupRef.document(groupId)
                            .collection(Constants.KEY_COLLECTIONS_MEMBERS)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null
                                        && task.getResult().getDocuments().size() > 0) {
                                    Group group = new Group();
                                    group.members = new ArrayList<>();
                                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                        String memberId = documentSnapshot.getId();
                                        User user = new User();
                                        user.id = memberId;
                                        user.role = documentSnapshot.getString(Constants.KEY_ROLE);

                                        DocumentReference documentReference1 = database.collection(Constants.KEY_COLLECTION_USERS)
                                                .document(memberId);
                                        documentReference1.addSnapshotListener((value12, error12) -> {
                                            if (error12 != null) {
                                                return;
                                            }
                                            if (value12 != null && value12.exists()) {
                                                user.name = value12.getString(Constants.KEY_NAME);
                                                user.image = value12.getString(Constants.KEY_IMAGE);
                                                if (user.role.equals("owner")) {
                                                    group.ownerId = user.id;
                                                    group.ownerName = user.name;
                                                }
                                            }
                                        });
                                        group.members.add(user);
                                        if (memberId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(groupId);
                                            documentReference.addSnapshotListener((value1, error1) -> {
                                                if (error1 != null) {
                                                    return;
                                                }
                                                if (value1 != null && value1.exists()) {
                                                    group.image = value1.getString(Constants.KEY_GROUP_IMAGE);
                                                    group.name = value1.getString(Constants.KEY_GROUP_NAME);
                                                    group.id = value1.getId();
                                                    group.dateObject = value1.getDate(Constants.KEY_TIMESTAMP);

                                                    value1.getReference().collection(Constants.KEY_COLLECTION_LAST_MESSAGE)
                                                            .document(groupId)
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    if (documentSnapshot.exists()) {
                                                                        group.lastMessageModel = documentSnapshot.toObject(GroupLastMessageModel.class);
                                                                        group.lastMessageModel.dateObject = documentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                                                                        group.lastMessageModel.dateTime = getReadableDateTime(group.lastMessageModel.dateObject);
                                                                    }
                                                                    else {
                                                                        HashMap<String, Object> message = new HashMap<>();
                                                                        message.put(Constants.KEY_TIMESTAMP, new Date());
                                                                        group.lastMessageModel = new GroupLastMessageModel();
                                                                        group.lastMessageModel.dateObject = (Date) message.get(Constants.KEY_TIMESTAMP);
                                                                        database.collection(Constants.KEY_COLLECTION_GROUP)
                                                                                .document(group.id)
                                                                                .collection(Constants.KEY_COLLECTION_LAST_MESSAGE)
                                                                                .document(group.id)
                                                                                .set(message);
                                                                    }
                                                                    groups.add(group);
                                                                    groups.sort((obj1, obj2) -> obj2.lastMessageModel.dateObject.compareTo(obj1.lastMessageModel.dateObject));
                                                                    recentGroupAdapter.notifyDataSetChanged();
                                                                }
                                                            });
                                                }
                                                binding.conversationsRecyclerView.smoothScrollToPosition(0);
                                                binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                                                binding.progressBar.setVisibility(View.GONE);
                                            });
                                        }
                                    }
                                }
                            });
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (Group group : groups) {
                        if (group.id.equals(documentChange.getDocument().getId())) {
                            groups.remove(group);
                            break;
                        }
                    }
                }
            }
            Collections.sort(groups, (obj1, obj2) -> obj2.lastMessageModel.dateObject.compareTo(obj1.lastMessageModel.dateObject));
            recentGroupAdapter.notifyDataSetChanged();
        }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM, dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public void onResume() {
        super.onResume();
        recentGroupAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Group changedGroup = intent.getParcelableExtra(Constants.KEY_COLLECTION_GROUP);
            if (changedGroup != null) {
                int i = 0;
                while (groups.size() > 0 && i < groups.size()) {
                    if (groups.get(i).id.equals(changedGroup.id)) {
                        groups.remove(i);
                    } else {
                        i += 1;
                    }
                }
                groups.add(0, changedGroup);
                recentGroupAdapter.notifyDataSetChanged();
                Toast.makeText(context, "Changed Group", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.KEY_COLLECTION_GROUP)
        );
    }

    @Override
    public void onDestroy() {
        // Unregister when activity finished
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
        super.onDestroy();
    }
}