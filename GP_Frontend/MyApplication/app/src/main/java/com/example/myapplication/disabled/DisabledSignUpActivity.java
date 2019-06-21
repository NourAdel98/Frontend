package com.example.myapplication.disabled;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.request.ViewHistoryActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DisabledSignUpActivity extends AppCompatActivity {

    private EditText fName, lName, email,disabilityType, address, age, phone;
    private TextInputLayout password, confirmPass;
    private Button SignUp;
    private String fNameHolder, lNameHolder, emailHolder, passwordHolder, ConfirmPassswordHolder, disabilityTypeHolder, addressHolder, ageHolder, phoneHolder;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    DatabaseReference mdatabase;
    private Boolean editTextStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_sign_up);
        fName = (EditText) findViewById(R.id.d_fname);
        lName = (EditText) findViewById(R.id.d_lname);
        email = (EditText) findViewById(R.id.d_email);
        password = (TextInputLayout ) findViewById(R.id.d_pass);
        confirmPass = (TextInputLayout ) findViewById(R.id.d_conf_pass);
        disabilityType = (EditText) findViewById(R.id.d_dis_type);
        age = (EditText) findViewById(R.id.d_age);
        phone = (EditText) findViewById(R.id.d_phone);
        SignUp = (Button) findViewById(R.id.d_button);

        firebaseAuth = FirebaseAuth.getInstance();
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(DisabledSignUpActivity.this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fNameHolder = fName.getText().toString().trim();
                lNameHolder = lName.getText().toString().trim();
                emailHolder = email.getText().toString().trim();
                passwordHolder = password.getEditText().getText().toString();
                ConfirmPassswordHolder = confirmPass.getEditText().getText().toString().trim();
                disabilityTypeHolder = disabilityType.getText().toString().trim();
                ageHolder = age.getText().toString().trim();
                phoneHolder = phone.getText().toString().trim();

                CheckEditTextIsEmptyOrNot();
                if (editTextStatus) {
                    if ((ConfirmPassswordHolder.equals(passwordHolder)) && (passwordHolder.length() >= 8 && ConfirmPassswordHolder.length() >= 8)) {
                        if (Integer.parseInt(ageHolder) >= 18) {
                            saveInDatabase();
                        } else {
                            phone.setError("Please enter a supporter phone number");
                            phone.requestFocus();

                            SignUp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    saveInDatabase();
                                }
                            });

                        }
                    }else {
                        ((TextInputLayout) findViewById(R.id.d_pass)).getEditText().setText("");
                        ((TextInputLayout) findViewById(R.id.d_conf_pass)).getEditText().setText("");

                        password.getEditText().setError("Password doesn't match with Confirmation Password! Please enter them again");
                        password.getEditText().requestFocus();
                        confirmPass.getEditText().setError("Password doesn't match with Confirmation Password! Please enter them again");
                        confirmPass.getEditText().requestFocus();
                    }
                } else {
                    Toast.makeText(DisabledSignUpActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, LoginActivity.class));
        return true;
    }

    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(DisabledSignUpActivity.this,"Check your Email for verification",Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }

    public void UserRegistrationFunction() {
        progressDialog.setMessage("Please Wait, We are Registering Your Data on Server");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailHolder, passwordHolder).
                addOnCompleteListener(DisabledSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(DisabledSignUpActivity.this, "User Registration Successfully", Toast.LENGTH_LONG).show();
                            sendEmailVerification();
                            progressDialog.dismiss();
                            firebaseAuth.signOut();

                            finish();
                            Intent intent = new Intent(DisabledSignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(DisabledSignUpActivity.this, "you entered invalid data Or this data is already used.", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    public void CheckEditTextIsEmptyOrNot() {
        if (TextUtils.isEmpty(fNameHolder) || TextUtils.isEmpty(lNameHolder) || TextUtils.isEmpty(emailHolder) ||
                TextUtils.isEmpty(passwordHolder) || TextUtils.isEmpty(ConfirmPassswordHolder) || TextUtils.isEmpty(disabilityTypeHolder) ||
                TextUtils.isEmpty(ageHolder)) {
            editTextStatus = false;
        } else {
            editTextStatus = true;
        }

    }

    public void saveInDatabase() {
        RequestQueue queue = Volley.newRequestQueue(DisabledSignUpActivity.this);
        String url = "http://192.168.43.198:8383/registerDisabled";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("true")) {
                            UserRegistrationFunction();
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
                params.put("email", ((EditText) findViewById(R.id.d_email)).getText().toString());
                params.put("age", ((EditText) findViewById(R.id.d_age)).getText().toString());
                params.put("fname", ((EditText) findViewById(R.id.d_fname)).getText().toString());
                params.put("lname", ((EditText) findViewById(R.id.d_lname)).getText().toString());
                params.put("password", passwordHolder);
                params.put("disabilityType", ((EditText) findViewById(R.id.d_dis_type)).getText().toString());
                params.put("phoneNumber", ((EditText) findViewById(R.id.d_phone)).getText().toString());
                return params;
            }
        };
        queue.add(stringRequest);
    }
}

