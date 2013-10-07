package com.javatomic.drupal.account;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

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
            holder.accountButton = (TextView) convertView.findViewById(R.id.account_button);

            convertView.setTag(holder);
        } else {
            holder = (AccountHolder) convertView.getTag();
        }

        Account account = mAccounts[position];
        holder.accountButton.setText(account.name);

        if (chosenAccount.equals(account)) {
            holder.accountButton.setTextColor(
                    mContext.getResources().getColor(R.color.holo_blue_dark));
        } else {
            holder.accountButton.setTextColor(Color.DKGRAY);
        }

        return convertView;
    }

    private static class AccountHolder {
        public TextView accountButton;
    }
}
