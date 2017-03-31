package com.smartsecurity.android.piudonnacouture.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.smartsecurity.android.piudonnacouture.ui.widget.ColorPickerButtonDrawable;

import java.text.DecimalFormat;

public class UiUtils {

    private static final MetricAffectingSpan sProductPriceSizeSpan = new RelativeSizeSpan(0.85f);
    private static final DecimalFormat sProductPriceFormatter = new DecimalFormat("'$'#,###.00");

    //    public static Spanned formatPrice(float price, MetricAffectingSpan span) {
    public static Spanned formatPrice(float price) {
        String formattedPrice = sProductPriceFormatter.format(price);

        SpannableString styledString = new SpannableString(formattedPrice);
        styledString.setSpan(sProductPriceSizeSpan,
                formattedPrice.lastIndexOf('.') + 1,  formattedPrice.length(), 0);

        return styledString;
    }

    /*public static Spanned formatPhoneLabel(String phoneStr) {
        SpannableString phone = new SpannableString(phoneStr);
        phone.setSpan(mMediumTypefaceSpan, 0, phone.toString().indexOf(':') + 1, 0);
        mPhoneView.setText(phone);
    }*/

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Get a color value from a theme attribute.
     * @param context used for getting the color.
     * @param attribute theme attribute.
     * @return color value
     */
    @ColorInt
    public static int getThemeColor(Context context, int attribute) {
        return getThemeColor(context, attribute, Color.TRANSPARENT);
    }

    /**
     * Get a color value from a theme attribute.
     * @param context used for getting the color.
     * @param attribute theme attribute.
     * @param defaultValue default color in case the attribute is not found.
     * @return color value
     */
    @ColorInt
    public static int getThemeColor(Context context, int attribute, @ColorInt int defaultValue) {
        int[] attrs = new int[]{attribute};

        TypedArray a = context.obtainStyledAttributes(attrs);
        int color = a.getColor(0, defaultValue);
        a.recycle();

        return color;
    }

    /**
     * Get a drawable from a theme attribute.
     * @param context used for getting the color.
     * @param attribute theme attribute.
     * @return the drawable
     */
    public static Drawable getThemeDrawable(Context context, int attribute) {
        int[] attrs = new int[] {attribute};

        TypedArray a = context.obtainStyledAttributes(attrs);
        Drawable drawable = a.getDrawable(0);
        a.recycle();

        return drawable;
    }

    /**
     * Compat implementation to set a drawable as the background of some view.
     * @param view to set its background.
     * @param background drawable for apply to the view.
     */
    public static void setBackgroundDrawable(View view, Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

    /**
     * Makes radio button styled for a color picker radio group.
     * @param context used to get button dimensions and screen density.
     * @param color the color value that the button will represent.
     * @param tag an object to attach it to the button.
     * @return the built radio button.
     */
    public static RadioButton makeColorCompoundButton(Context context, @ColorInt int color, @Nullable Object tag, @Nullable View.OnLongClickListener longClickListener) {
        int size = (int) (40 * context.getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);

        Drawable checked = new ColorPickerButtonDrawable(context, color, true);
        Drawable normal = new ColorPickerButtonDrawable(context, color);

        StateListDrawable background = new StateListDrawable();
        background.addState(new int[] {android.R.attr.state_checked}, checked);
        background.addState(new int[]{}, normal);

        RadioButton button = new RadioButton(context);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setButtonDrawable(null);
        button.setTag(tag);
        button.setOnLongClickListener(longClickListener);
        UiUtils.setBackgroundDrawable(button, background);

        return button;
    }

    public static int getNavigationBarPixelSize(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }
}
