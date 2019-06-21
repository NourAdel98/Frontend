package com.example.myapplication.disabled;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.R;
import com.example.myapplication.request.RequestQueueSinglton;
import com.example.myapplication.request.ViewHistoryActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SupporterProfileActivity extends AppCompatActivity {
    private String emailFirebase,updatedEmail,updatedPassword;
    private Button update;
    private FirebaseAuth firebaseAuth;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = RequestQueueSinglton.getInstance(SupporterProfileActivity.this);
        firebaseAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (firebaseAuth.getCurrentUser() != null) {
            emailFirebase = firebaseAuth.getCurrentUser().getEmail();
            System.out.println("EmailFirebase:" + emailFirebase);
        }

        viewProfile();

        setContentView(R.layout.activity_supporter_profile);

        update = (Button) findViewById(R.id.btn);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatedEmail=((EditText) findViewById(R.id.txt3)).getText().toString().trim();
                updatedPassword=((EditText) findViewById(R.id.txt4)).getText().toString().trim();

                updateFirebaseProfile();
                updateProfile();
            }
        });
    }

    public void viewProfile() {

        String url = "http://192.168.43.63:8383/viewProfile";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            ((EditText) findViewById(R.id.txt1)).setText(res.getString("fname"));
                            ((EditText) findViewById(R.id.txt2)).setText(res.getString("lname"));
                            ((EditText) findViewById(R.id.txt3)).setText(res.getString("email"));
                            ((EditText) findViewById(R.id.txt4)).setText(res.getString("password"));
                            ((EditText) findViewById(R.id.txt5)).setText(res.getString("address"));
                            ((EditText) findViewById(R.id.txt6)).setText(res.getString("age"));
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
                params.put("email", emailFirebase);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void updateFirebaseProfile(){

        if(emailFirebase.equals(updatedEmail)==false) {
            firebaseAuth.getCurrentUser().updateEmail(updatedEmail);
        }
        firebaseAuth.getCurrentUser().updatePassword(updatedPassword);
    }

    public void updateProfile() {

        String url = "http://192.168.43.63:8383/updateSupporterProfile";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("true")) {
                            finish();
                            Intent intent = new Intent(SupporterProfileActivity.this, DisabledHomeActivity.class);
                            startActivity(intent);
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
                params.put("fname",((EditText) findViewById(R.id.txt1)).getText().toString());
                params.put("lname",((EditText) findViewById(R.id.txt2)).getText().toString());
                params.put("email", emailFirebase);
                params.put("updatedEmail",updatedEmail);
                params.put("password",updatedPassword);
                params.put("address",((EditText) findViewById(R.id.txt5)).getText().toString());
                params.put("age",((EditText) findViewById(R.id.txt6)).getText().toString());
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }
}
