package com.smart.csoft.services;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by umprasad on 12/24/2017.
 */

public class DateTimeDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private final Calendar calendar = Calendar.getInstance();
    private final DateFormat sysFormat = DateFormat.getDateInstance();
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private String customFormat;
    private EditText editText;
    private boolean isDate = false;

    public DateTimeDialog() {
        timeFormat = new SimpleDateFormat(SmartHomeUtils.TIME_SEC);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfday = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        if (isDate) {
            return new DatePickerDialog(getActivity(), this, year, month, day);
        } else {
            return new TimePickerDialog(getActivity(), this, hourOfday, min, true);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        if (editText != null) {
            editText.setText(sysFormat.format(calendar.getTime()));
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hoursOfday, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hoursOfday);
        calendar.set(Calendar.MINUTE, minute);
        if (editText != null) {
            editText.setText(timeFormat.format(calendar.getTime()));
        }
    }

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public String getCustomFormat() {
        return customFormat;
    }

    public void setCustomeFormat(String customFormat) {
        this.customFormat = customFormat;
        timeFormat = new SimpleDateFormat(customFormat);
    }
}
