package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.disabled.DisabledHomeActivity;
import com.example.myapplication.disabled.DisabledSignUpActivity;
import com.example.myapplication.disabled.SupporterSignUpActivity;
import com.example.myapplication.helper.HelperHomeActivity;
import com.example.myapplication.request.RequestQueueSinglton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private TextInputLayout password;
    private CheckBox checkBox;
    private String emailHolder, passwordHolder;
    private Button Login, SignUP;
    private Boolean editTextStatus;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String helperStatus, userType, firebaseSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.txt1);
        password = (TextInputLayout) findViewById(R.id.txt2);
        Login = (Button) findViewById(R.id.btn1);
        SignUP = (Button) findViewById(R.id.btn2);
        checkBox = (CheckBox) findViewById(R.id.ch);

        progressDialog = new ProgressDialog(LoginActivity.this);
        signUp();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {

            RequestQueue queue = RequestQueueSinglton.getInstance(this);
            final String emailHolder = firebaseAuth.getCurrentUser().getEmail();
            String url = "http://192.168.43.198:8383/getUserType";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            System.out.println(response);

                            if(response.contains("disabled") || response.contains("supporter")){
                                startActivity(new Intent(LoginActivity.this, DisabledHomeActivity.class));
                            }else if(response.contains("helper")){
                                startActivity(new Intent(LoginActivity.this, HelperHomeActivity.class));
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
                    params.put("email", emailHolder);
                    return params;
                }
            };

            queue.add(stringRequest);
            finish();
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailHolder = email.getText().toString().trim();
                passwordHolder = password.getEditText().getText().toString().trim();

                CheckEditTextIsEmptyOrNot();
                if (editTextStatus) {
                    if (passwordHolder.length() >= 8){
                        loginInDatabase();
                    } else {
                        password.getEditText().setText("");
                        password.getEditText().setError("Please Password must be more than or equal 8 letters or numbers");
                        password.getEditText().requestFocus();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please Fill All the Fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);

            }
        });
    }

    public void CheckEditTextIsEmptyOrNot() {
        if (TextUtils.isEmpty(emailHolder) || TextUtils.isEmpty(passwordHolder)) {
            editTextStatus = false;
        } else {
            editTextStatus = true;
        }
    }

    public void loginFunction() {
        progressDialog.setMessage("Please wait..");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(emailHolder, passwordHolder)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            finish();
                            Intent intent = new Intent(LoginActivity.this, DisabledHomeActivity.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Email or Password Not found, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    public void loginHelperFunction() {

        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(emailHolder, passwordHolder)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            finish();
                            Intent intent = new Intent(LoginActivity.this, HelperHomeActivity.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Email or Password Not found, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    public void loginInDatabase() {
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        String url = "http://192.168.43.198:8383/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(!response.contains("no")){

                            if(response.contains("Disabled") || response.contains("Supporter")){
                                loginFunction();
                            }else if(response.contains("Helper_true")){
                                helperRegistrationFunction();
                                loginHelperFunction();
                            }else {
                                Toast.makeText(LoginActivity.this, "your are not accepted until now!", Toast.LENGTH_LONG).show();
                            }

                        }else{
                            Toast.makeText(LoginActivity.this, "Email or Password is wrong, Please Try Again", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("error: " + error.getMessage());
            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailHolder);
                params.put("password", passwordHolder);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void sendEmailVerification() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Check your Email for verification", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void helperRegistrationFunction() {

        progressDialog.setMessage("Please wait, We are registering your data");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailHolder, passwordHolder).
                addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendEmailVerification();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void signUp() {

        CheckBox checkBox1 = (CheckBox) findViewById(R.id.ch1);
        CheckBox checkBox3 = (CheckBox) findViewById(R.id.ch3);

        checkBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                Intent intent = new Intent(LoginActivity.this, DisabledSignUpActivity.class);
                startActivity(intent);
            }
        });

        checkBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                Intent intent = new Intent(LoginActivity.this, SupporterSignUpActivity.class);
                startActivity(intent);
            }
        });
    }

}