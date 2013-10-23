package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Dialog;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountArrayAdapter;
import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.net.NetworkReceiver;
import com.javatomic.drupal.service.SendEmailService;

import java.io.IOException;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Application's default Activity. Presents two EditText, one for the subject and one for the body
 * of the email. The Activity also has a navigation drawer that allows the user to switch between
 * email accounts.
 */
public class ComposeActivity extends ActionBarActivity {
    private static final String TAG = "ComposeActivity";

    private static final int INSTALL_PLAY_SERVICES_REQUEST = 1000;
    private static final int GET_AUTH_TOKEN_REQUEST = 2000;

    /** Listener for network connectivity changes. */
    private NetworkReceiver mNetworkReceiver;

    /** Listener for the account drawer open and close event. */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mAccountDrawer;
    private ListView mAccountList;

    private Account[] mAccounts;
    private AccountArrayAdapter mAccountAdapter;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    private EditText mSubjectEditText;
    private EditText mBodyEditText;

    /**
     * Initializes the Activity.
     *
     * @param savedInstanceState If the {@link Activity} is being re-initialized after being
     *     shut downData, this {@link Bundle} contains the data most recently supplied in
     *     {@link #onSaveInstanceState(android.os.Bundle)}, it is null otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeLayout();
        initializeListeners();
    }

    /**
     * Sets the activity content view layout and initializes the layout components.
     */
    private void initializeLayout() {
        setContentView(R.layout.compose_activity);

        final Account chosenAccount = AccountUtils.getChosenAccount(this);

        if (chosenAccount != null) {
            setTitle(chosenAccount.name);
        }

        mDrawerTitle = getResources().getString(R.string.choose_account);

        mAccountDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mAccountDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mAccounts = AccountUtils.getAvailableAccounts(this);
        mAccountAdapter = new AccountArrayAdapter(this, R.layout.drawer_account_item, mAccounts);
        mAccountList = (ListView) findViewById(R.id.account_list);
        mAccountList.setAdapter(mAccountAdapter);

        mSubjectEditText = (EditText) findViewById(R.id.compose_subject);
        mBodyEditText = (EditText) findViewById(R.id.compose_body);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Creates and registers the Activity's listeners.
     */
    private void initializeListeners() {
        // Register listener for network state changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver(this);
        this.registerReceiver(mNetworkReceiver, filter);


        // Setup the account list click listener.
        mAccountList.setOnItemClickListener(new AccountClickListener());

        // Listen for open and close events on the account drawer.
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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    /**
     * Unregister the {@link NetworkReceiver} when the {@link Activity} is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNetworkReceiver != null) {
            this.unregisterReceiver(mNetworkReceiver);
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

    /**
     * Called when the user either has installed Google Play Services or allowed SelfMail authorization
     * to access its email account.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data Data returned to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            LOGD(TAG, "User canceled operation, request code: " + requestCode);
        } else {
            switch (requestCode) {
                case INSTALL_PLAY_SERVICES_REQUEST:
                case GET_AUTH_TOKEN_REQUEST:
                    this.sendSelfMail();
                    break;
            }
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
                        statusCode, ComposeActivity.this, INSTALL_PLAY_SERVICES_REQUEST);

                if (alert == null) {
                    final String errorMessage = getResources().getString(R.string.incompatible_google_play);
                    Toast.makeText(ComposeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
            final Account account = AccountUtils.getChosenAccount(this);

            if (account != null) {
                // Retrieve text.
                final String subject = mSubjectEditText.getText().toString();
                final String body = mBodyEditText.getText().toString();

                // Build Email object.
                final Email email = new Email();
                email.setSender(account.name);
                email.addRecipient(account.name);
                email.setSubject(subject);
                email.setBody(body);

                // Get auth token in worker thread.
                AsyncTask<Email, Void, Void> task = new AsyncTask<Email, Void, Void>() {

                    @Override
                    protected Void doInBackground(Email... params) {
                        if (params.length == 1) {
                            try {
                                mNetworkReceiver.waitForNetwork();
                                getAuthToken(params[0]);
                            } catch (InterruptedException e) {
                                LOGE(TAG, e.toString(), e);
                            }
                        } else {
                            LOGW(TAG, "Background task can only execute one email, " +
                                    "multiple emails passed to the call to execute()");
                        }

                        return null;
                    }
                };
                task.execute(email);

            } else {
                // No supported account found. Show error message.
                final String errorMessage = getResources().getString(R.string.no_supported_account);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getAuthToken(Email email) {
        try {
            final String token = GoogleAuthUtil.getToken(this, email.getSender(), "oauth2:https://mail.google.com/");

            // Reset text fields.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubjectEditText.setText("");
                    mBodyEditText.setText("");
                }
            });

            // Start SendEmailService.
            final Intent intent = new Intent(this, SendEmailService.class);
            intent.putExtra(SendEmailService.EMAIL, email);
            intent.putExtra(SendEmailService.AUTH_TOKEN, token);
            startService(intent);

            // If  server indicates token is invalid.
            if (false) {

                // Invalidate token so it won't be returned next time.
                GoogleAuthUtil.invalidateToken(this, token);
            }
        } catch (final GooglePlayServicesAvailabilityException e) {
            showGooglePlayServicesDialog(e.getConnectionStatusCode());
        } catch (UserRecoverableAuthException e) {
            // Start the user recoverable action using the intent returned by getIntent()
            this.startActivityForResult(e.getIntent(), GET_AUTH_TOKEN_REQUEST);
        } catch (IOException e) {
            // Network or server error, should retry but not immediately.
            LOGW(TAG, e.toString(), e);
        } catch (GoogleAuthException e) {
            // Failure, call is not expected to ever succeed. It should not be retried.
            LOGE(TAG, e.toString(), e);
        }
    }

    /**
     * Listens for clicks in the navigation drawers. Sets the chosen account and updates the
     * navigation drawer.
     */
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
}
