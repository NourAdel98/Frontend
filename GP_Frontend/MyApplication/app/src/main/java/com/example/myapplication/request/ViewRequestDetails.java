package com.example.myapplication.request;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.IVolleyResponseCallback;
import com.example.myapplication.R;
import com.example.myapplication.disabled.DisabledHomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewRequestDetails extends AppCompatActivity {

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_details);

        queue = RequestQueueSinglton.getInstance(ViewRequestDetails.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewRequest(new IVolleyResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject request) {

                try {
                    Date obj = new Date((Long) request.get("date"));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    String dateText = sdf.format(obj);

                    String date, time;
                    date = dateText.split(" ")[0];
                    time = dateText.split(" ")[1];

                    JSONObject object = new JSONObject(request.getString("serviceId"));
                    ((TextView) findViewById(R.id.service)).setText(object.getString("description"));

                    ((TextView) findViewById(R.id.requestDate)).setText(date);
                    ((TextView) findViewById(R.id.requestTime)).setText(time);



                    if (request.getString("helperId").equals("null")) {
                        ((TextView) findViewById(R.id.helper)).setText("not handled yet!!");
                    } else {
                        object = new JSONObject(request.getString("helperId"));
                        ((TextView) findViewById(R.id.helper)).setText(object.getString("email"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, ViewHistoryActivity.class));
        return true;
    }

    void viewRequest(final IVolleyResponseCallback callback){

        String url = "http://192.168.43.198:8383/get-request-by-id";

        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    callback.onSuccess(jsonObject);
                } catch (JSONException e) {
                    startActivity(new Intent(ViewRequestDetails.this, ViewHistoryActivity.class));
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
                params.put("id", String.valueOf(getSharedPreferences("preferences",
                        Context.MODE_PRIVATE).getInt("id", 0)));
                return params;
            }
        };
        queue.add(request);
    }
}
