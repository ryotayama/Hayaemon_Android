package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.util.AttributeSet;

public class HighlightImageButton extends android.support.v7.widget.AppCompatImageButton {
    public HighlightImageButton(Context context, AttributeSet attr)
    {
        super(context, attr);
    }

    @Override
    public void setPressed(boolean pressed)
    {
        if(pressed) setAlpha(0.4f);
        else setAlpha(1.0f);
        super.setPressed(pressed);
    }
}
