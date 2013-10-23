package com.javatomic.drupal.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Base64;

import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.net.NetworkReceiver;

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
     * Handle to retrieve the email from the Intent that started the service.
     */
    public static final String AUTH_TOKEN = "auth_token";

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
     * Sets up a {@link NetworkReceiver} upon creation
     * of the service.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver(this);
        this.registerReceiver(mNetworkReceiver, filter);
    }

    /**
     * Unregister the {@link NetworkReceiver} when
     * the service is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        LOGD(TAG, "unregistering network receiver");
        if (mNetworkReceiver != null) {
            this.unregisterReceiver(mNetworkReceiver);
        }
    }

    /**
     * Sends the email.
     *
     * @param intent The Intent supplied to {@link android.app.IntentService#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Wait until the network is connected.
        try {
            mNetworkReceiver.waitForNetwork();
        } catch (InterruptedException e) {
            // TODO handle
            LOGE(TAG, e.toString(), e);
            return;
        }

        // Retrieve email and auth token.
        final String token = intent.getStringExtra(AUTH_TOKEN);
        final Email email = intent.getParcelableExtra(EMAIL);
        final String host = "smtp.gmail.com";
        final int port = 587;
        final String userEmail = email.getSender();

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
}
