package com.example.myapplication.request;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.IVolleyResponseCallback;
import com.example.myapplication.Pickers.DatePickerFragment;
import com.example.myapplication.Pickers.TimePickerFragment;
import com.example.myapplication.R;
import com.example.myapplication.disabled.DisabledHomeActivity;
import com.fasterxml.jackson.databind.util.JSONPObject;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class RequestActivity extends AppCompatActivity {

    private final static int RADIUS_OF_THE_EARTH = 6373;
    private FirebaseAuth firebaseAuth;
    private TextView editDate;
    private TextView editTime;
    private RequestQueue queue;
    private List<String> selectedServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        firebaseAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editDate = findViewById(R.id.editDate);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        editDate.setText(format.format(Calendar.getInstance().getTime()));

        editTime = findViewById(R.id.editTime);
        format = new SimpleDateFormat("HH:mm");
        editTime.setText(format.format(new Date()));

        setDateAndTime();
        makeRequest();

        TextView textView = (TextView) findViewById(R.id.request);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });
    }

    private void getAllServices(final IVolleyResponseCallback callback) {

        String url = "http://192.168.43.198:8383/get-all-services";
        queue = RequestQueueSinglton.getInstance(RequestActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray services = new JSONArray(response);
                    callback.onSuccess(services);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println(error);
            }
        });
        queue.add(request);
    }

    private void setDialog() {

        selectedServices = new ArrayList<>();
        getAllServices(new IVolleyResponseCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray res) {

                final CharSequence [] services = new CharSequence[res.length()];
                for (int i = 0; i < res.length(); i++) {

                    try {
                        services[i] = (String) res.getJSONObject(i).get("description");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                final String[] string = {""};

                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setTitle("choose services");

                builder.setMultiChoiceItems(services, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if(isChecked) {
                            string[0] += services[which] + ",";
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((TextView)findViewById(R.id.request)).setText(string[0].substring(0, string[0].length()-1));
                        selectedServices.addAll(Arrays.asList(string[0].split(",")));
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void setDateAndTime() {

        editDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "Set Date");
            }
        });

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment fragment = new TimePickerFragment();
                fragment.show(getSupportFragmentManager(), "Set Time");
            }
        });
    }

    private void makeRequest() {

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String service = ((Spinner) findViewById(R.id.request)).getSelectedItem().toString();
                String date = editDate.getText().toString();
                String time = editTime.getText().toString();

                if(checkLocationService()) {

                    doRequest(RequestActivity.this, selectedServices, date, time, firebaseAuth);
                    startActivity(new Intent(RequestActivity.this, DisabledHomeActivity.class));
                }else {
                    buildAlertMessageNoGps();
                }
            }
        });
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

    public void doRequest(final Context context, final List<String> services, final String date, final String time,
                   final FirebaseAuth firebaseAuth) {

        saveLocation(context, firebaseAuth);
        String url =  "http://192.168.43.198:8383/make-request";

        queue = RequestQueueSinglton.getInstance(context);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    JSONArray array = new JSONArray(response);
                    List<JSONObject> helpers = new ArrayList<>();

                    int requestId = array.getJSONObject(0).getInt("requestId");
                    for(int i=1; i<array.length(); i++){
                        System.out.println("kdddddddddddddddddddddddddddddddddddddddddaaaaaaa " + array.getJSONObject(i));
                        helpers.add(array.getJSONObject(i));
                    }

                    int numberOfHelpers = 7;
                    if(helpers.size() < 7)
                        numberOfHelpers = helpers.size();

                    propagateRequest(requestId, helpers, numberOfHelpers);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println(error);
            }
        }) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();

                Date dateObj = new Date();
                String[] dateDetails = date.split("/");
                String[] timeDetails = time.split(":");

                // set day
                dateObj.setDate(Integer.parseInt(dateDetails[0]));

                // month between 0-11 so MAY is in 4th index
                dateObj.setMonth(Integer.parseInt(dateDetails[1]) - 1);

                // year after 1900 so need to minus 1900 from the value that I entered
                dateObj.setYear(Integer.parseInt(dateDetails[2]) - 1900);

                // to set time
                dateObj.setHours(Integer.parseInt(timeDetails[0]));
                dateObj.setMinutes(Integer.parseInt(timeDetails[1]));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String date = sdf.format(dateObj);
                String userId = firebaseAuth.getCurrentUser().getEmail();

                String services = "";
                for(String str: selectedServices)
                    services += str + '_';
                params.put("servicesList", services.substring(0,services.length()-1));
                params.put("date", date);
                params.put("payMethod", "cash");
                params.put("requestState", "false");
                params.put("email", userId);

                return params;
            }
        };

        queue.add(request);
    }

    private void propagateRequest(int requestId, List<JSONObject> helpers, int nearestHelper){

        getNearestHelpers(new IVolleyResponseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> res) {

                String url = "http://192.168.43.198:8383/request-nearest-helpers";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.equals("true")){
                            // show alert
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error.getMessage());
                            }
                        });
            }
        }, nearestHelper, helpers);
    }

    private void getHelpersLocations(final IVolleyResponseCallback callback, final List<JSONObject> helpers) {

        String url = "https://gp-project-82613.firebaseio.com/locations/helpers.json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

                    String email;
                    JSONArray array = new JSONArray(response);
                    for(int i=0; i<array.length(); i++){

                        System.out.println("yanaaaaaaaaaaasyahooo " + array.getJSONObject(i));
                        email = array.getJSONObject(i).getString("email").replace(",",".");
                        for(int j=0; j<helpers.size(); j++){

                            if(helpers.get(j).getString("email").equals(email)){
                                helpers.get(j).put("location", array.getJSONObject(i));
                                break;
                            }
                        }
                    }
                }catch (JSONException e) {
                        e.printStackTrace();
                }
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError);
            }
        });

        queue = RequestQueueSinglton.getInstance(this);
        queue.add(request);
    }

    private void getCurrentUserLocation(final IVolleyResponseCallback callback){

        String path = firebaseAuth.getCurrentUser().getEmail().replace(".", ",");
        String url = "https://gp-project-82613.firebaseio.com/locations/users/" + path + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {

                try {

                    JSONObject object = new JSONObject(response);
                    System.out.println("kkkkkkkkkkkk " + object);
                    callback.onSuccess(object);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError);
            }
        });

        queue = RequestQueueSinglton.getInstance(this);
        queue.add(request);
    }

    private void getNearestHelpers(final IVolleyResponseCallback callback, final int numberOfHelpers, final List<JSONObject> objects){

        getHelpersLocations(new IVolleyResponseCallback<List<JSONObject>>() {
            @Override
            public void onSuccess(final List<JSONObject> helpers) {

                getCurrentUserLocation(new IVolleyResponseCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject user) {

                        for (JSONObject object : helpers) {

                            try {
                                object.put("distance", calculateDistanceBetween(object, user));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Collections.sort(helpers, new Comparator<JSONObject>() {
                            @Override
                            public int compare(JSONObject o1, JSONObject o2) {

                                int compare;
                                double dis1 = 0, dis2 = 0;
                                try {

                                    dis1 = o1.getDouble("distance");
                                    dis2 = o2.getDouble("distance");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if(dis1 <= dis2){
                                    compare = -1;
                                }else
                                    compare = 1;

                                return compare;
                            }
                        });

                        List<String> nearestHelpers = new ArrayList<>();
                        for(int i =0; i<numberOfHelpers; i++){

                            try {
                                nearestHelpers.add(helpers.get(i).getString("email"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        callback.onSuccess(nearestHelpers);
                    }
                });
            }
        }, objects);
    }

    private double calculateDistanceBetween(JSONObject object, JSONObject user) {

        double hlat, hlong, ulat, ulong, distance = 0;

        try {

            hlat = object.getDouble("Latitude");
            hlong = object.getDouble("Longitude");
            ulat = user.getDouble("Latitude");
            ulong = user.getDouble("Longitude");

            double dlong = toRadians(ulong - hlong);
            double dlat = toRadians(ulat - hlat);

            // formula of calculating distance using latitude & longitude
            double a = pow(sin(dlat/2),2) + cos(toRadians(ulat)) * cos(toRadians(hlat)) * pow(sin(dlong/2),2);
            double c = 2 * atan2( sqrt(a), sqrt(1 - a));
            distance = RADIUS_OF_THE_EARTH * c;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return distance;
    }

    private void saveLocation(Context context, FirebaseAuth firebaseAuth) {

        LocationRequest request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        final String path = "locations/users" + "/" + firebaseAuth.getCurrentUser().getEmail().replace(".", ",");

        int permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(" ", "location update " + location);
                        ref.setValue(location);
                    }
                }
            }, null);
        }
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
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
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }
}
