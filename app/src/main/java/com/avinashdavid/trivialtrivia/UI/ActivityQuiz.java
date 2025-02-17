package com.avinashdavid.trivialtrivia.UI;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.data.QuizDBContract;
import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.avinashdavid.trivialtrivia.scoring.QuestionScorer;
import com.avinashdavid.trivialtrivia.scoring.QuizScorer;
import com.avinashdavid.trivialtrivia.services.InsertRecordsService;
import com.avinashdavid.trivialtrivia.web.data.Questions;
import com.avinashdavid.trivialtrivia.web.services.LocalQuestionService;
import com.avinashdavid.trivialtrivia.web.services.RemoteService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityQuiz extends AppCompatActivity {
    private int QUIZ_NUMBER;
    private static final String KEY_QUIZ_NUMBER = "quizNumber";
    private static final String KEY_QUIZ_SIZE = "quizSize";
    private static final String KEY_QUESTION_NUMBER = "questionNumber";
    private static final String KEY_CURRENT_SECONDS = "currentSeconds";

    private ListView mListView;
    private CardView mCardView;
    private TextView mNumberTextView;
    private TextView mCategoryTextView;
    private static List<IndividualQuestion> sIndividualQuestions;
    private int mQuestionNumber;
    private Button mNextQuestionButton;
    private Button mPreviousQuestionButton;
    private int mQuizSize;
    private int currentVersionCode;

    private FrameLayout mFrameLayout;

    private ProgressBar mProgressBar;
    private TextView mSecondsTextview;

    private int maxTime;

    private CountDownTimer mCountDownTimer;
    public int mCurrentSeconds;

    private static QuizScorer sQuizScorer;

    private boolean hasVibrator;
    private Vibrator mVibrator;
    private static final int vibrationMillis = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        currentVersionCode = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_quiz);
        setupWindowAnimations();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        hasVibrator = ((Vibrator) getSystemService(VIBRATOR_SERVICE)).hasVibrator();
        if (hasVibrator) {
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }

        maxTime = 20;

        if (savedInstanceState != null) {
            mQuizSize = savedInstanceState.getInt(KEY_QUIZ_SIZE);
            mQuestionNumber = savedInstanceState.getInt(KEY_QUESTION_NUMBER);
            QUIZ_NUMBER = savedInstanceState.getInt(KEY_QUIZ_NUMBER);
            mCurrentSeconds = savedInstanceState.getInt(KEY_CURRENT_SECONDS);
        } else {
            mQuizSize = 10;
            Cursor c = getContentResolver().query(QuizDBContract.QuizEntry.CONTENT_URI, new String[]{QuizDBContract.QuizEntry._ID}, null, null, null);
            if (c.moveToFirst()) {
                QUIZ_NUMBER = c.getCount() + 1;
            } else {
                QUIZ_NUMBER = QuizScorer.sQuizNumber + 1;
            }
            c.close();
            mQuestionNumber = 0;
            mCurrentSeconds = maxTime;
        }

        // selectDifficultyLevel();
        sQuizScorer = QuizScorer.getInstance(this, mQuizSize, QUIZ_NUMBER);
//        sIndividualQuestions = QuestionsHandling.getInstance(this.getApplicationContext(), QUIZ_NUMBER).getRandomQuestionSet(mQuizSize, QUIZ_NUMBER);


