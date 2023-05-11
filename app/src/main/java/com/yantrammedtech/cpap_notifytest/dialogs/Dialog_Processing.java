package com.yantrammedtech.cpap_notifytest.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.yantrammedtech.cpap_notifytest.R;

public class Dialog_Processing {
    private Context context;
    private AlertDialog alertDialog;

    public Dialog_Processing(Context context) {
        this.context = context;
    }

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.alert_processing, null, false);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void dismissDialog() {
        alertDialog.dismiss();
    }
}
