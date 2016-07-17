package com.wouterdevos.fallingwords;

import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FallingWord {

    @IntDef({STATUS_PENDING, STATUS_FALLING, STATUS_PAUSED, STATUS_COMPLETED, STATUS_REMOVED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_FALLING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_REMOVED = 4;

    public ValueAnimator animator;
    public WordPair wordPair;
    public Rect bounds;
    public Point position;
    @Status
    public int status;

    public FallingWord(WordPair wordPair, Point position) {
        this.wordPair = new WordPair(wordPair);
        this.bounds = new Rect();
        this.position = new Point(position);
        this.status = STATUS_PENDING;
    }

    public void setBounds(Paint paint) {
        paint.getTextBounds(wordPair.textSpanish, 0, wordPair.textSpanish.length(), bounds);
    }
}
