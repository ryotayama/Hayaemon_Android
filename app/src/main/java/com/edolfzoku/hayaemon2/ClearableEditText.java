package com.edolfzoku.hayaemon2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ClearableEditText extends RelativeLayout {
    private EditText editText;

    public ClearableEditText(final Context context) {
        super(context);

        editText = new EditText (context);
        RelativeLayout.LayoutParams paramEditText = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramEditText.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramEditText.addRule(RelativeLayout.CENTER_VERTICAL);
        paramEditText.leftMargin = (int) (8 * context.getResources().getDisplayMetrics().density);
        paramEditText.rightMargin = (int) (8 * context.getResources().getDisplayMetrics().density);
        editText.setHintTextColor(Color.argb(255, 192, 192, 192));
        editText.setMaxLines(1);
        editText.setHorizontallyScrolling(true);
        editText.setSingleLine(true);
        final ImageView imgClear = new ImageView(context);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.isFocused()) {
                    if(editText.getText().length() == 0) {
                        imgClear.setVisibility(View.INVISIBLE);
                        editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingLeft(), editText.getPaddingBottom());
                    }
                    else {
                        imgClear.setVisibility(View.VISIBLE);
                        editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingLeft() + (int) (24 * context.getResources().getDisplayMetrics().density), editText.getPaddingBottom());
                    }
                }
            }
        });
        addView(editText, paramEditText);

        imgClear.setImageResource(R.drawable.ic_button_clear);
        imgClear.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FFCCCCCC"), PorterDuff.Mode.SRC_IN));
        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().clear();
            }
        });
        imgClear.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams paramImgClear = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramImgClear.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        paramImgClear.addRule(RelativeLayout.CENTER_VERTICAL);
        paramImgClear.rightMargin = (int) (16 * context.getResources().getDisplayMetrics().density);
        addView(imgClear, paramImgClear);

        setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean b)
            {
                if(b) editText.requestFocus();
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    imgClear.setVisibility(View.INVISIBLE);
                    editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingLeft(), editText.getPaddingBottom());
                    editText.setSelection(0);
                }
                else if(editText.getText().length() == 0) {
                    imgClear.setVisibility(View.INVISIBLE);
                    editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingLeft(), editText.getPaddingBottom());
                }
                else {
                    imgClear.setVisibility(View.VISIBLE);
                    editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingLeft() + (int) (24 * context.getResources().getDisplayMetrics().density), editText.getPaddingBottom());
                    editText.setSelection(editText.getText().toString().length());
                }
            }
        });
    }

    public void setHint(CharSequence hint) {
        editText.setHint(hint);
    }

    public void setHint(int resid) {
        editText.setHint(resid);
    }

    public void setText(int resid) {
        editText.setText(resid);
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    public Editable getText() {
        return editText.getText();
    }

    public void setSelection(int length) {
        editText.setSelection(length);
    }
}
