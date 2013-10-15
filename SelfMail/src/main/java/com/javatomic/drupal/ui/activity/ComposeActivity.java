package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountArrayAdapter;
import com.javatomic.drupal.account.AccountUtils;

import java.io.IOException;

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
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    getAuthToken();

                    return null;
                }
            };
            task.execute((Void)null);
        }
    }

    private void getAuthToken() {
        final Activity activity = ComposeActivity.this;
        final Account account = AccountUtils.getChosenAccount(activity);

        try {
            final String token = GoogleAuthUtil.getToken(activity, account.name, "oauth2:https://mail.google.com/mail/feed/atom");

            // Do work with token...

            // If  server indicates token is invalid.
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
}
