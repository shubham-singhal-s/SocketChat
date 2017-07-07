package com.bennyhawk.socketchat;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;

public class ChatMainFragment extends Fragment {

    private static final String USER_NAME = "userName";
    private static final int TYPING_TIMER_LENGTH = 600;

    private RecyclerView mMessageList;
    private EditText mMessageInput;
    private ImageButton mSendButton;
    private LinearLayoutManager mLayoutManager;
    private MessageAdapter mMessageAdapter;
    private Realm mMessageStore;
    private Socket mSocket;
    private Calendar calendar = Calendar.getInstance();
    private Toolbar mToolbar;
    private Handler mTypingHandler = new Handler();
    private RealmResults<Message> messageRealmResults;

    private String mUserName;
    private String mLastMessageUserName;
    private int mLastMessageType;
    //private List<Message> mMessageArrayList=new ArrayList<>();
    private int mNumUsers = 0;
    private boolean mTyping = false;

    public ChatMainFragment() {
        // Required empty public constructor
    }

    public static ChatMainFragment newInstance(String userName) {
        ChatMainFragment fragment = new ChatMainFragment();
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserName = getArguments().getString(USER_NAME);
        }

        Realm.init(getContext());
        mMessageStore = Realm.getDefaultInstance();

        messageRealmResults = mMessageStore.where(Message.class).findAll();

        //mMessageArrayList = mMessageStore.copyFromRealm(messageRealmResults);

        try {
            mSocket = IO.socket("http://139.59.35.160:4000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("login", onLogin);

        mSocket.on("message", onMessage);
        mSocket.connect();

        mSocket.emit("new user", "bennyhawk", "shubham21197", new Ack() {
            @Override
            public void call(Object... args) {
                if (args[0].toString().equals("true")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Running!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }






    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("TAG",data.toString());
                    String username;
                    String message;
                    try {
                        username = data.getString("user");
                        message = data.getString("msg");
                    } catch (JSONException e) {
                        Log.e("TAG", e.getMessage());
                        return;
                    }

                    addMessage(username, message,Message.TYPE_MESSAGE_OTHER);
                }
            });
        }
    };





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_chat, container, false);

        mMessageList = (RecyclerView)view.findViewById(R.id.messages);
        mMessageInput = (EditText) view.findViewById(R.id.message_input);
        mSendButton = (ImageButton) view.findViewById(R.id.send_button);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        mLayoutManager = new LinearLayoutManager(getContext());
        mMessageAdapter = new MessageAdapter(getContext());



        mMessageList.setLayoutManager(mLayoutManager);
        mMessageList.setAdapter(mMessageAdapter);





        mSendButton.setOnClickListener(mSendClickListener);
        mMessageInput.addTextChangedListener(textWatcher);

        mToolbar.setTitle("Chat Client");
        mToolbar.setSubtitle("Not connected");
        scrollToBottom();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("login", onLogin);
        mToolbar.setSubtitle(null);
        mLastMessageUserName="";
        mLastMessageType=0;

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!mSocket.connected()) return;

            if (!mTyping) {
                mTyping = true;
                mSocket.emit("typing");
            }

            if (mMessageInput.getText().toString().trim().length() == 0) {
                mSendButton.setVisibility(View.GONE);
            } else {
                mSendButton.setVisibility(View.VISIBLE);
            }

            mTypingHandler.removeCallbacks(onTypingTimeout);
            mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private ImageButton.OnClickListener mSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attemptSend();
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSocket.emit("add user", mUserName);
                    mToolbar.setSubtitle("Connected");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("TAG", "Error connecting");
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e("TAG", e.getMessage());
                        return;
                    }

                    addMessage(username, message,Message.TYPE_MESSAGE_OTHER);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                        mNumUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addMessage("",username + " joined",Message.TYPE_LOG);
                }
            });
        }
    };
    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                        mNumUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addMessage("",username + " left",Message.TYPE_LOG);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e("TAG", e.getMessage());
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };
    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeTyping();
                }
            });
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        mNumUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    mToolbar.setSubtitle("Members Online :- " + String.valueOf(mNumUsers));
                }
            });

        }
    };



    private void addMessage( String username,  String message,  int type) {


        if (type == Message.TYPE_MESSAGE_USER && mLastMessageType == Message.TYPE_MESSAGE_USER){
            type=Message.TYPE_MESSAGE_USER_CONTINUE;
        }



        final String tempusername = username; final String tempmessage=message; final int temptype=type;
        mMessageStore.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                final Message completeMessage = new Message.MessageBuilder(calendar.getTime())
                        .username(tempusername)
                        .message(tempmessage)
                        .type(temptype)
                        .build();
                realm.insert(completeMessage);


            }
        });
        mMessageAdapter.notifyItemInserted(messageRealmResults.size() - 1);
        //mMessageAdapter.notifyDataSetChanged();









/*

Message completeMessage = realm.createObject(Message.class);
        completeMessage.setmRecievedDate(calendar.getTime());
        completeMessage.setmType(type);
        completeMessage.setmUsername(username);
        completeMessage.setmMessage(message);

Message completeMessage = new Message();
        completeMessage.setmRecievedDate(calendar.getTime());
        completeMessage.setmType(type);
        completeMessage.setmUsername(username);
        completeMessage.setmMessage(message);

        Log.d("TAG",username);

        Log.d("TAG",mMessageArrayList.get(mMessageArrayList.size()-1).getmUsername());

        Log.d("TAG",message);

        Log.d("TAG",mMessageArrayList.get(mMessageArrayList.size()-1).getmMessage());


        Log.d("TAG",String.valueOf(mMessageArrayList.get(mMessageArrayList.size()-1).getmType()));


*/



/*
        mMessageStore.beginTransaction();
        mMessageStore.insert(completeMessage);
        mMessageStore.commitTransaction();

*/


        //Log.d("TAG",String.valueOf())



        //scrollToBottom();
    }

    private void addTyping(String username) {
        mToolbar.setSubtitle(username + " is typing...");
    }

    private void removeTyping() {
        mToolbar.setSubtitle("Members Online :- " + String.valueOf(mNumUsers));
    }

    private void attemptSend() {

        if (!mSocket.connected()) return;

        String message = mMessageInput.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            mMessageInput.requestFocus();
            return;
        }

        mMessageInput.setText("");
        addMessage(mUserName, message,Message.TYPE_MESSAGE_USER);

        mSocket.emit("chat message", message, "testroom");
    }

    private void scrollToBottom() {
        mMessageList.scrollToPosition(mMessageList.getAdapter().getItemCount());
    }
}
