package com.example.myapplication.disabled;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.IVolleyResponseCallback;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.request.RequestActivity;
import com.example.myapplication.request.RequestQueueSinglton;
import com.example.myapplication.request.ViewHistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DisabledHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String emailFirebase, userType;
    private Button viewProfile;
    private FirebaseAuth firebaseAuth;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home__page);

        queue = RequestQueueSinglton.getInstance(DisabledHomeActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            emailFirebase = firebaseAuth.getCurrentUser().getEmail();
        }

        emergency();
        reserveHelp();
        viewProfile();
        viewRequests();
        doTask();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void emergency() {

        Button btn = (Button) findViewById(R.id.emergency);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                queue = RequestQueueSinglton.getInstance(DisabledHomeActivity.this);
                String url = "http://192.168.43.198:8383/make-request";

                final StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(!response.equals("null")) {

                            Toast.makeText(DisabledHomeActivity.this, "done!", Toast.LENGTH_SHORT).show();
                            try {

                                JSONArray array = new JSONArray(response);
                                String request = array.getString(0);
                                buildNotification(request,"waiting...", true, false,null, null);
                                followNotifications(request);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(DisabledHomeActivity.this, "something wrong, try again!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        Date dateObj = Calendar.getInstance().getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String date = sdf.format(dateObj);
                        String disabledId = firebaseAuth.getCurrentUser().getEmail();

                        params.put("services", "emergency");
                        params.put("payMethod", "cash");
                        params.put("requestState", "false");
                        params.put("date", date);
                        params.put("email", disabledId);

                        return params;
                    }
                };
                queue.add(request);
            }
        });
    }


    private void reserveHelp() {

        Button btn = (Button) findViewById(R.id.reserveMe);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisabledHomeActivity.this, RequestActivity.class));
            }
        });
    }

    private void viewRequests() {

        Button btn = (Button) findViewById(R.id.requestHistory);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisabledHomeActivity.this, ViewHistoryActivity.class));
            }
        });
    }


    public void viewProfile() {

        String url = "http://192.168.43.198:8383/getUserType";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        userType = response;
                        viewProfile = (Button) findViewById(R.id.viewProfile);
                        viewProfile.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (userType.contains("disabled")) {
                                    finish();
                                    Intent intent = new Intent(DisabledHomeActivity.this, DisabledProfileActivity.class);
                                    startActivity(intent);

                                } else if (userType.contains("supporter")) {
                                    finish();
                                    Intent intent = new Intent(DisabledHomeActivity.this, SupporterProfileActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
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
                params.put("email", emailFirebase);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home__page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(DisabledHomeActivity.this, SettingsPrefActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, DisabledHomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, SettingsPrefActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_complaint) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, ComplaintListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, ContactUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_feedback) {

        } else if (id == R.id.nav_aboutUs) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, AboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_privacy) {

            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.freeprivacypolicy.com/privacy/view/6528bd31f6f4d50b18a9778ea984d99f"));
            startActivity(intent);
        } else if (id == R.id.nav_guides) {

            finish();
            Intent intent = new Intent(DisabledHomeActivity.this, GuidesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DisabledHomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void followNotifications(final String requestId){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("notifications");
        reference.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

                checkRequest(requestId, new IVolleyResponseCallback() {
                    @Override
                    public void onSuccess(Object res) {

                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        notifyWithAcceptance(map.get("helper"), requestId);
                    }
                });
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

    private void buildNotification(String id, String content, boolean ongoing, boolean cancel, Class activity, JSONObject object) {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setOngoing(ongoing)
                .setSmallIcon(R.drawable.helper_station)
                .setSound(soundUri)
                .setAutoCancel(cancel);

        if(activity != null){

            Intent intent = new Intent(getApplicationContext(), activity);
            intent.putExtra("object", object.toString());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Integer.parseInt(id), builder.build());
    }

    private void notifyWithAcceptance(String helperId, String requestId) {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(DisabledHomeActivity.this, RequestCommunication.class);
        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.apply();

        editor.putString("requestId", requestId);
        editor.putString("helperId", helperId);
        editor.commit();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("your request is handled now.")
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.helper_station)
                .setSound(soundUri);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Integer.parseInt(requestId), builder.build());
    }

    private void checkRequest(final String requestId, final IVolleyResponseCallback callback){

        String url = "http://192.168.43.198:8383/check-request";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(!response.equals("null")){
                            callback.onSuccess(response);
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
                params.put("email", emailFirebase);
                params.put("requestId", requestId);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    void doTask(){

        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {

            @Override
            public void run () {

                String url = "http://192.168.43.198:8383/check-complaints-replies";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.equals("null")) {

                                    try {
                                        JSONArray array = new JSONArray(response);
                                        JSONObject object;
                                        for (int i = 0; i < array.length(); i++) {

                                            object = array.getJSONObject(i);
                                            object.put("type", "user");
                                            buildNotification(String.valueOf(i),
                                                    "your complaint is replied, click here", false, true,
                                                    ComplaintDetailsActivity.class, object);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
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
                        params.put("email", emailFirebase);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        };

        timer.schedule (hourlyTask, 0l, 1000*5);
    }
}
