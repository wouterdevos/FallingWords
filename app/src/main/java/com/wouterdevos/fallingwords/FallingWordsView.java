package com.wouterdevos.fallingwords;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FallingWordsView extends View {

    private static final String TAG = FallingWordsView.class.getSimpleName();

    @IntDef({STATUS_PENDING_ANSWER, STATUS_SELECTED_ANSWER, STATUS_NO_ANSWER, STATUS_LOADING_ROUND, STATUS_PENDING_GAME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    private static final int STATUS_PENDING_ANSWER = 0;
    private static final int STATUS_SELECTED_ANSWER = 1;
    private static final int STATUS_NO_ANSWER = 2;
    private static final int STATUS_LOADING_ROUND = 3;
    private static final int STATUS_PENDING_GAME = 4;

    private static final int MAX_FALLING_WORDS = 4;
    private static final int COUNTER_SIZE = 80;
    private static final int TEST_WORD_SIZE = 60;
    private static final int TEST_WORD_START_Y = 30;
    private static final int FALLING_WORD_SIZE = 50;
    private static final int FALLING_WORD_PADDING = 10;
    private static final int FALLING_WORD_START_Y = 100;
    private static final int MAX_SPAWN_POINTS = 2;
    private static final int BOTTOM_PANEL_HEIGHT = 150;

    private static final long FALLING_WORD_DURATION = 8000;
    private static final long FALLING_WORD_START_DELAY = 1000;
    private static final long BOTTOM_PANEL_DURATION = 500;

    private Paint bottomPanelPaint;
    private Paint counterPaint;
    private Paint testWordPaint;
    private Paint fallingWordPaint;

    private Point testWordPosition;
    private List<Point> spawnPoints;

    private int round = 0;
    private int score = 0;
    private int remainingFallingWords = MAX_FALLING_WORDS;
    private int selectedFallingWordIndex = -1;

    private int bottomPanelColor = Color.LTGRAY;

    private Random random = new Random();

    private FallingWord correctFallingWord;
    private List<WordPair> wordPairs = new ArrayList<>();
    private List<FallingWord> fallingWords = new ArrayList<>();

    private ValueAnimator colorAnimator;

    private int status = STATUS_PENDING_GAME;

    public FallingWordsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialise bottom panel paint.
        bottomPanelPaint = new Paint();

        // Initialise the counter paint.
        counterPaint = new Paint();
        counterPaint.setAntiAlias(true);
        counterPaint.setColor(Color.WHITE);
        counterPaint.setTextAlign(Paint.Align.CENTER);
        counterPaint.setTypeface(Typeface.DEFAULT_BOLD);
        counterPaint.setTextSize(COUNTER_SIZE);

        // Initialise the test word paint.
        testWordPaint = new Paint();
        testWordPaint.setAntiAlias(true);
        testWordPaint.setColor(Color.BLACK);
        testWordPaint.setTextAlign(Paint.Align.CENTER);
        testWordPaint.setTypeface(Typeface.DEFAULT);
        testWordPaint.setTextSize(TEST_WORD_SIZE);

        // Initialise the falling word paint.
        fallingWordPaint = new Paint();
        fallingWordPaint.setAntiAlias(true);
        fallingWordPaint.setColor(Color.BLACK);
        fallingWordPaint.setTextAlign(Paint.Align.CENTER);
        fallingWordPaint.setTypeface(Typeface.DEFAULT);
        fallingWordPaint.setTextSize(FALLING_WORD_SIZE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        testWordPosition = new Point();
        testWordPosition.x = getMeasuredWidth() / 2;
        testWordPosition.y = TEST_WORD_START_Y;

        spawnPoints = new ArrayList<>();

        Point pointA = new Point(getMeasuredWidth() / 4, FALLING_WORD_START_Y);
        Point pointB = new Point(getMeasuredWidth() / 4 * 3, FALLING_WORD_START_Y);

        spawnPoints.add(pointA);
        spawnPoints.add(pointB);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBottomPanel(canvas);
        drawCounter(canvas);
        drawTestWord(canvas);
        drawFallingWords(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                checkFallingWordTouched(x, y);
                break;
        }

        return true;
    }

    private void drawBottomPanel(Canvas canvas) {
        if (status == STATUS_PENDING_GAME) {
            return;
        }

        bottomPanelPaint.setColor(bottomPanelColor);
        canvas.drawRect(0, getMeasuredHeight() - BOTTOM_PANEL_HEIGHT, getMeasuredWidth(), getMeasuredHeight(), bottomPanelPaint);
    }

    private void drawCounter(Canvas canvas) {
        if (status == STATUS_PENDING_GAME) {
            return;
        }

        String scoreText = "" + score;
        Rect bounds = new Rect();
        counterPaint.getTextBounds(scoreText, 0, scoreText.length(), bounds);
        int xPosition = getMeasuredWidth() / 2;
        int yPosition = getMeasuredHeight() - (BOTTOM_PANEL_HEIGHT / 2) + (bounds.height() / 2);
        canvas.drawText(scoreText, xPosition, yPosition, counterPaint);
    }

    private void drawTestWord(Canvas canvas) {
        if (correctFallingWord == null) {
            return;
        }

        String testWordText = correctFallingWord.wordPair.textEnglish;
        canvas.drawText(testWordText, testWordPosition.x, testWordPosition.y + correctFallingWord.bounds.height(), testWordPaint);
    }

    private void drawFallingWords(Canvas canvas) {
        for (FallingWord fallingWord : fallingWords) {
            if (fallingWord.status == FallingWord.STATUS_PENDING || fallingWord.status == FallingWord.STATUS_COMPLETED ||
                    fallingWord.status == FallingWord.STATUS_REMOVED) {
                continue;
            }
            String fallingWordText = fallingWord.wordPair.textSpanish;
            canvas.drawText(fallingWordText, fallingWord.position.x, fallingWord.position.y, fallingWordPaint);
        }
    }

    private void checkFallingWordTouched(float x, float y) {
        selectedFallingWordIndex = -1;
        for (int i = 0; i < fallingWords.size(); i++) {
            FallingWord fallingWord = fallingWords.get(i);
            int halfWidth = fallingWord.bounds.width() / 2;
            int height = fallingWord.bounds.height();

            int left = fallingWord.position.x - halfWidth - FALLING_WORD_PADDING;
            int right = fallingWord.position.x + halfWidth + FALLING_WORD_PADDING;
            int top = fallingWord.position.y - height - FALLING_WORD_PADDING;
            int bottom = fallingWord.position.y + FALLING_WORD_PADDING;

            boolean withinXBounds = x > left  && x < right;
            boolean withinYBounds = y > top && y < bottom;

            if (withinXBounds && withinYBounds) {
                selectedFallingWordIndex = i;
                break;
            }
        }

        if (selectedFallingWordIndex < 0) {
            return;
        }

        FallingWord selectedFallingWord = fallingWords.get(selectedFallingWordIndex);
        String selectedWord = selectedFallingWord.wordPair.textEnglish;
        String correctWord = correctFallingWord.wordPair.textEnglish;
        boolean correct = selectedWord.equals(correctWord);
        if (correct) {
            startCorrectAnimation();
        } else {
            startIncorrectAnimation();
        }
        status = STATUS_SELECTED_ANSWER;
    }

    public void setWordPairs(List<WordPair> wordPairs) {
        this.wordPairs.addAll(wordPairs);
    }

    public void startGame() {
        configureNextRound();
        startRound();
    }

    private void configureNextRound() {
        for (FallingWord fallingWord : fallingWords) {
            fallingWord.animator.removeAllListeners();
            fallingWord.animator.cancel();
        }
        remainingFallingWords = MAX_FALLING_WORDS;
        fallingWords.clear();

        int correctFallingWordPosition = random.nextInt(MAX_FALLING_WORDS - 1);
        for (int i = 0; i < MAX_FALLING_WORDS; i++) {
            if (i == correctFallingWordPosition) {
                Point spawnPoint = spawnPoints.get(i % MAX_SPAWN_POINTS);
                correctFallingWord = getFallingWord(wordPairs.get(round), spawnPoint);
                fallingWords.add(correctFallingWord);
                continue;
            }
            fallingWords.add(getRandomFallingWord(i));
        }

        // Increment the round counter.
        round++;
    }

    private FallingWord getRandomFallingWord(int index) {
        int randomNumber = round;
        while (randomNumber == round) {
            randomNumber = random.nextInt(wordPairs.size() - 1);
        }

        Point spawnPoint = spawnPoints.get(index % MAX_SPAWN_POINTS);
        return getFallingWord(wordPairs.get(randomNumber), spawnPoint);
    }

    private FallingWord getFallingWord(WordPair wordPair, Point position) {
        FallingWord fallingWord = new FallingWord(wordPair, position);
        fallingWord.setBounds(fallingWordPaint);
        return fallingWord;
    }

    private void startRound() {
        status = STATUS_PENDING_ANSWER;
        startFallingWordsAnimation();
    }

    private void startFallingWordsAnimation() {
        for (int i = 0; i < fallingWords.size(); i++) {
            FallingWord fallingWord = fallingWords.get(i);
            fallingWord.animator = startFallingWordAnimation(i);
        }
    }

    private ValueAnimator startFallingWordAnimation(int fallingWordIndex) {
        final FallingWord fallingWord = fallingWords.get(fallingWordIndex);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(fallingWord.position.y, getMeasuredHeight() - BOTTOM_PANEL_HEIGHT);
        valueAnimator.setDuration(FALLING_WORD_DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fallingWord.position.y = (int) animation.getAnimatedValue();
                FallingWordsView.this.invalidate();
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fallingWord.status = FallingWord.STATUS_FALLING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fallingWord.status = FallingWord.STATUS_COMPLETED;
                remainingFallingWords--;
                if (remainingFallingWords == 0) {
                    startGame();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.setStartDelay(fallingWordIndex * FALLING_WORD_START_DELAY);
        valueAnimator.start();

        return valueAnimator;
    }

    private void pauseFallingWordsAnimation() {
        for (FallingWord fallingWord : fallingWords) {
            ValueAnimator animator = fallingWord.animator;
            animator.pause();
        }
    }

    private void cancelFallingWordsAnimation() {
        for (FallingWord fallingWord : fallingWords) {
            ValueAnimator animator = fallingWord.animator;
            animator.removeAllListeners();
            animator.cancel();
        }
    }

    private void startCorrectAnimation() {
        colorAnimator = ValueAnimator.ofArgb(Color.GREEN, Color.LTGRAY);
        colorAnimator.setDuration(BOTTOM_PANEL_DURATION);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bottomPanelColor = (int) animation.getAnimatedValue();
                FallingWordsView.this.invalidate();
            }
        });
        colorAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                score++;
                clearFallingWords();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startGame();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        colorAnimator.start();
    }

    private void clearFallingWords() {
        cancelFallingWordsAnimation();
        startRound();
        for (FallingWord fallingWord : fallingWords) {
            fallingWord.status = FallingWord.STATUS_COMPLETED;
        }
    }

    private void startIncorrectAnimation() {
        colorAnimator = ValueAnimator.ofArgb(Color.RED, Color.LTGRAY);
        colorAnimator.setDuration(BOTTOM_PANEL_DURATION);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bottomPanelColor = (int) animation.getAnimatedValue();
                FallingWordsView.this.invalidate();
            }
        });
        colorAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                score--;
                score = score < 0 ? 0 : score;
                clearSelectedFallingWord();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        colorAnimator.start();
    }

    private void clearSelectedFallingWord() {
        remainingFallingWords--;
        FallingWord selectedFallingWord = fallingWords.get(selectedFallingWordIndex);
        selectedFallingWord.animator.removeAllListeners();
        selectedFallingWord.animator.cancel();
        selectedFallingWord.status = FallingWord.STATUS_REMOVED;
    }
}
