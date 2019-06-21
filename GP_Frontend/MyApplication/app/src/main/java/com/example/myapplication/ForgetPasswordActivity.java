package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.request.ViewHistoryActivity;

public class ForgetPasswordActivity extends AppCompatActivity {
    private EditText phoneNumber;
    private int smsCode;
    private Button send, back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        phoneNumber = (EditText) findViewById(R.id.txt3);
        send = (Button) findViewById(R.id.btn1);
        back = (Button) findViewById(R.id.btn2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCodeAsSMS();
                finish();
                Intent in = new Intent(ForgetPasswordActivity.this, CodeActivity.class);
                in.putExtra("smsCode",String.valueOf(smsCode));
                startActivity(in);
            }

        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }

        });
    }

    public void sendCodeAsSMS() {
        generateRandomNumber();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber.getText().toString(), null, String.valueOf(smsCode), null, null);
            Toast.makeText(getApplicationContext(), "Your sms has successfully sent!", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Your sms has failed...", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void generateRandomNumber(){
        int min=1,max=1000;
        smsCode= (int) ((Math.random()*((max-min)+1))+min);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, LoginActivity.class));
        return true;
    }

}
