package com.digitalonboarding.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.digitalonboarding.MessageActivity;

/**
 * Created by apptitud on 12/07/2017.
 */

public class IncomingSms extends BroadcastReceiver {

  public void onReceive(Context context, Intent intent) {

    // Retrieves a map of extended data from the intent.
    final Bundle bundle = intent.getExtras();

    try {

      if (bundle != null) {

        final Object[] pdusObj = (Object[]) bundle.get("pdus");

        for (int i = 0; i < pdusObj.length; i++) {

          SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
          String phoneNumber = currentMessage.getDisplayOriginatingAddress();

          String senderNum = phoneNumber;
          String message = currentMessage.getDisplayMessageBody();

          final SharedPreferences prefs = context.getSharedPreferences("Settings",Context.MODE_PRIVATE);

          Log.e("sms ", message);
          if (message.contains("Onboarding VeriTran")){
            Log.e("sms 2", message);
            Intent mBroadcastIntent = new Intent();
            mBroadcastIntent.putExtra("code",message.split(": ")[1]);
            mBroadcastIntent.setAction(MessageActivity.INTENT_ACTION);
            context.sendBroadcast(mBroadcastIntent);
          }
        }
      }

    } catch (Exception e) {
      Log.e("SmsReceiver", "Exception smsReceiver" +e);

    }
  }
}
