package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountArrayAdapter;
import com.javatomic.drupal.account.AccountUtils;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ComposeActivity extends ActionBarActivity {
    private static final String TAG = "ComposeActivity";

    private static final int REQUEST_PLAY_SERVICES = 1000;

    private DrawerLayout mAccountDrawer;
    private ListView mAccountList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Account[] mAccounts;
    private AccountArrayAdapter mAccountAdapter;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_activity);

        final Account chosenAccount = AccountUtils.getChosenAccount(this);

        if (chosenAccount != null) {
            setTitle(chosenAccount.name);
        }

        mDrawerTitle = getResources().getString(R.string.choose_account);
        mAccounts = AccountUtils.getAvailableAccounts(this);
        mAccountDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mAccountList = (ListView) findViewById(R.id.account_list);
        mAccountAdapter = new AccountArrayAdapter(this, R.layout.drawer_account_item, mAccounts);

        // Setup list adapter and click listener.
        mAccountList.setAdapter(mAccountAdapter);
        mAccountList.setOnItemClickListener(new AccountClickListener());

        // Listen for open and close events.
        mDrawerToggle = new ActionBarDrawerToggle(this, mAccountDrawer,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);

                // Calls onPrepareOptionsMenu()
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View view) {
                getSupportActionBar().setTitle(mDrawerTitle);

                // Calls onPrepareOptionsMenu()
                supportInvalidateOptionsMenu();
            }
        };

        mAccountDrawer.setDrawerListener(mDrawerToggle);
        mAccountDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // If returns true, drawer has handled app icon touch event.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle other action bar items here.
        switch (item.getItemId()) {
            case R.id.send:
                sendSelfMail();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // If the account drawer is opened, hide action items related to the content view.
        final boolean drawerOpen = mAccountDrawer.isDrawerOpen(mAccountList);
        // menu.findItem(R.id.xxx).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.compose, menu);
        return true;
    }

    /**
     * Sets the action bar title.
     *
     * @param title The new title.
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    private class AccountClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AccountUtils.setChosenAccount(ComposeActivity.this, mAccounts[position]);

            mTitle = mAccounts[position].name;

            mAccountList.setItemChecked(position, true);
            mAccountAdapter.notifyDataSetChanged();
            mAccountDrawer.closeDrawer(mAccountList);
        }
    }

    /**
     * Checks that Google Play Services are available on the device. Shows the dialog to install
     * Google Play Services if they are not available.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionsStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (GooglePlayServicesUtil.isUserRecoverableError(connectionsStatusCode)) {
            showGooglePlayServicesDialog(connectionsStatusCode);

            return false;
        }

        return true;
    }

    /**
     * Show the dialog that prompt the user to install Google Play Services.
     *
     * @param statusCode Status from the UserRecoverableError.
     */
    private void showGooglePlayServicesDialog(final int statusCode) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                        statusCode, ComposeActivity.this, REQUEST_PLAY_SERVICES);

                if (alert == null) {
                    Log.e(TAG, "Incompatible version of Google Play Services");
                }

                alert.show();
            }
        });
    }

    /**
     * Send a SelfMail with the content of the ComposeActivity.
     */
    private void sendSelfMail() {
        final boolean googlePlayServicesAvailable = checkGooglePlayServicesAvailable();

        if (googlePlayServicesAvailable) {
            final String subject = ((EditText) findViewById(R.id.compose_subject)).getText().toString();
            final String body = ((EditText) findViewById(R.id.compose_body)).getText().toString();

            AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

                @Override
                protected Void doInBackground(String... params) {
                    final String subject = params[0];
                    final String body = params[1];
                    final Activity activity = ComposeActivity.this;
                    final Account account = AccountUtils.getChosenAccount(activity);

                    getAuthToken(activity, account, subject, body);

                    return null;
                }
            };
            task.execute(subject, body);
        }
    }

    private void getAuthToken(Activity activity, Account account, String subject, String body) {
        try {
            final String token = GoogleAuthUtil.getToken(activity, account.name, "oauth2:https://mail.google.com/");

            // If  server indicates token is invalid.
            try {
                sendEmail(account, token, subject, body);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }

            if (false) {

                // Invalidate token so it won't be returned next time.
                GoogleAuthUtil.invalidateToken(activity, token);
            }
        } catch (final GooglePlayServicesAvailabilityException e) {
            Log.e(TAG, e.toString(), e);
            showGooglePlayServicesDialog(e.getConnectionStatusCode());
        } catch (UserRecoverableAuthException e) {
            Log.e(TAG, e.toString(), e);
            // Start the user recoverable action using the intent returned by getIntent()
            activity.startActivityForResult(e.getIntent(), 0);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
            // Network or server error, should retry but not immediately.
        } catch (GoogleAuthException e) {
            Log.e(TAG, e.toString(), e);
            // Failure, call is not expected to ever succeed. It should not be retried.
        }
    }

    private void sendEmail(Account account, String token, String subject, String body) throws NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
        final String host = "smtp.gmail.com";
        final int port = 587;
        final String userEmail = account.name;

        AuthenticatingSMTPClient client = new AuthenticatingSMTPClient();

        try {
            client.setDefaultTimeout(10 * 1000);
            client.connect(host, port);
            client.ehlo("localhost");

            if (client.execTLS()) {
                byte[] response = String.format("user=%s\u0001auth=Bearer %s\u0001\u0001", userEmail, token).getBytes("utf-8");
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
                    SimpleSMTPHeader header = new SimpleSMTPHeader(userEmail, userEmail, subject);
                    writer.write(header.toString());
                    writer.write(body);
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
        } finally {
            client.logout();
            client.disconnect();
        }
    }
}
