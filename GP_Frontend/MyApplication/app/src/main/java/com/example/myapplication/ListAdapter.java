package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.helper.RequestHandleActivity;
import com.example.myapplication.request.RequestActivity;
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


public class ListAdapter extends ArrayAdapter<JSONObject> {

    Context context;
    ArrayList<JSONObject> objects;

    public ListAdapter(Context context, ArrayList<JSONObject> objects) {

        super(context, 0 , objects);
        this.context = context;
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.helper_request_list, parent, false);
        }

        final SharedPreferences preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.apply();

        JSONObject request = getItem(position);
        try {

            final TextView requestId = (TextView) convertView.findViewById(R.id.request_id);
            requestId.setText(Integer.toString(request.getInt("requestId")));

            TextView service = (TextView) convertView.findViewById(R.id.service_type);
            service.setText(request.getJSONObject("serviceId").getString("description"));

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.accept);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        Toast.makeText(context, "accepted", Toast.LENGTH_SHORT).show();

                        RequestQueue queue = RequestQueueSinglton.getInstance(context);
                        String url = "http://192.168.43.198:8383/accept";

                        StringRequest stringRequest = new StringRequest
                                (Request.Method.POST, url, new Response.Listener<String>() {

                                    @Override
                                    public void onResponse(String response) {

                                        try {
                                            JSONArray array = new JSONArray(response);
                                            sendNotification(Integer.parseInt(array.getString(0)));

                                            editor.putInt("requestId", Integer.parseInt(array.getString(0)));
                                            editor.putString("disabledId", array.getString(1));
                                            editor.putString("disabledName", array.getString(2));
                                            editor.putString("disabledNum", array.getString(3));

                                            editor.commit();
                                            Intent intent = new Intent(context, RequestHandleActivity.class);
                                            context.startActivity(intent);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println("errr");
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> params = new HashMap<>();
                                params.put("requestId", String.valueOf(requestId.getText()));
                                params.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                return params;
                            }
                        };
                        queue.add(stringRequest);
                    } else
                        Toast.makeText(context, "unchecked", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    void sendNotification(int requestId){

        DatabaseReference notifications = FirebaseDatabase.getInstance().getReference().child("notifications")
                .child(String.valueOf(requestId));
        Map<String, String> map = new HashMap<>();
        map.put("helper", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        notifications.setValue(map);
    }
}
