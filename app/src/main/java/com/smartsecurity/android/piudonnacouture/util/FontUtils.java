package com.smartsecurity.android.piudonnacouture.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class FontUtils {

    private static final String LOG_TAG = FontUtils.class.getSimpleName();

    private static final Map<String, Typeface> FONTS = new HashMap<>();

    public static void setTypeface(TextView view, String typefaceName) {
        view.setTypeface(getTypeface(view.getContext(), typefaceName));
    }

    public static Typeface getTypeface(Context context, String typefaceName) {
        Typeface typeface = FONTS.get(typefaceName);
        if (typeface == null) {
            try{
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + typefaceName);
                FONTS.put(typefaceName, typeface);
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "Typeface \"" + typefaceName + "\" not found.");
            }
        }

        return typeface;
    }
}
