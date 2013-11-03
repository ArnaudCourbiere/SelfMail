package com.javatomic.drupal.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.javatomic.drupal.R;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.service.SendEmailService;
import com.javatomic.drupal.ui.activity.ResendActivity;
import com.javatomic.drupal.ui.activity.ShareDataActivity;

/**
 * Notification utilities.
 */
public class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    public static void showErrorSendingEmail(Context context, Email email) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_info, Notification.PRIORITY_MAX)
                .setContentTitle(context.getString(R.string.error_sending_selfmail))
                .setContentText(email.getSubject())
                .setAutoCancel(true);

        final Intent resendIntent = new Intent(context, ResendActivity.class);
        resendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        resendIntent.putExtra(SendEmailService.EMAIL, email);

        final PendingIntent notifyIntent = PendingIntent.getActivity(
                context,
                0,
                resendIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(notifyIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
