package com.michael.dormie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.michael.dormie.model.Chat;

public class ChatService extends IntentService {
    private static final String TAG = "ChatService";
    private static final String ACTION_CREATE_CHAT = "com.michael.dormie.service.action.ACTION_CREATE_CHAT";
    private static final String CHAT_USER_1 = "com.michael.dormie.service.action.CHAT_USER_1";
    private static final String CHAT_USER_2 = "com.michael.dormie.service.action.CHAT_USER_2";
    private static final String CHAT_RECEIVER = "com.michael.dormie.service.action.CHAT_RECEIVER";
    private static final String CHAT_CONTENT = "com.michael.dormie.service.action.CHAT_CONTENT";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ChatService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_CHAT.equals(action)) {
                final ResultReceiver param1 = intent.getParcelableExtra(CHAT_RECEIVER);
                final String param2 = intent.getStringExtra(CHAT_USER_1);
                final String param3 = intent.getStringExtra(CHAT_USER_2);
                final String param4 = intent.getStringExtra(CHAT_CONTENT);
                handleActionUpdateAccount(param1, param2, param3, param4);
            }
        }
    }

    private void handleActionUpdateAccount(ResultReceiver param1, String param2, String param3, String param4) {
    }

    public static void startActionCreateChat(Context context, ResultReceiver param1, String param2, String param3, Chat param4) {
        Intent intent = new Intent(context, ChatService.class);
        intent.setAction(ACTION_CREATE_CHAT);
        intent.putExtra(CHAT_RECEIVER, param1);
        intent.putExtra(CHAT_USER_1, param2);
        intent.putExtra(CHAT_USER_2, param3);
        intent.putExtra(CHAT_CONTENT, param4);
        context.startService(intent);
    }
}
