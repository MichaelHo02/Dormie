package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.databinding.FragmentChatTenantBinding;
import com.michael.dormie.model.Chat;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatTenantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatTenantFragment extends Fragment {
    private final String TAG = "ChatTenantFragment";

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
    private DatabaseReference reference;

    private List<Chat> chats;

    public ChatTenantFragment() {
        // Required empty public constructor
    }

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
        db = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        b.fragmentChatTopBar.setNavigationOnClickListener(this::handleNavigationOnClick);

        try {
            b.fragmentChatRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        } catch (Exception e) {
        }


    }

    private void loadChatList(View view) {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("chats")
                .limitToLast(50);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        };
        query.addChildEventListener(childEventListener);
        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();

    }

    private void handleNavigationOnClick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
        drawerLayout.open();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}