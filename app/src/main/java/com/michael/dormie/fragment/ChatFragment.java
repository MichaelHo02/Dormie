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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentChatBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
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

        b.refreshLayout.setOnRefreshListener(() -> {
            handleQueryChatRoom();
            b.refreshLayout.setRefreshing(false);
        });
        handleQueryChatRoom();
    }

    private void handleQueryChatRoom() {
        chatRooms = new ArrayList<>();
        userMap = new HashMap<>();

        boolean isFromDetailFragment = ChatFragmentArgs.fromBundle(getArguments()).getIsFromDetailFragment();
        Log.e(TAG, String.valueOf(isFromDetailFragment));
        if (isFromDetailFragment) {
            Place place = ChatFragmentArgs.fromBundle(getArguments()).getPlace();
            String receiverId = place.getAuthorId();
            if (!chatRooms.stream().anyMatch(chatRoom -> chatRoom.getUserIds().contains(receiverId))) {
                handleCreateNewChat(place);
                return;
            }
        }

        queryChatRoom();
    }

    private void handleCreateNewChat(Place place) {
        String uid = UUID.randomUUID().toString();
        ChatRoom chatRoom = new ChatRoom(uid, Arrays.asList(currentUser.getUid(), place.getAuthorId()));
        db.collection(FireBaseDBPath.CHAT_ROOM)
                .document(uid)
                .set(chatRoom, SetOptions.merge())
                .addOnCompleteListener(t -> {
                    queryChatRoom();
                });
    }

    private void queryChatRoom() {
        Query query = db.collection(FireBaseDBPath.CHAT_ROOM)
                .whereArrayContains("userIds", currentUser.getUid());
        query.get().addOnSuccessListener(this::handleQueryChatRoomSuccess);
    }

    private void handleQueryChatRoomSuccess(QuerySnapshot queryDocumentSnapshots) {
        chatRooms = queryDocumentSnapshots.toObjects(ChatRoom.class);
        handleFetchAndRenderChatRooms();
    }

    private void handleFetchAndRenderChatRooms() {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            List<String> userIds = chatRoom.getUserIds();
            userIds.remove(currentUser.getUid());
            String receiverId = userIds.get(0);
            Task<DocumentSnapshot> query = db.collection(FireBaseDBPath.USERS)
                    .document(receiverId).get();
            tasks.add(query);
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
            for (Task<DocumentSnapshot> task : tasks) {
                DocumentSnapshot doc = task.getResult();
                User user = doc.toObject(User.class);
                userMap.put(user.getUid(), user);
            }
            adapter.setData(chatRooms, userMap);
        });
    }
}