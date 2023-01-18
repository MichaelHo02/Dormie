package com.michael.dormie;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.adapter.ChatBubbleAdapter;
import com.michael.dormie.databinding.FragmentChatDetailBinding;
import com.michael.dormie.model.ChatBubble;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.ValidationUtil;

import java.util.Calendar;
import java.util.UUID;

public class ChatDetailFragment extends Fragment {
    private static final String TAG = "ChatDetailFragment";

    private FragmentChatDetailBinding b;
    private ChatBubbleAdapter adapter;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private User receiver;
    private ChatRoom chatRoom;

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
        b.sendBtn.setOnClickListener(this::handleSendMessage);


        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setReverseLayout(true);
        b.recycleView.setLayoutManager(manager);
        adapter = new ChatBubbleAdapter(receiver);
        b.recycleView.setAdapter(adapter);

    }

    private void handleSendMessage(View view) {
        String msg = b.chatEditText.getText().toString();
        if (msg.isEmpty()) return;
        msg = msg.trim();
        String uid = UUID.randomUUID().toString();
        ChatBubble chatBubble = new ChatBubble(uid, msg, chatRoom.getUid(), currentUser.getUid(),
                Calendar.getInstance().getTime());
        db.collection(FireBaseDBPath.CHAT_BUBBLE)
                .document(uid)
                .set(chatBubble, SetOptions.merge())
                .addOnCompleteListener(task -> adapter.addData(chatBubble));
    }
}