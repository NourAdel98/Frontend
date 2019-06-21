package com.example.myapplication.request;


import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSinglton {

    private static RequestQueue requestQueue;
    private Context context;

    private RequestQueueSinglton() { }

    public static synchronized RequestQueue getInstance(Context context){

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }
}
