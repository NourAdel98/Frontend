package com.example.myapplication.helper;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.myapplication.Upload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class HelperSignUpActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE = 2342;

    EditText email,fileName;
    TextInputLayout password;
    CheckBox ch1, ch2, ch3, ch4, ch5;
    Button submit;
    int count=0;
    String emailHolder, passwordHolder,skills="";
    ProgressDialog progressDialog;
    Boolean editTextStatus, uploadedPDF = false;

    TextView textViewStatus;
    Button upload;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    DatabaseReference mdatabase;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper__sign_up);

        email = (EditText) findViewById(R.id.txt2);
        password = (TextInputLayout ) findViewById(R.id.pass);
        fileName = (EditText) findViewById(R.id.txt5);
        submit = (Button) findViewById(R.id.btn);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        upload = (Button) findViewById(R.id.buttonUploadFile);
        ch1 = (CheckBox) findViewById(R.id.ch1);
        ch2 = (CheckBox) findViewById(R.id.ch2);
        ch3 = (CheckBox) findViewById(R.id.ch3);
        ch4 = (CheckBox) findViewById(R.id.ch4);
        ch5 = (CheckBox) findViewById(R.id.ch5);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        progressDialog = new ProgressDialog(HelperSignUpActivity.this);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPDF();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailHolder = email.getText().toString().trim();
                passwordHolder = password.getEditText().getText().toString().trim();

                CheckEditTextIsEmptyOrNot();
                if (editTextStatus) {
                    if ((passwordHolder.length() < 8)) {
                        password.getEditText().setText("");
                        password.getEditText().setError("Please Password must be more than or equal 8 letters or numbers");
                        password.getEditText().requestFocus();
                    }
                }
                if (ch1.isChecked()) {
                    skills+=ch1.getText().toString()+",";
                    count++;
                }
                if (ch2.isChecked()) {
                    skills+=ch2.getText().toString()+",";
                    count++;
                }
                if (ch3.isChecked()) {
                    skills+=ch3.getText().toString()+",";
                    count++;
                }
                if (ch4.isChecked()) {
                    skills+=ch4.getText().toString()+",";
                    count++;
                }
                if (ch5.isChecked()) {
                    skills+=ch5.getText().toString();
                    count++;
                }
                if(count >0 && editTextStatus){
                    saveInDatabase();
                }else {
                    if (editTextStatus == false) {
                        Toast.makeText(HelperSignUpActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();
                    }
                    if (count == 0) {
                        Toast.makeText(HelperSignUpActivity.this, "Please select your skill/skills.", Toast.LENGTH_LONG).show();
                    }
                }

               // System.out.println("Skills:"+skills);
            }
        });
    }

    private void getPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (data.getData() != null) {
                uploadFile(data.getData());
            } else {
                Toast.makeText(this, "No file chosen", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadFile(Uri data) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference sRef = mStorageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + ".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        textViewStatus.setText("File Uploaded Successfully");

                        Upload upload = new Upload(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                        mDatabaseReference.child(mDatabaseReference.push().getKey()).setValue(upload);

                        uploadedPDF = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        textViewStatus.setText((int) progress + "% Uploading...");
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
                        Toast.makeText(HelperSignUpActivity.this,"Check your Email for verification",Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }


    public void UserRegistrationFunction() {
        progressDialog.setMessage("Please Wait, We are Registering Your Data");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailHolder, passwordHolder).
                addOnCompleteListener(HelperSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && uploadedPDF == true) {
                            Toast.makeText(HelperSignUpActivity.this, "User Registration Successfully", Toast.LENGTH_LONG).show();
                            sendEmailVerification();
                            progressDialog.dismiss();
                            firebaseAuth.signOut();

                            finish();
                            Intent intent = new Intent(HelperSignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else if (task.isSuccessful() && uploadedPDF == false) {
                            Toast.makeText(HelperSignUpActivity.this, "you must upload your CV.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HelperSignUpActivity.this, "you entered invalid data Or this data is already used.", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
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

    public void saveInDatabase() {
        RequestQueue queue = Volley.newRequestQueue(HelperSignUpActivity.this);
        String url = "http://192.168.43.63:8383/registerHelper";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.contains("true")) {
                            UserRegistrationFunction();
                        } else {
                            Toast.makeText(HelperSignUpActivity.this, "Something went wrong ,please try again.", Toast.LENGTH_LONG).show();
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
                params.put("email", ((EditText) findViewById(R.id.txt2)).getText().toString());
                params.put("password", passwordHolder);
                params.put("type", "helper");
                params.put("status", "accepted");
                params.put("skills",skills);
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
