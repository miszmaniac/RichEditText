package com.github.kubatatami.richedittext.styles.base;

import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;

import com.github.kubatatami.richedittext.BaseRichEditText;
import com.github.kubatatami.richedittext.modules.StyleSelectionInfo;

import java.util.List;

/**
 * Created by Kuba on 12/11/14.
 */
public abstract class MultiStyleController<T, Z> extends SpanController<T> {

    protected Z value;
    protected SpanInfo<Z> spanInfo;
    protected BaseRichEditText.OnValueChangeListener<Z> onValueChangeListener;

    public MultiStyleController(Class<T> clazz) {
        super(clazz);
    }

    public abstract Z getValueFromSpan(T span);

    public String getDebugValueFromSpan(T span) {
        return getValueFromSpan(span).toString();
    }

    public abstract T add(Z value, Editable editable, int selectionStart, int selectionEnd, int flags);

    public void add(Z value, Editable editable, int selectionStart, int selectionEnd) {
        add(value, editable, selectionStart, selectionEnd, defaultFlags);
    }

    public void setOnValueChangeListener(BaseRichEditText.OnValueChangeListener<Z> onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    public boolean perform(Z value, Editable editable, StyleSelectionInfo styleSelectionInfo) {
        boolean result = clearStyles(editable, styleSelectionInfo);
        result = selectStyle(value, editable, styleSelectionInfo) || result;
        this.value = value;
        return result;
    }

    public boolean selectStyle(Z value, Editable editable, StyleSelectionInfo styleSelectionInfo) {
        if (styleSelectionInfo.selectionStart == styleSelectionInfo.selectionEnd) {
            spanInfo = new SpanInfo<Z>(styleSelectionInfo.selectionStart, styleSelectionInfo.selectionEnd, defaultFlags, value);
            return false;
        } else {
            int finalSpanStart = styleSelectionInfo.selectionStart;
            int finalSpanEnd = styleSelectionInfo.selectionEnd;
            for (Object span : filter(editable.getSpans(styleSelectionInfo.selectionStart, styleSelectionInfo.selectionEnd, getClazz()))) {
                int spanStart = editable.getSpanStart(span);
                int spanEnd = editable.getSpanEnd(span);
                if (spanStart < finalSpanStart) {
                    finalSpanStart = spanStart;
                }
                if (spanEnd > finalSpanEnd) {
                    finalSpanEnd = spanEnd;
                }
                editable.removeSpan(span);
            }
            add(value, editable, finalSpanStart, finalSpanEnd);
            return true;
        }
    }

    @Override
    public void clearStyle(Editable editable, Object span, StyleSelectionInfo styleSelectionInfo) {
        int spanStart = editable.getSpanStart(span);
        int spanEnd = editable.getSpanEnd(span);
        if (spanStart >= styleSelectionInfo.selectionStart && spanEnd <= styleSelectionInfo.selectionEnd) {
            editable.removeSpan(span);
        } else if (spanStart < styleSelectionInfo.selectionStart && spanEnd <= styleSelectionInfo.selectionEnd) {
            editable.removeSpan(span);
            add(getValueFromSpan((T) span), editable, spanStart, styleSelectionInfo.selectionStart);
        } else if (spanStart >= styleSelectionInfo.selectionStart && spanEnd > styleSelectionInfo.selectionEnd) {
            editable.removeSpan(span);
            add(getValueFromSpan((T) span), editable, styleSelectionInfo.selectionEnd, spanEnd);
        } else {
            editable.removeSpan(span);
            add(getValueFromSpan((T) span), editable, spanStart, styleSelectionInfo.selectionStart);
            add(getValueFromSpan((T) span), editable, styleSelectionInfo.selectionEnd, spanEnd);
        }
    }

    @Override
    public boolean clearStyles(Editable editable, StyleSelectionInfo styleSelectionInfo) {
        if (styleSelectionInfo.selectionStart != styleSelectionInfo.selectionEnd) {
            for (T span : filter(editable.getSpans(styleSelectionInfo.selectionStart, styleSelectionInfo.selectionEnd, getClazz()))) {
                clearStyle(editable, span, styleSelectionInfo);
            }
            return true;
        }
        return false;
    }


    @Override
    public void checkAfterChange(EditText editText, StyleSelectionInfo styleSelectionInfo) {
        T[] spans = editText.getText().getSpans(styleSelectionInfo.realSelectionStart, styleSelectionInfo.realSelectionEnd, getClazz());
        Z newValue = spans.length > 0 ? getValueFromSpan(spans[0]) : getDefaultValue(editText);
        if (spans.length > 2) {
            newValue = getMultiValue();
        } else if (spans.length > 1 && styleSelectionInfo.realSelectionStart == styleSelectionInfo.realSelectionEnd) {
            if (editText.getText().getSpanFlags(spans[0]) == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                newValue = getValueFromSpan(spans[1]);
            } else {
                newValue = getValueFromSpan(spans[0]);
            }
        }

        if ((newValue == null && value!=null) || (newValue != null && value==null) || (value!=null && !newValue.equals(value))) {
            value = newValue;
            if (onValueChangeListener != null) {
                onValueChangeListener.onValueChange(value);
            }
        }
        if (spanInfo != null) {
            add(spanInfo.span, editText.getText(), spanInfo.start, Math.min(spanInfo.end, editText.getText().length()), spanInfo.flags);
        }
        spanInfo = null;

    }

    @Override
    public void checkBeforeChange(final Editable editable, StyleSelectionInfo styleSelectionInfo) {
        if (spanInfo != null && styleSelectionInfo.selectionStart == styleSelectionInfo.selectionEnd
                && spanInfo.start == styleSelectionInfo.selectionStart) {
            List<T> spans = filter(editable.getSpans(styleSelectionInfo.selectionStart, styleSelectionInfo.selectionEnd, getClazz()));
            add(spanInfo.span, editable, spanInfo.start, spanInfo.end, spanInfo.flags);
            spanInfo = null;
            if (spans.size() > 0) {
                final T span = spans.get(0);
                final int spanStart = editable.getSpanStart(span);
                final int spanEnd = editable.getSpanEnd(span);
                editable.removeSpan(span);
                if (styleSelectionInfo.selectionStart != 0 && styleSelectionInfo.selectionEnd == styleSelectionInfo.realSelectionEnd) {
                    spanInfo = new SpanInfo<Z>(spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, getValueFromSpan(span));
                }
            }
        }
    }


    public abstract String beginValueTag(Z value);

    public abstract Z getDefaultValue(EditText editText);

    protected abstract Z getMultiValue();

}
