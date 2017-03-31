package com.smartsecurity.android.piudonnacouture.util;


import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class AnimUtils {

    /**
     * Common animation interpolator.
     */
    private static Interpolator sFastOutSlowIn;
    private static Interpolator sFastOutLinearIn;
    private static Interpolator sLinearOutSlowIn;
    private static Interpolator sDecelerate;

    public static Interpolator getFastOutSlowInInterpolator() {
        if (sFastOutSlowIn == null) {
            sFastOutSlowIn = new FastOutSlowInInterpolator();
        }

        return sFastOutSlowIn;
    }

    public static Interpolator getFastOutLinearInInterpolator() {
        if (sFastOutLinearIn == null) {
            sFastOutLinearIn = new FastOutLinearInInterpolator();
        }

        return sFastOutLinearIn;
    }

    public static Interpolator getLinearOutSlowInInterpolator() {
        if (sLinearOutSlowIn == null) {
            sLinearOutSlowIn = new LinearOutSlowInInterpolator();
        }

        return sLinearOutSlowIn;
    }

    public static Interpolator getDecelerateInterpolator() {
        if (sDecelerate == null) {
            sDecelerate = new DecelerateInterpolator(2f);
        }

        return sDecelerate;
    }

}
