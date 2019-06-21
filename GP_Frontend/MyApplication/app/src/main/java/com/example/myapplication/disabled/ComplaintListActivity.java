package com.example.myapplication.disabled;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.IVolleyResponseCallback;
import com.example.myapplication.R;
import com.example.myapplication.helper.HelperHomeActivity;
import com.example.myapplication.request.RequestQueueSinglton;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComplaintListActivity extends AppCompatActivity {

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        queue = RequestQueueSinglton.getInstance(this);

        getAllComplaints(new IVolleyResponseCallback<JSONArray>() {
            @Override
            public void onSuccess(final JSONArray res) {

                getUserType(new IVolleyResponseCallback<String>() {
                    @Override
                    public void onSuccess(String type) {

                        ArrayList<JSONObject> complaints = new ArrayList<>();
                        JSONObject complaint = new JSONObject();

                        for (int i = 0; i < res.length(); i++) {
                            try {

                                complaint.put("date", res.getJSONObject(i).getLong("date"));
                                complaint.put("description", res.getJSONObject(i).getString("description"));
                                complaint.put("complaintReply", res.getJSONObject(i).getString("complaintReply"));
                                complaint.put("warning", res.getJSONObject(i).getString("warning"));

                                if(type.equals("disabled") || type.equals("supporter")) {
                                    complaint.put("type", "user");
                                }
                                else {
                                    complaint.put("type", "helper");
                                }

                                complaints.add(complaint);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ComplaintAdapter adapter = new ComplaintAdapter(ComplaintListActivity.this, complaints);
                        final ListView listView = findViewById(R.id.complaint_list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                JSONObject object = (JSONObject) parent.getItemAtPosition(position);
                                Intent intent = new Intent(getApplicationContext(), ComplaintDetailsActivity.class);
                                intent.putExtra("object", object.toString());
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getUserType(new IVolleyResponseCallback<String>() {
            @Override
            public void onSuccess(String res) {

                if(res.equals("disabled") || res.equals("supporter")){

                    MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.menuu, menu);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.add) {
            startActivity(new Intent(this, ComplaintActivity.class));
        }else{
            getUserType(new IVolleyResponseCallback<String>() {
                @Override
                public void onSuccess(String res) {

                    if(res.equals("disabled") || res.equals("supporter"))
                        startActivity(new Intent(ComplaintListActivity.this, DisabledHomeActivity.class));
                    else
                        startActivity(new Intent(ComplaintListActivity.this, HelperHomeActivity.class));
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllComplaints(final IVolleyResponseCallback callback){

        String url = "http://192.168.43.198:8383/get-all-complaints";

        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray complaints = new JSONArray(response);
                    callback.onSuccess(complaints);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                return params;
            }
        };
        queue.add(request);
    }

    private void getUserType(final IVolleyResponseCallback callback){

        String url = "http://192.168.43.198:8383/getUserType";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
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
}
