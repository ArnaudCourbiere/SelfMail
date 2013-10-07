package com.javatomic.drupal.account;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import com.javatomic.drupal.R;

public class AccountArrayAdapter extends ArrayAdapter<Account> {
    private Context mContext;
    private int mLayoutResourceId;
    private Account[] mAccounts;

    public AccountArrayAdapter(Context context, int layoutResourceId, Account[] accounts) {
        super(context, layoutResourceId, accounts);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mAccounts = accounts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountHolder holder;
        Account chosenAccount = AccountUtils.getChosenAccount(mContext);

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new AccountHolder();
            holder.accountButton = (RadioButton) convertView.findViewById(R.id.account_button);

            convertView.setTag(holder);
        } else {
            holder = (AccountHolder) convertView.getTag();
        }

        Account account = mAccounts[position];
        holder.accountButton.setText(account.name);

        if (chosenAccount.equals(account)) {
            holder.accountButton.setChecked(true);
        }

        return convertView;
    }

    private static class AccountHolder {
        public RadioButton accountButton;
    }
}
