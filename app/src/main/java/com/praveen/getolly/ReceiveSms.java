package com.praveen.getolly;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiveSms extends BroadcastReceiver {
    private String TAG = ReceiveSms.class.getSimpleName();
    private String msgFrom;
    public ReceiveSms() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress() + " : ";
                msgFrom =  msgs[i].getOriginatingAddress();
                int id = msgs[i].getStatus();
                str += msgs[i].getMessageBody().toString();
                str += "\n";
            }
            Log.d(TAG, str);
        }
    }



}
