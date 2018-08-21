package com.example.xyzreader2.util;

import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.widget.TextView;


public class AppendTextScrollHandler implements NestedScrollView.OnScrollChangeListener {

    public static final int TEXT_SEGMENT_SIZE = 2500;
    private int priorScrollY;
    private int priorSegmentEndIndex;
    private boolean moreTextAvailable;
    private String fullTextContent;
    private TextView textView;
    private static final String TAG = AppendTextScrollHandler.class.getSimpleName();

    public AppendTextScrollHandler(@NonNull TextView textView, @NonNull String fullContent) {
        this.textView = textView;
        this.fullTextContent = fullContent;
        this.priorSegmentEndIndex = TEXT_SEGMENT_SIZE; // assume the first segment of text was already loaded so this jumps to start of next segment
        if (fullContent.length() < TEXT_SEGMENT_SIZE) {
            this.moreTextAvailable = false;
            Log.e(TAG, "No need for this listener when there is no more text available.");
            return;
        }
        this.moreTextAvailable = true;
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        int SCROLL_TRIGGER_SIZE = 500;
        if ( (scrollY > (priorScrollY + SCROLL_TRIGGER_SIZE)) && moreTextAvailable) { // identify when scroll is beyond prior scroll by at least the trigger value
            Log.d(TAG, "OnScrollChangeListener - Y=" + String.valueOf(scrollY) + " vs. PriorY=" + priorScrollY);
            textView.append(getMoreArticleText());
            priorScrollY = scrollY; // remember for next time
        }
    }

    private String getMoreArticleText() {
        if (!moreTextAvailable) return "";
        String result;
        if (priorSegmentEndIndex + TEXT_SEGMENT_SIZE > fullTextContent.length()) {
            // return the remaining substring
            result = fullTextContent.substring(priorSegmentEndIndex, (fullTextContent.length() - 1));
            priorSegmentEndIndex = priorSegmentEndIndex + result.length();
            moreTextAvailable = false;
        } else {
            result = fullTextContent.substring(priorSegmentEndIndex, (priorSegmentEndIndex + TEXT_SEGMENT_SIZE));
            priorSegmentEndIndex = priorSegmentEndIndex + TEXT_SEGMENT_SIZE;
        }
        return result;
    }
}
