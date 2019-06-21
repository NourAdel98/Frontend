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

public class SupporterSignUpActivity extends AppCompatActivity {


    EditText fName, lName, email,address, phone;
    TextInputLayout password, confirmPass;
    Button SignUp;
    String fNameHolder, lNameHolder, emailHolder, passwordHolder, ConfirmPassswordHolder, addressHolder, phoneHolder;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference mdatabase;
    Boolean editTextStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supporter__sign_up);
        fName = (EditText) findViewById(R.id.txt1);
        lName = (EditText) findViewById(R.id.txt2);
        email = (EditText) findViewById(R.id.txt3);
        password = (TextInputLayout ) findViewById(R.id.pass);
        confirmPass = (TextInputLayout ) findViewById(R.id.conf_pass);
        address = (EditText) findViewById(R.id.txt6);
        phone = (EditText) findViewById(R.id.txt7);

        SignUp = (Button) findViewById(R.id.button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(SupporterSignUpActivity.this);

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fNameHolder = fName.getText().toString().trim();
                lNameHolder = lName.getText().toString().trim();
                emailHolder = email.getText().toString().trim();
                passwordHolder = password.getEditText().getText().toString().trim();
                ConfirmPassswordHolder = confirmPass.getEditText().getText().toString().trim();
                addressHolder = address.getText().toString().trim();
                phoneHolder = phone.getText().toString().trim();

                CheckEditTextIsEmptyOrNot();
                if (editTextStatus) {
                    if ((ConfirmPassswordHolder.equals(passwordHolder))&&(passwordHolder.length() >= 8 && ConfirmPassswordHolder.length() >= 8)) {
                        if(phoneHolder.length()==11 || phoneHolder.length()==8) {
                            saveInDatabase();
                        }else {
                            phone.setError("Please Enter a valid phone number");
                            phone.requestFocus();
                        }
                    } else {
                        ((TextInputLayout ) findViewById(R.id.pass)).getEditText().setText("");
                        ((TextInputLayout ) findViewById(R.id.conf_pass)).getEditText().setText("");

                        password.getEditText().setError("Password doesn't match with Confirmation Password! Please enter them again");
                        password.getEditText().requestFocus();
                        confirmPass.getEditText().setError("Password doesn't match with Confirmation Password! Please enter them again");
                        confirmPass.getEditText().requestFocus();
                    }
                } else {
                    Toast.makeText(SupporterSignUpActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SupporterSignUpActivity.this,"Check your Email for verification",Toast.LENGTH_SHORT).show();
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
                addOnCompleteListener(SupporterSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SupporterSignUpActivity.this, "User Registration Successfully", Toast.LENGTH_LONG).show();
                            sendEmailVerification();
                            progressDialog.dismiss();
                            firebaseAuth.signOut();

                            finish();
                            Intent intent = new Intent(SupporterSignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SupporterSignUpActivity.this, "you entered invalid data Or this data is already used.", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    public void CheckEditTextIsEmptyOrNot() {
        if (TextUtils.isEmpty(fNameHolder)||TextUtils.isEmpty(lNameHolder)||TextUtils.isEmpty(emailHolder) ||
                TextUtils.isEmpty(passwordHolder)||TextUtils.isEmpty(ConfirmPassswordHolder)|| TextUtils.isEmpty(addressHolder)||
                TextUtils.isEmpty(phoneHolder)) {
            editTextStatus = false;
        } else {
            editTextStatus = true;
        }

    }

    public void saveInDatabase() {
        RequestQueue queue = Volley.newRequestQueue(SupporterSignUpActivity.this);
        String url = "http://192.168.43.63:8383/registerSupporter";

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
                params.put("email", ((EditText) findViewById(R.id.txt3)).getText().toString());
                params.put("fname", ((EditText) findViewById(R.id.txt1)).getText().toString());
                params.put("lname", ((EditText) findViewById(R.id.txt2)).getText().toString());
                params.put("password", passwordHolder);
                params.put("address", ((EditText) findViewById(R.id.txt6)).getText().toString());
                params.put("phoneNumber", ((EditText) findViewById(R.id.txt7)).getText().toString());
                params.put("type", "supporter");
                params.put("status", "---");

                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, LoginActivity.class));
        return true;
    }
}
