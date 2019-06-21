package com.example.myapplication.Pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hours, minutes,false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        TextView editTime = (TextView) getActivity().findViewById(R.id.editTime);
        Date date = new Date();
        date.setHours(hourOfDay);
        date.setMinutes(minute);

        editTime.setText(new SimpleDateFormat("HH:mm").format(date));
    }
}
