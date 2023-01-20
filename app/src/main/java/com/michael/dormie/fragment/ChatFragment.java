package com.michael.dormie.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.R;
import com.michael.dormie.adapter.ChatRoomAdapter;
import com.michael.dormie.databinding.FragmentChatBinding;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    private FragmentChatBinding b;
    private ChatRoomAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<ChatRoom> chatRooms;
    private Map<String, User> userMap;
    private ListenerRegistration roomRegistration;
    private ListenerRegistration userRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentChatBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
        roomRegistration.remove();
        userRegistration.remove();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });

        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        b.recycleView.setLayoutManager(manager);
        adapter = new ChatRoomAdapter();
        b.recycleView.setAdapter(adapter);

        queryChatRoom();
    }

    private void queryChatRoom() {
        Query roomQuery = db.collection(FireBaseDBPath.CHAT_ROOM)
                .whereArrayContains("userIds", currentUser.getUid());
        roomRegistration = roomQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Cannot listen: ", error);
                return;
            }
            if (value == null) return;
            for (DocumentChange dc : value.getDocumentChanges()) {
                ChatRoom chatRoom = dc.getDocument().toObject(ChatRoom.class);
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    if (adapter.getChatRooms().contains(chatRoom)) return;
                    adapter.append(chatRoom);
                } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    Log.e(TAG, "Modify");
                    adapter.modify(chatRoom);
                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                    Log.e(TAG, "Remove");
                    adapter.remove(chatRoom);
                }
            }
            b.recycleView.smoothScrollToPosition(0);
        });

        Query userQuery = db.collection(FireBaseDBPath.USERS);
        userRegistration = userQuery.addSnapshotListener((value, error) -> {
            Log.e(TAG, "User registration");
            if (error != null) {
                Log.w(TAG, "Cannot listen: ", error);
                return;
            }
            if (value == null) return;
            for (DocumentChange dc : value.getDocumentChanges()) {
                User user = dc.getDocument().toObject(User.class);
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    adapter.append(user);
                } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    Log.e(TAG, "Modify");
                    adapter.modify(user);
                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                    Log.e(TAG, "Remove");
                    adapter.remove(user);
                }
            }
        });
    }
}