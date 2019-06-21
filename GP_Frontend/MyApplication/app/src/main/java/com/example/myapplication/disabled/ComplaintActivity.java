package com.example.myapplication.disabled;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.R;
import com.example.myapplication.request.RequestQueueSinglton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {

    private Button send;
    private String complaint, userEmail;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        firebaseAuth = FirebaseAuth.getInstance();
        userEmail = firebaseAuth.getCurrentUser().getEmail();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        send = (Button) findViewById(R.id.btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                complaint = ((EditText) findViewById(R.id.txt2)).getText().toString();
                saveComplaintsInDatabase();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, ComplaintListActivity.class));
        return true;
    }

    public void saveComplaintsInDatabase() {

        RequestQueue queue = RequestQueueSinglton.getInstance(this);
        String url = "http://192.168.43.198:8383/addComplaint";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.contains("true")) {
                            Toast.makeText(ComplaintActivity.this, "your complaint is submitted.", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ComplaintActivity.this, "You don't have any requests to complain :)", Toast.LENGTH_LONG).show();
                        }

                        finish();
                        Intent intent = new Intent(ComplaintActivity.this, DisabledHomeActivity.class);
                        startActivity(intent);
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

                Date dateObj = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String date = sdf.format(dateObj);

                params.put("description", complaint);
                params.put("date", date);
                params.put("userEmail", userEmail);

                return params;
            }
        };
        queue.add(stringRequest);
    }
}
