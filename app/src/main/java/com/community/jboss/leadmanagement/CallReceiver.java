package com.community.jboss.leadmanagement;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.community.jboss.leadmanagement.data.daos.ContactNumberDao;
import com.community.jboss.leadmanagement.data.entities.ContactNumber;
import com.community.jboss.leadmanagement.main.contacts.editcontact.EditContactActivity;
import com.community.jboss.leadmanagement.utils.DbUtil;

public class CallReceiver extends BroadcastReceiver {
    private static final int ID = 47981;

    private ContactNumber mContactNumber;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TextUtils.equals(intent.getAction(), "android.intent.action.PHONE_STATE")) {
            return;
        }

        final Bundle extras = intent.getExtras();
        if (extras == null) return;

        final String state = extras.getString(TelephonyManager.EXTRA_STATE);
        if (state == null) return;

        final String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (number == null) return;

        mContext = context;
        final ContactNumberDao dao = DbUtil.contactNumberDao(mContext);
        mContactNumber = dao.getContactNumber(number);

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            showNotification();
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            hideNotification();
        } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            showNotification();
        }
    }

    private void hideNotification() {
        final NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(ID);
        }
    }

    private void showNotification() {

        final Intent notificationIntent = new Intent(mContext, EditContactActivity.class);

        notificationIntent.putExtra(
                EditContactActivity.INTENT_EXTRA_CONTACT_ID, mContactNumber.getContactId());

        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_call_black_24dp)
                .setContentTitle("Call in Progress")
                .setTicker("Lead Management")
                .setContentIntent(contentIntent)
                .setContentText("Number: " + mContactNumber.getNumber());

        final NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(ID);
            manager.notify(ID, notification.build());
        }
    }
}
