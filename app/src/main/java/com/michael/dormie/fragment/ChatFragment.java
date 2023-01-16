package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.michael.dormie.R;
import com.michael.dormie.model.ChatMessage;

import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    public ChatFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso);

        // Display current messages
        displayChatMessages();

        // Using FAB to send message
        EditText input = (EditText) view.findViewById(R.id.input);
        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Read the input field and push a new instance of Chat to the Firebase database
                FirebaseDatabase database = FirebaseDatabase.getInstance("https://dormie-5ad44-default-rtdb.asia-southeast1.firebasedatabase.app");
                DatabaseReference myRef = database.getReference("server/saved_chat");
                myRef.push().setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                // Clear current input
                input.setText("");
            }
        });
        return view;
    }

    private void displayChatMessages() {

        // Currently using temporary database for chat
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://dormie-5ad44-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("server/saved_chat/");

        // Use FirebaseListOptions to obtain database
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(myRef, ChatMessage.class)
                .setLayout(R.layout.message)
                .setLifecycleOwner(this)
                .build();

        // User Firebase List Adapter to populate chat data
        FirebaseListAdapter<ChatMessage> adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Obtain references for message, time, and user
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);
                // Change message text, user, and time
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
            }
        };
        ListView listOfMessages = (ListView) view.findViewById(R.id.list_of_messages);
        listOfMessages.setAdapter(adapter);


    };

    private FirebaseListAdapter<ChatMessage> adapter;
}