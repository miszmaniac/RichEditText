package com.github.kubatatami.richedittext.styles.binary;

import android.graphics.Typeface;

/**
 * Created by Kuba on 19/11/14.
 */
public class ItalicSpanController extends StyleSpanController {
    public ItalicSpanController() {
        super(Typeface.ITALIC);
    }

    @Override
    public String beginTag(Object span) {
        return "<i>";
    }

    @Override
    public String endTag() {
        return "</i>";
    }
}