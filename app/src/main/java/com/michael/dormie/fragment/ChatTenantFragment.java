package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.adapter.ChatAdapter;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.databinding.FragmentChatTenantBinding;
import com.michael.dormie.model.Chat;
import com.michael.dormie.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatTenantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatTenantFragment extends Fragment {

    private final static String TAG = "ChatTenantFragment";

    // collections need to create in Firebase:
    // chats + chatppl: chats for storing chat, chatppl to store the ppl who have chat with

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentChatTenantBinding b;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private ChatAdapter chatAdapter;
//    private DatabaseReference reference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dormie-5ad44-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public ChatTenantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatTenantFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatTenantFragment newInstance(String param1, String param2) {
        ChatTenantFragment fragment = new ChatTenantFragment();
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
        b = FragmentChatTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        List<Chat> chats = new ArrayList<>();

        b.fragmentChatTopBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        b.fragmentChatRv.setHasFixedSize(true);
        b.fragmentChatRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        // handle chats
        // chat list require chat people list and chat content
        getChatList(user.getUid());

    }

    private void getChatList(String userId) {
        List<Chat> chats = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        db.collection("chats")
                .whereArrayContains("userList", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.e(TAG, document.getData().toString());
                                Chat chat = document.toObject(Chat.class);
                                chats.add(chat);
                            }

                            LinearLayoutManager manager = new LinearLayoutManager(requireContext());
                            b.fragmentChatRv.setLayoutManager(manager);
                            chatAdapter = new ChatAdapter(getContext(), chats);
                            b.fragmentChatRv.setAdapter(chatAdapter);
                        } else {
                            System.out.println("Error");
                        }

                    }
                });
    }

    private void handleNavigationOnClick(View view) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}