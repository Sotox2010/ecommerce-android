package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.Property;
import android.view.View;

public class ViewBackgroundColorProperty extends Property<View, Integer> {

    /**
     * A constructor that takes an identifying name and {@link #getType() type} for the property.
     *
     * @param type
     * @param name
     */
    public ViewBackgroundColorProperty(Class<Integer> type, String name) {
        super(type, name);
    }

    @Override
    public Integer get(View view) {
        Drawable backgroundDrawable = view.getBackground();

        return (backgroundDrawable instanceof ColorDrawable)
                ? ((ColorDrawable) backgroundDrawable).getColor()
                : Color.TRANSPARENT;
    }

    @Override
    public void set(View view, @ColorInt Integer color) {
        view.setBackgroundColor(color);
    }
}
