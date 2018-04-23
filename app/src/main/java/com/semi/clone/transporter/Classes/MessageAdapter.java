package com.semi.clone.transporter.Classes;

import android.content.ClipData;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.semi.clone.transporter.Controllers.ClipboardMonitorService;
import com.semi.clone.transporter.Controllers.Messages;
import com.semi.clone.transporter.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {
    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_row,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Message message = messages.get(position);
        final UserViewHolder userViewHolder = (UserViewHolder) holder;
        userViewHolder.text.setText(message.getMessage());
        userViewHolder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardMonitorService.stop();
                Messages.clipboardManager.setPrimaryClip(new ClipData(ClipData.newPlainText(null ,userViewHolder.text.getText())));
                Toast.makeText(v.getContext(),v.getContext().getString(R.string.copied),Toast.LENGTH_SHORT).show();
                ClipboardMonitorService.start();
            }
        });
        userViewHolder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Utils.getDatabase().getReference(Utils.getAuth().getUid()+"/"+message.getId()).getRef().removeValue();
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(v.getContext());
                ab.setMessage(v.getContext().getString(R.string.delete)).setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        UserViewHolder(final View view) {
            super(view);
            text = view.findViewById(R.id.messageText);
        }
    }
}
