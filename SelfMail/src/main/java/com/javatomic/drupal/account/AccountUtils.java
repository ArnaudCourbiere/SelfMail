package com.javatomic.drupal.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * Utilities to manipulate accounts.
 */
public class AccountUtils {
    private static final String TAG = "AccountUtils";

    /**
     * Google account type identifier.
     */
    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    /**
     * Preference key to store the currently chosen account name.
     */
    public static final String PREF_CHOSEN_ACCOUNT_NAME = "chosen_account_name";

    /**
     * Preference key to store the currently chosen account type.
     */
    public static final String PREF_CHOSEN_ACCOUNT_TYPE = "chosen_account_type";

    /**
     * Retrieves the currently chosen account.
     *
     * @param context Context used to access the shared preferences.
     * @return The chosen account, or null if no account is found.
     */
    public static Account getChosenAccount(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String accountName = sp.getString(PREF_CHOSEN_ACCOUNT_NAME, null);
        final String accountType = sp.getString(PREF_CHOSEN_ACCOUNT_TYPE, null);

        if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
            return new Account(accountName, accountType);
        } else {
            final Account[] accounts = getAvailableAccounts(context);
            final Account account = accounts.length > 0 ? accounts[0] : null;

            if (account != null) {
                setChosenAccount(context, account);
            }

            return account;
        }
    }

    /**
     * Sets the chosen account.
     *
     * @param context Context used to access the shared preferences.
     * @param account The chosen account.
     * @return True if the chosen account was successfully written to persistent storage.
     */
    public static boolean setChosenAccount(Context context, Account account) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sp.edit();

        editor.putString(PREF_CHOSEN_ACCOUNT_NAME, account.name);
        editor.putString(PREF_CHOSEN_ACCOUNT_TYPE, account.type);

        return editor.commit();
    }

    /**
     * Retrieves all supported accounts found on the device.
     *
     * @param context Context the utils is running within.
     * @return accounts Supported accounts found on the device.
     */
    public static Account[] getAvailableAccounts(Context context) {
        final AccountManager am = AccountManager.get(context);
        final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE_GOOGLE);

        return accounts;
    }
}
