package com.example.myapplication.helper;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.ListAdapter;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.disabled.ComplaintDetailsActivity;
import com.example.myapplication.disabled.ComplaintListActivity;
import com.example.myapplication.request.RequestQueueSinglton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HelperHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper__home);

        doTask();
        queue = RequestQueueSinglton.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showRequests();
        if(checkLocationService()){
            requestLocationUpdates();
        }else{
            buildAlertMessageNoGps();
        }
    }

    private void viewProfile() {
        finish();
        Intent intent = new Intent(HelperHomeActivity.this, HelperProfileActivity.class);
        startActivity(intent);
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
        getMenuInflater().inflate(R.menu.helper__home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            finish();
            Intent intent = new Intent(HelperHomeActivity.this, HelperHomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            finish();
            Intent intent = new Intent(HelperHomeActivity.this, SettingsHelperActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            viewProfile();
        }else if (id == R.id.nav_complaint) {

            finish();
            Intent intent = new Intent(HelperHomeActivity.this, ComplaintListActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_contact) {
            finish();
            Intent intent = new Intent(HelperHomeActivity.this, HelperContactUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_aboutUs) {
            finish();
            Intent intent = new Intent(HelperHomeActivity.this, HelperAboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_privacy) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.freeprivacypolicy.com/privacy/view/6528bd31f6f4d50b18a9778ea984d99f"));
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HelperHomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void showRequests() {

        String url = "http://192.168.43.198:8383/show-requests";
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {

                            final JSONArray jsonArray = new JSONArray(response);
                            ArrayList<JSONObject> listItems = getArrayListFromJSONArray(jsonArray);

                            final ListView listV = (ListView) findViewById(R.id.listv);
                            ListAdapter adapter = new ListAdapter(HelperHomeActivity.this, listItems);
                            listV.setAdapter(adapter);


                            listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Object selectedItem = parent.getItemAtPosition(position);
                                    CheckBox checkBox = (CheckBox) selectedItem;

                                    for(int i=0; i<listV.getCount(); i++){
                                        CheckBox box = (CheckBox) parent.getItemAtPosition(i);
                                        if(box != checkBox){
                                            box.setChecked(false);
                                        }
                                    }
                                }
                            });
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                return params;
            }

        };
        queue.add(stringRequest);
    }

    private ArrayList<JSONObject> getArrayListFromJSONArray(JSONArray jsonArray){

        ArrayList<JSONObject> aList=new ArrayList<JSONObject>();
        try {

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    aList.add(jsonArray.getJSONObject(i));
                }
            }
        }
        catch (JSONException je){je.printStackTrace();}
        return  aList;
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
                                            object.put("type", "helper");
                                            buildNotification(String.valueOf(i),
                                                    "your have warning!!, click here", false, true,
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
                        params.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        };

        timer.schedule (hourlyTask, 0l, 1000*5);
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


        Intent intent = new Intent(getApplicationContext(), activity);
        intent.putExtra("object", object.toString());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Integer.parseInt(id), builder.build());
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, final int id) {

                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    boolean checkLocationService(){

        boolean check = false;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            check = true;
        }

        return check;
    }

    private void requestLocationUpdates() {

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        final String path = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations/" + path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d("", "location update " + location);
                        ref.setValue(location);
                    }
                }
            }, null);
        }
    }

}
