package com.benezra.nir.poi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatFragment extends Fragment {

    private FirebaseListAdapter<ChatMessage> adapter;
    private ListView listOfMessages;
    private EditText input;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        listOfMessages = (ListView) view.findViewById(R.id.list_of_messages);
        input = (EditText) view.findViewById(R.id.input);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
//                FirebaseDatabase.getInstance()
//                        .getReference().child("events").child("9408a488-7c29-43b6-bb9a-de4e15561579").child("chat")
//                        .push()
//                        .setValue(new ChatMessage(input.getText().toString(),
//                                FirebaseAuth.getInstance()
//                                        .getCurrentUser()
//                                        .getDisplayName())
//                        );

                // Clear the input
                input.setText("");
            }
        });
        displayChatMessages();

        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void displayChatMessages() {

        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("events").child("9408a488-7c29-43b6-bb9a-de4e15561579").child("chat")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}
