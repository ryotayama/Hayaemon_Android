package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.R.attr;

import org.jcodec.api.NotImplementedException;

/**
 * ライト/ダークテーマが反映されるダイアログ
 */
public class LightDarkDialogBuilder extends AlertDialog.Builder {
    public LightDarkDialogBuilder(@NonNull MainActivity sActivity, @StringRes int titleId) {
        super(sActivity, sActivity.isDarkMode() ? R.style.DarkModeDialog : LightDarkDialogBuilder.resolveDialogTheme(sActivity, 0));
        setTitle(titleId);
    }

    public LightDarkDialogBuilder(@NonNull MainActivity sActivity, String title) {
        super(sActivity, sActivity.isDarkMode() ? R.style.DarkModeDialog : LightDarkDialogBuilder.resolveDialogTheme(sActivity, 0));
        setTitle(title);
    }

    /**
     * AlertDialog.resolveDialogTheme のコピペ
     */
    static int resolveDialogTheme(@NonNull Context context, @StyleRes int resId) {
        if ((resId >>> 24 & 255) >= 1) {
            return resId;
        } else {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(attr.alertDialogTheme, outValue, true);
            return outValue.resourceId;
        }
    }
}
