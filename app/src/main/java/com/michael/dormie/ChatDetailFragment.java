package com.michael.dormie;

import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.adapter.ChatBubbleAdapter;
import com.michael.dormie.databinding.FragmentChatDetailBinding;
import com.michael.dormie.model.ChatBubble;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ChatDetailFragment extends Fragment {
    private static final String TAG = "ChatDetailFragment";

    private FragmentChatDetailBinding b;
    private ChatBubbleAdapter adapter;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private User receiver;
    private ChatRoom chatRoom;
    private Query query;
    private DocumentSnapshot lastDoc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentChatDetailBinding.inflate(inflater, container, false);
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

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        receiver = ChatDetailFragmentArgs.fromBundle(getArguments()).getReceiver();
        chatRoom = ChatDetailFragmentArgs.fromBundle(getArguments()).getChatRoom();

        b.topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(b.getRoot()).popBackStack());
        b.topAppBar.setTitle(receiver.getName());
        b.sendBtn.setOnClickListener(this::handleSendMessage);
        b.refreshLayout.setOnRefreshListener(() -> {
            if (lastDoc == null) return;
            query = db.collection(FireBaseDBPath.CHAT_BUBBLE)
                    .whereEqualTo("chatRoomId", chatRoom.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastDoc)
                    .limit(10);
            handleFetchChat();
        });


        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setReverseLayout(true);
        b.recycleView.setLayoutManager(manager);
        adapter = new ChatBubbleAdapter(receiver);
        b.recycleView.setAdapter(adapter);

        query = db.collection(FireBaseDBPath.CHAT_BUBBLE)
                .whereEqualTo("chatRoomId", chatRoom.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);

        handleFetchChat();
    }

    private void handleFetchChat() {
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                b.refreshLayout.setRefreshing(false);
                return;
            }
            lastDoc = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
            List<ChatBubble> chatBubbles = queryDocumentSnapshots.toObjects(ChatBubble.class);
            adapter.setData(chatBubbles);
            if (b.refreshLayout.isRefreshing()) {
                b.refreshLayout.setRefreshing(false);
                b.recycleView.smoothScrollBy(0, -300);
                return;
            }
            b.recycleView.smoothScrollToPosition(0);
        });
    }

    private void handleSendMessage(View view) {
        String msg = b.chatEditText.getText().toString();
        if (msg.isEmpty()) return;
        b.chatEditText.setText(null);
        msg = msg.trim();
        String uid = UUID.randomUUID().toString();
        ChatBubble chatBubble = new ChatBubble(uid, msg, chatRoom.getUid(), currentUser.getUid(),
                Calendar.getInstance().getTime());
        db.collection(FireBaseDBPath.CHAT_BUBBLE)
                .document(uid)
                .set(chatBubble, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    adapter.addData(chatBubble);
                    b.recycleView.smoothScrollToPosition(0);
                });
    }
}