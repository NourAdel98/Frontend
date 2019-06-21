package com.example.myapplication.helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;

public class HelperContactUsActivity extends AppCompatActivity {
    private Button email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_contact__us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        email = (Button) findViewById(R.id.btn2);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(HelperContactUsActivity.this, HelperMailActivity.class);
                startActivity(intent);
            }
        });
    }

    public void Call(View v) {
        String phoneNumber="01128115377";

        Uri u = Uri.parse("tel:" + phoneNumber);

        Intent i = new Intent(Intent.ACTION_DIAL, u);
        try {
            startActivity(i);
        } catch (SecurityException s) {
            Toast.makeText(this, s.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, HelperHomeActivity.class));
        return true;
    }
}