//        mCardView = (CardView) findViewById(R.id.card_view);
//        mListView = (ListView)rootview.findViewById(R.id.choices_listview);

        RemoteService questionsService = new RemoteService();
        questionsService.getWiseService().getQuestions().enqueue(new Callback<Questions>() {
            @Override
            public void onResponse(Call<Questions> call, Response<Questions> response) {
                start(response.body().getQuestions());
            }

            @Override
            public void onFailure(Call<Questions> call, Throwable t) {
                start(new LocalQuestionService(getBaseContext()).getQuestions(10));
            }
        });

    }

    protected void selectDifficultyLevel() {
        Intent intent = new Intent(this, ActivityPostQuiz.class);
        intent.putExtra(ActivityPostQuiz.KEY_QUIZ_SIZE, mQuizSize);
        intent.putExtra(ActivityPostQuiz.KEY_QUIZ_NUMBER, QUIZ_NUMBER);
        startActivity(intent);
    }

    private void start(List<IndividualQuestion> questions) {
        sIndividualQuestions = questions;
        mNumberTextView = (TextView) findViewById(R.id.questionNumber_textview);
        mCategoryTextView = (TextView) findViewById(R.id.category_textview);
        mSecondsTextview = (TextView) findViewById(R.id.seconds_display);
        mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
        mFrameLayout = (FrameLayout) findViewById(R.id.card_framelayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(maxTime);
        mProgressBar.setProgress(mCurrentSeconds);
        mCountDownTimer = new CountDownTimer((mCurrentSeconds + 2) * 1000, 1000) {
            int mTicknumber = 0;

            @Override
            public void onTick(long l) {
                mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
                mProgressBar.setProgress(mCurrentSeconds);
//                Log.d("timer", "ontick" + Integer.toString(mTicknumber++) + ": " + Integer.toString(mCurrentSeconds));
                if (mCurrentSeconds <= 0) {
                    mSecondsTextview.setTextColor(getResources().getColor(R.color.wrongAnswerRed));
                    if (mCurrentSeconds < 0) {
                        mFrameLayout.setClickable(false);
                    }
                }
                mCurrentSeconds -= 1;
            }

            @Override
            public void onFinish() {
                mSecondsTextview.setTextColor(getResources().getColor(R.color.darker_gray));
                mProgressBar.setProgress(0);
                IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
                sQuizScorer.addQuestionScorer(currentQuestion, mQuestionNumber, QuestionScorer.NO_ANSWER);
                goToNextQuestion();
                mTicknumber = 0;
            }
        };

//        mNextQuestionButton = (Button)findViewById(R.id.buttonNextQuestion);
//        mPreviousQuestionButton = (Button)findViewById(R.id.buttonPreviousQuestion);

        setAndUpdateChoiceTextViews(mQuestionNumber);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_QUESTION_NUMBER, mQuestionNumber);
        outState.putInt(KEY_QUIZ_NUMBER, QUIZ_NUMBER);
        outState.putInt(KEY_QUIZ_SIZE, mQuizSize);
        outState.putInt(KEY_CURRENT_SECONDS, mCurrentSeconds);
        super.onSaveInstanceState(outState);
    }

    @TargetApi(21)
    private void setupWindowAnimations() {
        Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
        getWindow().setEnterTransition(slide);
    }

    //updates the mCurrentDisplayQuestion object and text of the respective textviews
    private void setAndUpdateChoiceTextViews(int questionNumber) {
        if (currentVersionCode >= 13) {
            updateFragmentAnimated();
        } else {
            updateFragmentTraditional();
        }
        mNumberTextView.setText(Integer.toString(mQuestionNumber + 1));
        IndividualQuestion individualQuestion = sIndividualQuestions.get(questionNumber);
        mCategoryTextView.setText(individualQuestion.categoryText);

        if (mCountDownTimer == null) {
            mCountDownTimer = new CountDownTimer((mCurrentSeconds + 2) * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    mProgressBar.setProgress(mCurrentSeconds);
                    mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
                    mCurrentSeconds -= 1;
                }

                @Override
                public void onFinish() {
                    mProgressBar.setProgress(0);
                    IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
                    sQuizScorer.addQuestionScorer(currentQuestion, mQuestionNumber, QuestionScorer.NO_ANSWER);
                    goToNextQuestion();
//                    if (mQuestionNumber < mQuizSize) {
//                        this.start();
//                    }
                }
            };
        } else {
            mCountDownTimer.cancel();
        }

        mCountDownTimer.start();
    }

    //updates question number by +=1
    private void goToNextQuestion() {
        doVibration(hasVibrator);
        if (mQuestionNumber < mQuizSize - 1) {
            mQuestionNumber += 1;
            setAndUpdateChoiceTextViews(mQuestionNumber);
            mSecondsTextview.setTextColor(getResources().getColor(R.color.darker_gray));
            mCurrentSeconds = maxTime;
            mCountDownTimer.cancel();
            mCountDownTimer.start();
        } else {
            endQuiz();
        }
    }

    //updates question number by -=1
    private void goToPreviousQuestion() {
        if (mQuestionNumber > 0) {
            mQuestionNumber -= 1;
            setAndUpdateChoiceTextViews(mQuestionNumber);
        }
    }

    private void updateFragmentTraditional() {
        android.support.v4.app.Fragment fragmentQuestion = FragmentQuestion.getInstance(sIndividualQuestions.get(mQuestionNumber));
        getSupportFragmentManager().beginTransaction().replace(R.id.card_framelayout, fragmentQuestion).commit();
    }

    @TargetApi(13)
    private void updateFragmentAnimated() {
        android.app.Fragment fragmentQuestion = FragmentQuestionHoneycomb.getInstance(sIndividualQuestions.get(mQuestionNumber));
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (mQuestionNumber > 0) {
            fragmentTransaction

                    // Replace the default fragment animations with animator resources
                    // representing rotations when switching to the back of the card, as
                    // well as animator resources representing rotations when flipping
                    // back to the front (e.g. when the system Back button is pressed).
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out);
        }
        fragmentTransaction
                // Replace any fragments currently in the container view with a
                // fragment representing the next page (indicated by the
                // just-incremented currentPage variable).
                .replace(R.id.card_framelayout, fragmentQuestion)

