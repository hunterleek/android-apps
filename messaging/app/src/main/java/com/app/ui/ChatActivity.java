package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView textTitle;
    private RecyclerView recycler;
    private EditText editText;
    private ImageButton btnSend;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private String number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        textTitle = findViewById(R.id.textTitle);
        recycler = findViewById(R.id.recyclerViewMessages);
        editText = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.buttonSend);

        String name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");
        if (name == null) name = number != null ? number : "New Message";
        textTitle.setText(name);

        adapter = new MessageAdapter(messages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            return;
        }
        try {
            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(number, null, text, null, null);
            messages.add(new Message(text, true));
            adapter.notifyItemInserted(messages.size() - 1);
            recycler.scrollToPosition(messages.size() - 1);
            editText.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    static class Message {
        String text;
        boolean sent;
        Message(String t, boolean s) { text = t; sent = s; }
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {
        List<Message> data;
        MessageAdapter(List<Message> data) { this.data = data; }
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(24, 16, 24, 16);
            tv.setTextSize(16);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(VH h, int p) {
            Message m = data.get(p);
            h.tv.setText(m.text);
            h.tv.setTextColor(0xFFFFFFFF);
            h.tv.setBackgroundResource(m.sent ? R.drawable.bg_message_sent : R.drawable.bg_message_received);
            h.tv.setGravity(m.sent ? android.view.Gravity.END : android.view.Gravity.START);
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(TextView tv) { super(tv); this.tv = tv; }
        }
    }
}
