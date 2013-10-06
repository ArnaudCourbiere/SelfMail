package com.javatomic.drupal.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.javatomic.drupal.R;

public class ComposeActivity extends ActionBarActivity {
    private DrawerLayout mAccountDrawer;
    private ListView mAccountList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_activity);

        String[] dummyStrings = { "One", "Two", "Three" };
        mTitle = mDrawerTitle = getTitle();

        mAccountDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mAccountList = (ListView) findViewById(R.id.account_list);

        // Setup list adapter and click listener.
        mAccountList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_account_item, dummyStrings));
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // If the account drawer is opened, hide action items related to the content view.
        boolean drawerOpen = mAccountDrawer.isDrawerOpen(mAccountList);
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

            // TODO: Handle account change here.

            mAccountList.setItemChecked(position, true);
            //setTitle("New Account Name");
            mAccountDrawer.closeDrawer(mAccountList);
        }
    }
}
