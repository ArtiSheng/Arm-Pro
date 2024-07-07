/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.material.textfield.TextInputEditText;

public class XEditText extends TextInputEditText {
    public XEditText(Context context) {
        super(context);
    }

    public XEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }
}

