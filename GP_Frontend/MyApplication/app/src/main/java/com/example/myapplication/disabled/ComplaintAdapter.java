package com.example.myapplication.disabled;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ComplaintAdapter extends ArrayAdapter<JSONObject> {

    ComplaintAdapter(Context context, ArrayList objects) {
        super(context, 0 , objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.complaint_list_item, parent, false);
        }

        try {

            JSONObject complaint = getItem(position);

            Date obj = new Date((Long) complaint.get("date"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String dateText = sdf.format(obj);

            String date, time;
            date = dateText.split(" ")[0];
            time = dateText.split(" ")[1];

            String type = complaint.getString("type");
            String message;
            if(type.equals("user"))
                message = complaint.getString("description");
            else {
                message = complaint.getString("warning");
            }

            TextView complaintMessage = convertView.findViewById(R.id.com_desc);
            if(message.length() >= 7)
                message = message.substring(0,7) + "...";
            complaintMessage.setText(message);

            TextView complaintDate = convertView.findViewById(R.id.com_date);
            complaintDate.setText(date + '\n' + time);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}