package com.example.myapplication.helper;

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
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HelperProfileActivity extends AppCompatActivity {
    private String emailFirebase,updatedPassword;
    private Button update;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            emailFirebase = firebaseAuth.getCurrentUser().getEmail();
            System.out.println("EmailFirebase:" + emailFirebase);
        }

        viewProfile();

        setContentView(R.layout.activity_helper_profile);

        update = (Button) findViewById(R.id.btn);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatedPassword=((EditText) findViewById(R.id.txt2)).getText().toString().trim();
                System.out.println("updatedPassword:"+updatedPassword);

                updateFirebaseProfile();
                updateProfile();
            }
        });

    }

    public void viewProfile() {

        RequestQueue queue = Volley.newRequestQueue(HelperProfileActivity.this);
        String url = "http://192.168.43.63:8383/viewProfile";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            ((EditText) findViewById(R.id.txt1)).setText(res.getString("email"));
                            ((EditText) findViewById(R.id.txt2)).setText(res.getString("password"));
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
        firebaseAuth.getCurrentUser().updatePassword(updatedPassword);
    }

    public void updateProfile() {
        RequestQueue queue = Volley.newRequestQueue(HelperProfileActivity.this);
        String url = "http://192.168.43.63:8383/updateHelperProfile";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("true")) {
                            finish();
                            Intent intent = new Intent(HelperProfileActivity.this, HelperHomeActivity.class);
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
                params.put("email", emailFirebase);
                params.put("password",updatedPassword);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, HelperHomeActivity.class));
        return true;
    }
}
