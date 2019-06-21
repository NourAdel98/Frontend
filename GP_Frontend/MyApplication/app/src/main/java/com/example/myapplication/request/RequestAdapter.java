package com.example.myapplication.request;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class RequestAdapter extends ArrayAdapter<Request> {

    RequestAdapter(Context context, ArrayList objects) {
        super(context, 0 , objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list,parent,false);
        }

        Request request = getItem(position);

        TextView service = (TextView) convertView.findViewById(R.id.service);
        service.setText(request.getService());

        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(request.getDate());

        TextView time = (TextView) convertView.findViewById(R.id.time);
        time.setText(request.getTime());

        TextView id = (TextView) convertView.findViewById(R.id.requestId);
        id.setText(Integer.toString(request.getId()));

        return convertView;
    }
}
