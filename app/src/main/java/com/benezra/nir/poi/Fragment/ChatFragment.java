package com.benezra.nir.poi.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.benezra.nir.poi.Objects.ChatMessage;
import com.benezra.nir.poi.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;

public class ChatFragment extends Fragment implements  View.OnClickListener{

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseUser mFirebaseUser;
    private String mEventId;
    private EditText mInput;
    private FloatingActionButton mFloatingActionButton;
    private ListView mListOfMessages;


    public static ChatFragment newInstance(String eventId) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);

        chatFragment.setArguments(args);
        return chatFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_chat, container, false);

        mInput = rootView.findViewById(R.id.input);
        mFloatingActionButton = rootView.findViewById(R.id.fab);
        mListOfMessages = rootView.findViewById(R.id.list_of_messages);
        mFloatingActionButton.setOnClickListener(this);
        displayChatMessages();

        return rootView;

    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mEventId = getArguments().getString(EVENT_ID);

    }


    private void displayChatMessages() {

        adapter = new FirebaseListAdapter<ChatMessage>(getContext(), ChatMessage.class,
                R.layout.chat_message_item, FirebaseDatabase.getInstance().getReference().child("events").child(mEventId).child("chat")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of chat_message_itemssage_item.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                ImageView messageImage = (ImageView)v.findViewById(R.id.message_image);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("hh:mm a",
                        model.getMessageTime()));

                if (!model.getMessageImage().equals(""))
                Picasso.with(getContext()).load(model.getMessageImage()).into(messageImage);
            }
        };

        mListOfMessages.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {

        if(mInput.getText().toString().isEmpty()) return;

        // Read the input field and push a new instance
        // of ChatMessage to the Firebase database
        String image = "";
        if (mFirebaseUser.getPhotoUrl()!=null)
            image = mFirebaseUser.getPhotoUrl().toString();

        FirebaseDatabase.getInstance()
                .getReference().child("events").child(mEventId).child("chat")
                .push()
                .setValue(new ChatMessage(mInput.getText().toString(),
                        mFirebaseUser.getDisplayName(),
                        image)
                );

        // Clear the input
        mInput.setText("");
    }
}
