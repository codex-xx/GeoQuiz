package com.example.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_SCORE = "score";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
    };

    private int mCurrentIndex = 0;
    private boolean[] mQuestionAnswered = new boolean[mQuestionBank.length];
    private int mCorrectAnswers = 0;
    private boolean mIsCheater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            mIsCheater = false;
            updateQuestion();
        });

        mTrueButton = findViewById(R.id.true_button);
        mFalseButton = findViewById(R.id.false_button);
        mNextButton = findViewById(R.id.next_button);
        mPrevButton = findViewById(R.id.prev_button);
        mCheatButton = findViewById(R.id.cheat_button);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mQuestionAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            mCorrectAnswers = savedInstanceState.getInt(KEY_SCORE, 0);
            mIsCheater = savedInstanceState.getBoolean("is_cheater", false);
        }

        updateQuestion();

        mTrueButton.setOnClickListener(v -> checkAnswer(true));
        mFalseButton.setOnClickListener(v -> checkAnswer(false));

        mNextButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            mIsCheater = false;
            updateQuestion();
        });

        mPrevButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex - 1 + mQuestionBank.length) % mQuestionBank.length;
            mIsCheater = false;
            updateQuestion();
        });

        mCheatButton.setOnClickListener(v -> {
            boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
            Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
            startActivityForResult(intent, REQUEST_CODE_CHEAT);
        });
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        if (mQuestionAnswered[mCurrentIndex]) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId;
        if (mIsCheater) {
            messageResId = R.string.judgment_toast;
        } else {
            messageResId = userPressedTrue == answerIsTrue
                    ? R.string.correct_toast
                    : R.string.incorrect_toast;
        }

        Toast toast = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 100);
        toast.show();

        mQuestionAnswered[mCurrentIndex] = true;
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);

        if (userPressedTrue == answerIsTrue) {
            mCorrectAnswers++;
        }

        if (isQuizComplete()) {
            showScore();
        }
    }

    private boolean isQuizComplete() {
        for (boolean answered : mQuestionAnswered) {
            if (!answered) return false;
        }
        return true;
    }

    private void showScore() {
        int totalQuestions = mQuestionBank.length;
        int scorePercent = (mCorrectAnswers * 100) / totalQuestions;

        Toast toast = Toast.makeText(this,
                "Your score: " + scorePercent + "%", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBooleanArray(KEY_ANSWERED, mQuestionAnswered);
        outState.putInt(KEY_SCORE, mCorrectAnswers);
        outState.putBoolean("is_cheater", mIsCheater);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_CODE_CHEAT && data != null) {
            mIsCheater = CheatActivity.wasAnswerShown(data);
        }
    }
}
