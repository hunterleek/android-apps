package com.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.app.R;
import com.app.adapter.ContactAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<ContactItem> contactList;
    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        adapter = new ContactAdapter(this, contactList);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> startNewMessage());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        }
    }

    private void loadContacts() {
        contactList.clear();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contactList.add(new ContactItem(name, "Tap to message", "10:30 AM"));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void startNewMessage() {
        // Launch new message activity
    }
}
