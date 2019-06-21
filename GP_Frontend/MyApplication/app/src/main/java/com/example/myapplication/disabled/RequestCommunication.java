package com.example.myapplication.disabled;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.ChatActivity;
import com.example.myapplication.R;
import com.example.myapplication.request.RequestQueueSinglton;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestCommunication extends AppCompatActivity implements OnMapReadyCallback {

    private RequestQueue queue;
    private static final String TAG = RequestCommunication.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;
    private String helperId;
    private String helperName;
    private String helperNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_communication);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = RequestQueueSinglton.getInstance(this);

        final SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        helperId = preferences.getString("helperId","");

        getHelperInfo(helperId);
        notifyWithNewMessage();

        Button chat = (Button) findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                editor.putString("helperName", helperName);
                editor.commit();

                Intent intent = new Intent(RequestCommunication.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        Button call = (Button) findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Uri u = Uri.parse("tel:" + helperNumber);
                    Intent i = new Intent(Intent.ACTION_DIAL, u);
                    startActivity(i);

                } catch (SecurityException e) {
                    Toast.makeText(RequestCommunication.this, e.getMessage(), Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMaxZoomPreference(17);
        subscribeToUpdates();
    }


    private void subscribeToUpdates() {

        final String temp = helperId.replace(".", ",");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("locations");
        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                if(dataSnapshot.getKey().equals(temp)) {
                    setMarker(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                if(dataSnapshot.getKey().equals(temp)) {
                    setMarker(dataSnapshot);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // snapshot contains data from firebase db location
    private void setMarker(DataSnapshot dataSnapshot) {

        String key = dataSnapshot.getKey();
        System.out.println(dataSnapshot);
        // get values from snapshots in from of hashmap
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
        } else {
            mMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

    private void getHelperInfo(final String email){

        String url = "http://192.168.43.198:8383/get-helper-info";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            final JSONObject object = new JSONObject(response);
                            ((TextView) findViewById(R.id.helperName)).setText(object.getString("fname") + " " +
                                    object.getString("lname"));
                            ((TextView) findViewById(R.id.helperRate)).setText(String.valueOf(object.getDouble("rate")));

                            helperName = object.getString("fname");
                            helperNumber = object.getString("phoneNumber");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }

    private void notifyWithNewMessage(){

        final String requestId = getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("requestId","");
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("chats").child(requestId);

        reference1.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

                Map<String, String> map = (HashMap<String, String>)dataSnapshot.getValue();
                if(map.get("type").equals("helper")){

                    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Intent intent = new Intent(RequestCommunication.this, ChatActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addNextIntent(intent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText("your have new message from " + helperName)
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