//                // Add this transaction to the back stack, allowing users to press
//                // Back to get to the front of the card.
//                .addToBackStack(null)

                // Commit the transaction.
                .commit();
    }

    public void addQuestionScorer(View v) {
        int chosenAnswer = -1;
        switch (v.getId()) {
            case R.id.choice1:
                chosenAnswer = 0;
                break;
            case R.id.choice2:
                chosenAnswer = 1;
                break;
            case R.id.choice3:
                chosenAnswer = 2;
                break;
            case R.id.choice4:
                chosenAnswer = 3;
                break;
        }
        IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
        int timeTaken = maxTime - mCurrentSeconds;
        try {
            sQuizScorer.addQuestionScorer(currentQuestion, timeTaken, chosenAnswer);
//            Log.d("quizTracker", Integer.toString(mQuestionNumber) + ": chosen answer is " + Integer.toString(chosenAnswer) + " correct answer is " +Integer.toString(currentQuestion.correctAnswer));
        } finally {
            goToNextQuestion();
        }
    }

    public void endQuiz() {
        if (sQuizScorer.getQuestionScorers().size() == mQuizSize) {
            ArrayList<QuestionScorer> questionScorers = sQuizScorer.getQuestionScorers();
            if (questionScorers != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
                try {
                    Intent intent = new Intent(this, InsertRecordsService.class);
                    intent.putExtra(InsertRecordsService.EXTRA_SERICE_QUIZ_SIZE, mQuizSize);
                    intent.putExtra(InsertRecordsService.EXTRA_SERVICE_QUIZ_NUMBER, QUIZ_NUMBER);
                    startService(intent);
                } finally {
                    Intent intent = new Intent(this, ActivityPostQuiz.class);
                    intent.putExtra(ActivityPostQuiz.KEY_QUIZ_SIZE, mQuizSize);
                    intent.putExtra(ActivityPostQuiz.KEY_QUIZ_NUMBER, QUIZ_NUMBER);
                    startActivity(intent);
                }
            } else {
//                Log.d("ActivityQuiz", "null questionScorers object");
            }
        } else {
//            Log.d("ActivityQuiz","quiz size is too small");
        }
    }

    public void doVibration(boolean hasVibrator) {
        if (hasVibrator) {
            if (mVibrator == null) {
                mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }
            mVibrator.vibrate(vibrationMillis);
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

}
