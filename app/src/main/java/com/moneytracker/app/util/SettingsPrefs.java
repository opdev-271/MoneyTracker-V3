package com.moneytracker.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Small app-wide flags that live outside Room (language is handled by LocaleHelper). */
public class SettingsPrefs {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private static final String KEY_ACTIVE_ACCOUNT_ID = "active_account_id";

    private SettingsPrefs() {
    }

    public static boolean isBalanceHidden(Context context) {
        return prefs(context).getBoolean(KEY_HIDE_BALANCE, false);
    }

    public static void setBalanceHidden(Context context, boolean hidden) {
        prefs(context).edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }

    public static long getActiveAccountId(Context context) {
        return prefs(context).getLong(KEY_ACTIVE_ACCOUNT_ID, -1L);
    }

    public static void setActiveAccountId(Context context, long accountId) {
        prefs(context).edit().putLong(KEY_ACTIVE_ACCOUNT_ID, accountId).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
