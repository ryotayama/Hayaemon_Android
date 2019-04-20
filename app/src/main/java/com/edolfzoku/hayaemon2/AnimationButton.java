package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class AnimationButton extends android.support.v7.widget.AppCompatImageButton
{
    private boolean mAnimationFlag = true;

    public void setAnimation(boolean animationFlag) { mAnimationFlag = animationFlag; }

    public AnimationButton(Context context, AttributeSet attr)
    {
        super(context, attr);
    }

    @Override
    public void setPressed(boolean pressed)
    {
        if(!mAnimationFlag) return;

        if (pressed) {
            /* 押してる時 */
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f / 1.0f, 1.0f, 0.9f / 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(80);
            scaleAnimation.setRepeatCount(0);
            scaleAnimation.setFillAfter(true);
            this.startAnimation(scaleAnimation);
        } else {
            /* 放した時 */
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.9f / 1.0f, 1.0f, 0.9f / 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(80);
            scaleAnimation.setRepeatCount(0);
            scaleAnimation.setFillAfter(true);
            this.startAnimation(scaleAnimation);
        }
        super.setPressed(pressed);
    }
}
