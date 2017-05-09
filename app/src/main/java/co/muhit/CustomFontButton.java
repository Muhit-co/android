package co.muhit;

import android.content.Context;
import android.util.AttributeSet;

/**
 * See also: https://github.com/fs-opensource/android-tutorials-customfont
 */
public class CustomFontButton extends android.support.v7.widget.AppCompatButton {

    public CustomFontButton(Context context) {
        super(context);

        CustomFontUtils.applyCustomFont(this, context, null);
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        CustomFontUtils.applyCustomFont(this, context, attrs);
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        CustomFontUtils.applyCustomFont(this, context, attrs);
    }
}