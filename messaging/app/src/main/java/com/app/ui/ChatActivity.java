package com.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.R;
import com.app.adapter.MessageAdapter;
import com.app.model.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private MessageAdapter adapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(adapter);

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            messageList.add(new Message(text, true, System.currentTimeMillis()));
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            editTextMessage.setText("");
        }
    }
}
