package com.example.myapplication.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.ChatActivity;
import com.example.myapplication.R;
import com.example.myapplication.disabled.RequestCommunication;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class RequestHandleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_handle);
        makeCall();
        sendMessage();
        notifyWithNewMessage();
    }

    private void makeCall(){

        Button btn = findViewById(R.id.make_call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri u = Uri.parse("tel:" + getSharedPreferences("prefs",
                        Context.MODE_PRIVATE).getString("disabledNum",""));

                Intent i = new Intent(Intent.ACTION_DIAL, u);
                startActivity(i);
            }
        });
    }

    private void sendMessage(){

        Button btn = findViewById(R.id.open_chat);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(RequestHandleActivity.this, HelperChatActivity.class));
            }
        });
    }

    private void notifyWithNewMessage(){

        final String requestId = getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("requestId","");
        final String disabledName = getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("disabledName","");
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("chats").child(requestId);

        reference1.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

                Map<String, String> map = (HashMap<String, String>)dataSnapshot.getValue();
                if(map.get("type").equals("user")){

                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Intent intent = new Intent(RequestHandleActivity.this, HelperChatActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addNextIntent(intent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("your have new message from " + disabledName)
                            .setAutoCancel(true)
                            .setContentIntent(resultPendingIntent)
                            .setSmallIcon(R.drawable.helper_station)
                            .setSound(soundUri);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(1, builder.build());
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
}
