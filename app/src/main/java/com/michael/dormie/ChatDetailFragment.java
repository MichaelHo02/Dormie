package com.michael.dormie;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.adapter.ChatBubbleAdapter;
import com.michael.dormie.databinding.FragmentChatDetailBinding;
import com.michael.dormie.model.ChatBubble;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ChatDetailFragment extends Fragment {
    private static final String TAG = "ChatDetailFragment";

    private FragmentChatDetailBinding b;
    private ChatBubbleAdapter adapter;
    private FirebaseUser currentUser;
    private User receiver;
    private FirebaseFirestore db;
    private ChatRoom chatRoom;
    private Query topQuery;
    private Query bottomQuery;
    private DocumentSnapshot topDoc;
    private ListenerRegistration registration;

    int limit = 5;
    final int increaseSize = 5;
    private boolean isSendBtnClick = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentChatDetailBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
        registration.remove();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ChatBubble chatBubble = adapter.getLatestChatBubble();
        if (chatBubble == null) return;
        chatRoom.setLatestMessage(chatBubble.getContent());
        if (chatBubble.getPersonId().equals(currentUser.getUid())) {
            chatRoom.setLatestMessageSender(currentUser.getDisplayName());
        } else {
            chatRoom.setLatestMessageSender(receiver.getName());
        }
        chatRoom.setTimeStamp(chatBubble.getTimestamp());
        chatRoom.setUserIds(Arrays.asList(currentUser.getUid(), receiver.getUid()));
        db.collection(FireBaseDBPath.CHAT_ROOM)
                .document(chatRoom.getUid())
                .set(chatRoom, SetOptions.merge());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        receiver = ChatDetailFragmentArgs.fromBundle(getArguments()).getReceiver();
        chatRoom = ChatDetailFragmentArgs.fromBundle(getArguments()).getChatRoom();

        b.topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(b.getRoot()).popBackStack());
        b.topAppBar.setTitle(receiver.getName());
        b.sendBtn.setOnClickListener(this::handleSendMessage);
        b.refreshLayout.setOnRefreshListener(() -> {
            if (topDoc == null) return;
            topQuery = db.collection(FireBaseDBPath.CHAT_BUBBLE)
                    .whereEqualTo("chatRoomId", chatRoom.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(topDoc);
            handleFetchChat();
        });

        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setReverseLayout(true);
        b.recycleView.setLayoutManager(manager);
        adapter = new ChatBubbleAdapter(receiver);
        b.recycleView.setAdapter(adapter);

        bottomQuery = db.collection(FireBaseDBPath.CHAT_BUBBLE)
                .whereEqualTo("chatRoomId", chatRoom.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit);

        registration = bottomQuery.addSnapshotListener(this::handleSnapshotListener);
    }

    private void handleSnapshotListener(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "Cannot listen: ", e);
            return;
        }
        if (queryDocumentSnapshots == null) return;
        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
            if (dc.getType() == DocumentChange.Type.ADDED) {
                ChatBubble chatBubble = dc.getDocument().toObject(ChatBubble.class);
                Log.e(TAG, String.valueOf(adapter.getChatBubbles().contains(chatBubble)));
                if (adapter.getChatBubbles().contains(chatBubble)) return;
                Log.e(TAG, dc.getDocument().toString());
                if (isSendBtnClick) {
                    adapter.appendBottom(chatBubble);
                } else {
                    adapter.appendTop(chatBubble);
                    topDoc = dc.getDocument();
                }
                b.recycleView.smoothScrollToPosition(0);
            } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                Log.e(TAG, "Modify");
            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                Log.e(TAG, "Remove");
            }
        }
        if (!isSendBtnClick) isSendBtnClick = true;
    }

    private void handleFetchChat() {
        topQuery.limit(increaseSize).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                b.refreshLayout.setRefreshing(false);
                return;
            }
            topDoc = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
            List<ChatBubble> chatBubbles = queryDocumentSnapshots.toObjects(ChatBubble.class);
            for (ChatBubble chatBubble : chatBubbles) {
                if (adapter.getChatBubbles().contains(chatBubble)) continue;
                adapter.appendTop(chatBubble);
            }
            b.refreshLayout.setRefreshing(false);
            b.recycleView.smoothScrollBy(0, -300);
        });
    }

    private void handleSendMessage(View view) {
        isSendBtnClick = true;
        String msg = b.chatEditText.getText().toString();
        if (msg.isEmpty()) return;
        b.chatEditText.setText(null);
        msg = msg.trim();
        String uid = UUID.randomUUID().toString();
        Log.e(TAG, "Current User: " + currentUser.getUid() + " " + currentUser.getDisplayName());
        ChatBubble chatBubble = new ChatBubble(uid, msg, chatRoom.getUid(), currentUser.getUid(),
                Calendar.getInstance().getTime());
        db.collection(FireBaseDBPath.CHAT_BUBBLE)
                .add(chatBubble);

        if (adapter.getItemCount() >= limit) {
            limit += increaseSize;
            Log.e(TAG, String.valueOf(limit));
            bottomQuery = bottomQuery.limit(limit);
            registration.remove();
            registration = bottomQuery.addSnapshotListener(this::handleSnapshotListener);
        }
    }
}