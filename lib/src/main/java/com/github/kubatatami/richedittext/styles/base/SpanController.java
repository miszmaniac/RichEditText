package com.github.kubatatami.richedittext.styles.base;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.EditText;

import com.github.kubatatami.richedittext.modules.StyleSelectionInfo;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SpanController<T> {
    protected Class<T> clazz;
    protected String tagName;

    public final static int defaultFlags = Spanned.SPAN_INCLUSIVE_INCLUSIVE;

    public SpanController(Class<T> clazz, String tagName) {
        this.clazz = clazz;
        this.tagName = tagName;
    }


    public List<T> filter(Object[] spans) {
        List<T> result = new ArrayList<T>();
        for (Object span : spans) {
            if (acceptSpan(span)) {
                result.add((T) span);
            }
        }
        return result;
    }

    public boolean acceptSpan(Object span) {
        return span.getClass().equals(clazz);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public abstract void clearStyle(Editable editable, Object span, StyleSelectionInfo styleSelectionInfo);

    public abstract boolean clearStyles(Editable editable, StyleSelectionInfo styleSelectionInfo);

    public abstract void checkBeforeChange(Editable editable, StyleSelectionInfo styleSelectionInfo, boolean added);

    public abstract void checkAfterChange(EditText editText, StyleSelectionInfo styleSelectionInfo);

    public abstract String beginTag(Object span);

    public abstract Object createSpanFromTag(String tag, Map<String,String> styleMap,Attributes attributes);

    public Class<?> spanFromEndTag(String tag) {
        if(tag.equals(tagName)){
            return clazz;
        }
        return null;
    }

    public String endTag() {
        return "</"+tagName+">";
    }
}