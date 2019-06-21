package com.example.myapplication.disabled;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class ComplaintDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        try {
            JSONObject object = new JSONObject(getIntent().getExtras().getString("object"));
            showComplaint(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showComplaint(final JSONObject object){

        try {

            TextView complaint = (TextView) findViewById(R.id.complaintId);
            complaint.setText(object.getString("description"));

            String type = object.getString("type");
            TextView reply = (TextView) findViewById(R.id.reply);
            TextView admin = (TextView) findViewById(R.id.admin);

            if(type.equals("user")) {
                reply.setText(object.getString("complaintReply"));
                admin.setText("Admin reply..");
            }else{
                reply.setText(object.getString("warning"));
                admin.setText("Admin warning!!");
                ((TextView)findViewById(R.id.textView2)).setVisibility(View.GONE);
            }

            Date obj = new Date((Long) object.get("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String dateText = sdf.format(obj);

            String date, time;
            date = dateText.split(" ")[0];
            time = dateText.split(" ")[1];

            TextView complaintDate = (TextView) findViewById(R.id.complaint_date);
            complaintDate.setText(date + '\n' + time);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivity(new Intent(this, ComplaintListActivity.class));
        return true;
    }
}
