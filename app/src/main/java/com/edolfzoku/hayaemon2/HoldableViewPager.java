/*
 * HoldableViewPager
 *
 * Copyright (c) 2018 Ryota Yamauchi. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.edolfzoku.hayaemon2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yamauchiryouta on 2017/08/06.
 */

public class HoldableViewPager extends ViewPager {

    // スワイプの禁止フラグ(true: スワイプ禁止, false: スワイプOK)
    private boolean mSwipeHold = false;

    public void setSwipeHold(boolean enabled) {
        mSwipeHold = enabled;
    }

    // コンストラクタ
    public HoldableViewPager(Context context) {
        super(context);
    }

    // コンストラクタ
    public HoldableViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(mSwipeHold) {
            // スワイプ禁止の場合
            return false;
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(mSwipeHold) {
            // スワイプ禁止の場合
            return false;
        }

        return super.onInterceptTouchEvent(event);
    }
}
