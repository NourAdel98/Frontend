package com.example.myapplication.request;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.IVolleyResponseCallback;
import com.example.myapplication.R;
import com.example.myapplication.disabled.DisabledHomeActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewHistoryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);
        firebaseAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        queue = RequestQueueSinglton.getInstance(ViewHistoryActivity.this);
        final SharedPreferences.Editor editor = getSharedPreferences("preferences", Context.MODE_PRIVATE).edit();
        editor.apply();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getAllRequests(new IVolleyResponseCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray res) {

                ArrayList<Request> requests = new ArrayList<>();
                Request request;
                for (int i = 0; i < res.length(); i++) {

                    try {

                        Date obj = new Date((Long) res.getJSONObject(i).get("date"));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                        String dateText = sdf.format(obj);

                        String date, time;
                        date = dateText.split(" ")[0];
                        time = dateText.split(" ")[1];

                        JSONObject serviceObj = new JSONObject(res.getJSONObject(i).getString("serviceId"));
                        String service = serviceObj.getString("description");

                        int id = res.getJSONObject(i).getInt("requestId");

                        requests.add(new Request(id, service, date, time));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                RequestAdapter adapter = new RequestAdapter(ViewHistoryActivity.this, requests);
                final ListView listView = findViewById(R.id.viewList);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Request item = (Request) parent.getItemAtPosition(position);
                        editor.putInt("id", item.getId()).commit();
                        startActivity(new Intent(getApplicationContext(), ViewRequestDetails.class));
                    }
                });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }

    private void getAllRequests(final IVolleyResponseCallback callback){

        String url = "http://192.168.43.198:8383/get-all-requests";

        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray requests = new JSONArray(response);
                    System.out.println(response);
                    callback.onSuccess(requests);

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
                params.put("email", firebaseAuth.getCurrentUser().getEmail());

                return params;
            }
        };
        queue.add(request);
    }
}
