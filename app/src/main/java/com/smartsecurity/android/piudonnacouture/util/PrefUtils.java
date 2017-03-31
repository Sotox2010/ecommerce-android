package com.smartsecurity.android.piudonnacouture.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    private static final String PREF_FIRST_LAUNCH = "pref_first_launch";

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isFirstLaunch(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_FIRST_LAUNCH, true);
    }

    public static void markFirstLaunchDone(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply();
    }

}
