package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.service.SendEmailService;
import com.javatomic.drupal.ui.util.SendEmailAsyncTask;

/**
 * Activity displaying the choice to resend or discard an email that failed to send.
 */
public class ResendActivity extends FragmentActivity {
    private static final String TAG = "ResendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Email email = intent.getParcelableExtra(SendEmailService.EMAIL);

        if (email != null) {
            final DialogFragment newFragment = new ResendDialogFragment(email);
            newFragment.show(getSupportFragmentManager(), "resend");
        } else {
            Toast.makeText(this, getString(R.string.error_resending_selfmail), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class ResendDialogFragment extends DialogFragment {
        private Email mEmail;

        public ResendDialogFragment(Email email) {
            mEmail = email;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.resend_selfmail);
            builder.setPositiveButton(R.string.resend, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: Don't get chosen account type here, the user may have changed it.
                    final Account account = AccountUtils.getChosenAccount(getActivity());
                    SendEmailAsyncTask task = new SendEmailAsyncTask(getActivity(), account) {

                        @Override
                        protected void onPostExecute(Boolean success) {
                            super.onPostExecute(success);
                            finish();
                        }
                    };

                    task.execute(mEmail);
                    Toast.makeText(getActivity(), getString(R.string.sending_selfmail), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            return builder.create();
        }
    }
}
