package com.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.R;
import com.app.model.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private SimpleDateFormat timeFormat;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.isSent()) {
            holder.containerSent.setVisibility(View.VISIBLE);
            holder.containerReceived.setVisibility(View.GONE);
            holder.messageSent.setText(message.getText());
            holder.timeSent.setText(timeFormat.format(new Date(message.getTimestamp())));
        } else {
            holder.containerSent.setVisibility(View.GONE);
            holder.containerReceived.setVisibility(View.VISIBLE);
            holder.messageReceived.setText(message.getText());
            holder.timeReceived.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerSent, containerReceived;
        TextView messageSent, timeSent, messageReceived, timeReceived;

        ViewHolder(View itemView) {
            super(itemView);
            containerSent = itemView.findViewById(R.id.containerSent);
            containerReceived = itemView.findViewById(R.id.containerReceived);
            messageSent = itemView.findViewById(R.id.textMessageSent);
            timeSent = itemView.findViewById(R.id.textTimeSent);
            messageReceived = itemView.findViewById(R.id.textMessageReceived);
            timeReceived = itemView.findViewById(R.id.textTimeReceived);
        }
    }
}
