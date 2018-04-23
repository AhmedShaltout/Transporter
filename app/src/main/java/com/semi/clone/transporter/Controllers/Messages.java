package com.semi.clone.transporter.Controllers;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.semi.clone.transporter.Classes.Message;
import com.semi.clone.transporter.Classes.MessageAdapter;
import com.semi.clone.transporter.Classes.Utils;
import com.semi.clone.transporter.R;

import java.util.ArrayList;
import java.util.List;

public class Messages extends AppCompatActivity {
    public static ClipboardManager clipboardManager;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private int TOTAL_MESSAGES;
    private DatabaseReference myRef;
    private int TIMES_RELOAD;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if(mAdView.getAdListener() == null)
            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mAdView.setVisibility(View.GONE);
                }
            });
        String userID = Utils.getAuth().getUid();
        myRef = Utils.getDatabase().getReference(userID);
        mAdapter = new MessageAdapter(messagesList);
        RecyclerView mMessagesList = findViewById(R.id.messages);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();
                message.setId(messageKey);
                if(!messagesList.isEmpty() && messageKey.compareTo(messagesList.get(messagesList.size()-1).getId())<0)
                    messagesList.add(0,message);
                else
                    messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                if(TIMES_RELOAD == 1)
                    mLinearLayout.scrollToPositionWithOffset(messagesList.size()-1, 0);
                mRefreshLayout.setRefreshing(false);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = -1;
                for (int x = 0; x< messagesList.size(); x++){
                    if(key.equals(messagesList.get(x).getId())){
                        index = x;
                        break;
                    }
                }
                if(index!= -1){
                    messagesList.remove(index);
                    mAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override public void onCancelled(DatabaseError databaseError) { }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TOTAL_MESSAGES = (int)dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getData();

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(messagesList.size() != TOTAL_MESSAGES)
                    getData();
                else
                    mRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void send(View view) {
        EditText message = findViewById(R.id.message);
        sendThis(message.getText().toString());
        message.setText("");
    }
    private void sendThis(String message){
        Message m = new Message(message);
        myRef.push().setValue(m);
    }

    public void logout(View view) {
        Utils.getAuth().signOut();
        startActivity(new Intent(this, User_Login.class));
        finish();
    }

    private void getData() {
        final int TOTAL_ITEMS_TO_LOAD = 20;
        if(TIMES_RELOAD > 0 )
            myRef.orderByKey().limitToLast(TIMES_RELOAD * TOTAL_ITEMS_TO_LOAD)
                    .removeEventListener(childEventListener);
        TIMES_RELOAD++;
        messagesList.clear();
        myRef.orderByKey().limitToLast(TIMES_RELOAD * TOTAL_ITEMS_TO_LOAD).
                addChildEventListener(childEventListener);
    }
}