package com.javatomic.drupal.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.javatomic.drupal.mail.Email;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class SendEmailService extends IntentService {
    private static final String TAG = "SendEmailService";

    /**
     * Handle.
     */
    public static final String EMAIL = "email";

    /**
     * Handle.
     */
    public static final String AUTH_TOKEN = "auth_token";

    /**
     * Default constructor.
     */
    public SendEmailService() {
        super(TAG);
    }

    /**
     * Retrieves and send the email from the intent that started the service.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        final Email email = intent.getParcelableExtra(EMAIL);
        final String authToken = intent.getStringExtra(AUTH_TOKEN);
        final String host = "smtp.gmail.com";
        final int port = 587;
        final String userEmail = email.getSender();

        AuthenticatingSMTPClient client = null;

        try {
            client = new AuthenticatingSMTPClient();
            client.setDefaultTimeout(10 * 1000);
            client.connect(host, port);
            client.ehlo("localhost");

            if (client.execTLS()) {
                byte[] response = String.format(
                        "user=%s\u0001auth=Bearer %s\u0001\u0001", userEmail, authToken).getBytes("utf-8");
                String xoauthArg = Base64.encodeToString(response, Base64.NO_WRAP);
                int code = client.sendCommand("AUTH XOAUTH2", xoauthArg);

                if (code != 235) {
                    Log.d(TAG, "Invalid response code, returning");
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
