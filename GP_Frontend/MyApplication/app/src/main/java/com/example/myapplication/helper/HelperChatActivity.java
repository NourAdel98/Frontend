package com.example.myapplication.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.disabled.RequestCommunication;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HelperChatActivity extends AppCompatActivity {

    private LinearLayout layout;
    private RelativeLayout layout_2;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private DatabaseReference reference1;
    private int requestId;
    private String disabledId;
    private String disabledName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layout = (LinearLayout) findViewById(R.id.h_layout1);
        layout_2 = (RelativeLayout) findViewById(R.id.h_layout2);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.h_scrollView);

        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        requestId = preferences.getInt("requestId",0);
        disabledId = preferences.getString("disabledId","");
        disabledName = preferences.getString("disabledName","");

        Firebase.setAndroidContext(this);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        reference1 = FirebaseDatabase.getInstance().getReference().child("chats").child(String.valueOf(requestId));
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String messageText = messageArea.getText().toString();
                if (!messageText.equals("")) {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("type", "helper");
                    map.put("message", messageText);
                    map.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    reference1.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

                Map<String, String> map = (HashMap<String, String>)dataSnapshot.getValue();
                String message = map.get("message");
                String userName = map.get("user");

                if (userName.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    addMessageBox("You:-\n" + message, 1);
                } else {
                    addMessageBox(  disabledName + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type) {

        TextView textView = new TextView(HelperChatActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if (type == 1) {

            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        } else {

            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }

        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, RequestHandleActivity.class));
        return true;
    }
}
