package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountArrayAdapter;
import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.service.SendEmailService;

/**
 * Application's default Activity. Presents two EditText, one for the subject and one for the body
 * of the email. The Activity also has a navigation drawer that allows the user to switch between
 * email accounts.
 */
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
                final EditText subjectEditText = (EditText) findViewById(R.id.compose_subject);
                final EditText bodyEditText = (EditText) findViewById(R.id.compose_body);
                final String subject = subjectEditText.getText().toString();
                final String body = bodyEditText.getText().toString();

                // Build Email object.
                final Email email = new Email();
                email.setSender(account.name);
                email.addRecipient(account.name);
                email.setSubject(subject);
                email.setBody(body);

                // Start SendEmailService.
                final Intent intent = new Intent(this, SendEmailService.class);
                intent.putExtra(SendEmailService.EMAIL, email);
                startService(intent);

                // Reset text fields.
                subjectEditText.setText("");
                bodyEditText.setText("");
            } else {
                // No supported account found. Show error message.
                final String errorMessage = getResources().getString(R.string.no_supported_account);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * TODO
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
