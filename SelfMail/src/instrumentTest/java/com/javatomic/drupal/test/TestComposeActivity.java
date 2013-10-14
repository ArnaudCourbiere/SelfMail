package com.javatomic.drupal.test;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;

import com.javatomic.drupal.R;
import com.javatomic.drupal.ui.activity.ComposeActivity;

/*
 * Class for Testing the ComposeActivity.
 */
public class TestComposeActivity extends ActivityInstrumentationTestCase2<ComposeActivity> {

    /*
     *  The Activity to test.
     */
    private ComposeActivity mActivity;

    /*
     * The context being tested.
     */
    private Context mTargetContext;

    /*
     * Activity UI components.
     */
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private DrawerLayout mNavDrawer;
    private ListView mAccountList;

    public TestComposeActivity() {
        super("com.javatomic.drupal.ui.activity", ComposeActivity.class);
    }

    @Override
    protected void setUp() {

        // Prepare to send key events to the app under test by turning off touch mode.
        setActivityInitialTouchMode(false);

        mTargetContext = getInstrumentation().getTargetContext();

        // Retrieve activity UI components.
        mActivity = getActivity();
        mSubjectEditText = (EditText) mActivity.findViewById(R.id.compose_subject);
        mBodyEditText = (EditText) mActivity.findViewById(R.id.compose_body);
        mNavDrawer = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        mAccountList = (ListView) mActivity.findViewById(R.id.account_list);
    }

    /*
     * Tests that the test case has been successfully setup.
     */
    public void testPreConditions() {

        // Test that we have the appropriate views.
        assertTrue(mSubjectEditText != null);
        assertTrue(mBodyEditText != null);
        assertTrue(mNavDrawer != null);
        assertTrue(mAccountList != null);
    }

    /*
     * Tests the The activity initial test.
     */
    public void testInitialState() {

        // Test initial EditText contents.
        assertTrue("Subject EditText must initially be empty", TextUtils.isEmpty(mSubjectEditText.getText()));
        assertTrue("Body EditText must initially be empty", TextUtils.isEmpty(mBodyEditText.getText()));

        // Test hint values.
        final CharSequence subjectHint = mTargetContext.getResources().getString(R.string.subject);
        final CharSequence bodyHint = mTargetContext.getResources().getString(R.string.compose);
        assertEquals("Subject hint must be the one from strings resources", subjectHint, mSubjectEditText.getHint());
        assertEquals("Body hint must be the one from strings resources", bodyHint, mBodyEditText.getHint());
    }
}
