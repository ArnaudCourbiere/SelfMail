package com.javatomic.drupal.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Base64;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.util.NetworkUtils;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Service used for sending emails. It extends {@link android.app.IntentService} so request to send
 * emails are queued up and delivered one at a time to {@link #onHandleIntent(android.content.Intent)}
 */
public class SendEmailService extends IntentService {
    private static final String TAG = "SendEmailService";

    /**
     * Handle to retrieve the email from the Intent that started the service.
     */
    public static final String EMAIL = "email";

    /**
     * Listener for network connectivity changes.
     */
    private NetworkReceiver mNetworkReceiver;

    /**
     * Default constructor.
     */
    public SendEmailService() {
        super(TAG);
    }

    /**
     * Sets up a {@link com.javatomic.drupal.service.SendEmailService.NetworkReceiver} upon creation
     * of the service.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver();
        this.registerReceiver(mNetworkReceiver, filter);
    }

    /**
     * Unregister the {@link com.javatomic.drupal.service.SendEmailService.NetworkReceiver} when
     * the service is destroyed.
     */
    @Override
    public void onDestroy() {
        if (mNetworkReceiver != null) {
            this.unregisterReceiver(mNetworkReceiver);
        }

        super.onDestroy();
    }

    /**
     * Sends the email.
     *
     * @param intent The Intent supplied to {@link android.app.IntentService#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Wait until the network is connected.
        synchronized (this) {
            while(!NetworkUtils.isNetworkConnected(this)) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    // TODO
                    LOGE(TAG, e.toString(), e);
                }
            }
        }

        // Retrieve email.
        final Email email = intent.getParcelableExtra(EMAIL);
        final String host = "smtp.gmail.com";
        final int port = 587;
        final String userEmail = email.getSender();
        String token = null;

        // Get auth token.
        try {
            // Build intent for resending email if user action is required to get the auth token.
            final Intent callback = new Intent(this, SendEmailService.class);
            callback.putExtra(SendEmailService.EMAIL, email);

            token = GoogleAuthUtil.getTokenWithNotification(this, userEmail, "oauth2:https://mail.google.com/", null, callback);

            // If  server indicates token is invalid.
            if (false) {

                // Invalidate token so it won't be returned next time.
                GoogleAuthUtil.invalidateToken(this, token);
            }
        } catch (UserRecoverableNotifiedException e) {
            LOGD(TAG, e.toString(), e);
            // Notification has already been pushed, stop service.
            //stopSelf();
        } catch (IOException e) {
            LOGE(TAG, e.toString(), e);
            // Network or server error, should retry but not immediately.
        } catch (GoogleAuthException e) {
            LOGE(TAG, e.toString(), e);
            // Failure, call is not expected to ever succeed. It should not be retried.
        }

        // Send email.
        AuthenticatingSMTPClient client = null;

        try {
            client = new AuthenticatingSMTPClient();
            client.setDefaultTimeout(10 * 1000);
            client.connect(host, port);
            client.ehlo("localhost");

            if (client.execTLS()) {
                byte[] response = String.format(
                        "user=%s\u0001auth=Bearer %s\u0001\u0001", userEmail, token).getBytes("utf-8");
                String xoauthArg = Base64.encodeToString(response, Base64.NO_WRAP);
                int code = client.sendCommand("AUTH XOAUTH2", xoauthArg);

                if (code != 235) {
                    LOGD(TAG, "Invalid response code, returning");
                    return;
                }

                // TODO: check response code.

                client.setSender(userEmail);
                client.addRecipient(userEmail);
                Writer writer = client.sendMessageData();

                if (writer != null) {
                    SimpleSMTPHeader header = new SimpleSMTPHeader(userEmail, userEmail, email.getSubject());
                    writer.write(header.toString());
                    writer.write(email.getBody());
                    writer.close();

                    if (!client.completePendingCommand()) {
                        throw new RuntimeException("Failed to send email " + client.getReply() + client.getReplyString());
                    }
                } else {
                    throw new RuntimeException("Failed to send email (didn't get writer) " + client.getReply() + client.getReplyString());
                }
            } else {
                throw new RuntimeException("STARTTLS was not accepted " + client.getReply() + client.getReplyString());
            }
        } catch (NoSuchAlgorithmException e) {

        } catch (IOException e) {

        } finally {
            if (client != null) {
                try {
                    client.logout();
                    client.disconnect();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * Listens for network state changes.
     */
    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(context)) {
                synchronized (SendEmailService.this) {
                    SendEmailService.this.notifyAll();
                }
            }
        }
    }
}
