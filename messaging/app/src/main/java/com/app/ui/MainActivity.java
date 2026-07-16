package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION = 100;
    private RecyclerView recycler;
    private ContactAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyclerView);
        adapter = new ContactAdapter(contacts);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.fab).setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, PERMISSION);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION) loadContacts();
    }

    private void loadContacts() {
        contacts.clear();
        try {
            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (c != null) {
                while (c.moveToNext()) {
                    contacts.add(new Contact(c.getString(0), c.getString(1)));
                }
                c.close();
            }
        } catch (Exception e) {}
        adapter.notifyDataSetChanged();
    }

    static class Contact {
        String name, number;
        Contact(String n, String num) { name = n; number = num; }
    }

    class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {
        List<Contact> data;
        ContactAdapter(List<Contact> data) { this.data = data; }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(24, 24, 24, 24);
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(18);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(VH h, int p) {
            Contact c = data.get(p);
            h.tv.setText(c.name + "\n" + c.number);
            h.tv.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                i.putExtra("number", c.number);
                i.putExtra("name", c.name);
                startActivity(i);
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(TextView tv) { super(tv); this.tv = tv; }
        }
    }
}
