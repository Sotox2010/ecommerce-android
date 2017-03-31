package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.util.FontUtils;

public class FontTextView extends TextView {

    private static final String LOG_TAG = FontTextView.class.getSimpleName();

    public FontTextView(Context context) {
        this(context, null, 0);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.FontView, defStyleAttr, 0);

            String typefaceName = array.getString(R.styleable.FontView_typeface);
            if (typefaceName != null) {
                setTypeface(context, typefaceName);
            }

            array.recycle();
        }
    }

    public void setTypeface(Context context, String typefaceName) {
        setTypeface(FontUtils.getTypeface(context, typefaceName));
    }
}
