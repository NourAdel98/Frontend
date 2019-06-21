package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.request.ViewHistoryActivity;

public class CodeActivity extends AppCompatActivity {
    private EditText code, email;
    private Button send;
    public String emailHolder,codeHolder,checkedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        email = (EditText) findViewById(R.id.txt2);
        code = (EditText) findViewById(R.id.txt4);
        send = (Button) findViewById(R.id.btn);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailHolder = email.getText().toString().trim();
                codeHolder=code.getText().toString().trim();

                Intent intent = getIntent();
                checkedCode = intent.getStringExtra("smsCode");

                if(codeHolder.equals(checkedCode)) {
                    Intent in = new Intent(CodeActivity.this, GetPasswordActivity.class);
                    in.putExtra("email", emailHolder);
                    startActivity(in);
                }else {
                    Toast.makeText(getApplicationContext(),"Wrong Code! Please enter code again.", Toast.LENGTH_LONG).show();
                    ((EditText) findViewById(R.id.txt4)).setText("");
                }
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, ForgetPasswordActivity.class));
        return true;
    }
}
