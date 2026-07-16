package com.app.ui;

import com.app.R;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
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

    private TextView textNumber;
    private RecyclerView listCalls;
    private CallLogAdapter adapter;
    private List<CallEntry> entries = new ArrayList<>();
    private static final int PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textNumber = findViewById(R.id.textNumber);
        listCalls = findViewById(R.id.listCalls);
        ImageButton btnCall = findViewById(R.id.btnCall);
        ImageButton btnBackspace = findViewById(R.id.btnBackspace);
        ImageButton btnContacts = findViewById(R.id.btnContacts);

        setupDialer();

        btnCall.setOnClickListener(v -> makeCall());
        btnBackspace.setOnClickListener(v -> {
            String s = textNumber.getText().toString();
            if (!s.isEmpty()) textNumber.setText(s.substring(0, s.length() - 1));
        });
        btnContacts.setOnClickListener(v -> loadContacts());

        adapter = new CallLogAdapter(entries);
        listCalls.setLayoutManager(new LinearLayoutManager(this));
        listCalls.setAdapter(adapter);

        checkPermissions();
    }

    private void setupDialer() {
        int[] ids = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
                     R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnStar, R.id.btnHash};
        String[] labels = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"};
        for (int i = 0; i < ids.length; i++) {
            final String digit = labels[i];
            findViewById(ids[i]).setOnClickListener(v -> textNumber.append(digit));
        }
    }

    private void checkPermissions() {
        List<String> needed = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.CALL_PHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.READ_CONTACTS);
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSIONS);
        } else {
            loadCallLog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS) loadCallLog();
    }

    private void makeCall() {
        String number = textNumber.getText().toString().trim();
        if (number.isEmpty()) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Call permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            TelecomManager tm = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (tm != null) {
                tm.placeCall(Uri.parse("tel:" + number), null);
            } else {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
            }
        } else {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
        }
        loadCallLog();
    }

    private void loadCallLog() {
        entries.clear();
        try {
            Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE},
                    null, null, CallLog.Calls.DATE + " DESC LIMIT 50");
            if (c != null) {
                while (c.moveToNext()) {
                    entries.add(new CallEntry(c.getString(0), c.getInt(1), c.getLong(2)));
                }
                c.close();
            }
        } catch (Exception e) {}
        adapter.notifyDataSetChanged();
    }

    private void loadContacts() {
        entries.clear();
        try {
            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (c != null) {
                while (c.moveToNext()) {
                    entries.add(new CallEntry(c.getString(0) + "\n" + c.getString(1), -1, 0));
                }
                c.close();
            }
        } catch (Exception e) {}
        adapter.notifyDataSetChanged();
    }

    static class CallEntry {
        String number;
        int type;
        long date;
        CallEntry(String n, int t, long d) { number = n; type = t; date = d; }
    }

    class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.VH> {
        List<CallEntry> data;
        CallLogAdapter(List<CallEntry> data) { this.data = data; }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(24, 24, 24, 24);
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(16);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(VH h, int p) {
            CallEntry e = data.get(p);
            h.tv.setText(e.number);
            h.tv.setOnClickListener(v -> {
                String num = e.number.contains("\n") ? e.number.substring(e.number.indexOf("\n") + 1) : e.number;
                textNumber.setText(num);
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(TextView tv) { super(tv); this.tv = tv; }
        }
    }
}
