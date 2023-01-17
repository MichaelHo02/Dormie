package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.michael.dormie.R;
import com.michael.dormie.adapter.MessageAdapter;
import com.michael.dormie.databinding.FragmentMessageTenantBinding;
import com.michael.dormie.model.Message;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageTenantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageTenantFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentMessageTenantBinding b;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference senderReference, receiverReference;

    private String receiverId;
    private String senderChat, receiverChat;
    private MessageAdapter messageAdapter;


    public MessageTenantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatContentTenantFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageTenantFragment newInstance(String param1, String param2) {
        MessageTenantFragment fragment = new MessageTenantFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentMessageTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        receiverId = getArguments().getString("receiverId");
        senderChat = user.getUid() + receiverId;
        receiverChat = receiverId + user.getUid();

        messageAdapter = new MessageAdapter(requireContext());
        b.chatRcv.setAdapter(messageAdapter);
        b.chatRcv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        senderReference = FirebaseDatabase.getInstance().getReference("chats").child( senderChat);
        receiverReference = FirebaseDatabase.getInstance().getReference("chats").child( receiverChat);

        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageAdapter.clear();
                for (DataSnapshot data :  snapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messageAdapter.add(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        b.sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = b.chatMsg.getText().toString();
                if (msg.trim().length() > 0) {
                    sendMessage(msg);
                }
            }
        });
    }

    private void sendMessage(String msg) {
        String msgId = UUID.randomUUID().toString();
        Message newMsg = new Message(msgId, msg, user.getUid());
        messageAdapter.add(newMsg);
        senderReference
                .child(msgId)
                .setValue(newMsg);

        receiverReference
                .child(msgId)
                .setValue(newMsg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}