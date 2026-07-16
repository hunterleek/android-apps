package com.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.R;
import com.app.ui.ContactItem;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private List<ContactItem> contactList;

    public ContactAdapter(Context context, List<ContactItem> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactItem contact = contactList.get(position);
        holder.nameText.setText(contact.getName());
        holder.lastMessageText.setText(contact.getLastMessage());
        holder.timeText.setText(contact.getTime());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, lastMessageText, timeText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textName);
            lastMessageText = itemView.findViewById(R.id.textLastMessage);
            timeText = itemView.findViewById(R.id.textTime);
        }
    }
}
