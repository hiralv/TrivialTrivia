package com.avinashdavid.trivialtrivia.UI;

import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.UI.Adapters.CardAdapter;
import com.avinashdavid.trivialtrivia.data.DataHolder;
import com.avinashdavid.trivialtrivia.scoring.QuestionScorer;
import com.avinashdavid.trivialtrivia.scoring.QuizScorer;
import com.avinashdavid.trivialtrivia.services.SendDeviceDetails;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;

public class ActivityPostQuiz extends AppCompatActivity {
    private ArrayList<QuestionScorer> mQuestionScorers;
    private int mQuizSize;
    private QuizScorer mQuizScorer;
    private int mQuizNumber;

    private RecyclerView mRecyclerView;
    private CardAdapter mCardAdapter;

    public static final String KEY_QUIZ_NUMBER = "questionScorers";
    public static final String KEY_QUIZ_SIZE = "quizSize";

    public static ActivityPostQuiz getInstance(ArrayList<QuestionScorer> questionScorers){
        ActivityPostQuiz activityPostQuiz = new ActivityPostQuiz();
        activityPostQuiz.mQuestionScorers = questionScorers;
        return activityPostQuiz;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_quiz);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*ImageView imageView = (ImageView)findViewById(R.id.imageview_Pie);
        imageView.getLayoutParams().height = 300                                                                                                                                ;
        imageView.getLayoutParams().width = 300;
        ((ImageView)findViewById(R.id.imageview_Pie)).setImageDrawable(getRoundedDrawable("start.jpg"));*/
        mRecyclerView = (RecyclerView) findViewById(R.id.cards_listview);
//        mRecyclerView.setDividerHeight(Float.valueOf(getResources().getDimension(R.dimen.activity_vertical_margin)).intValue());
        Intent intent = getIntent();
        mQuizSize = intent.getIntExtra(KEY_QUIZ_SIZE, 10);
        mQuizNumber = intent.getIntExtra(KEY_QUIZ_NUMBER, 0);
        mQuizScorer = QuizScorer.getInstance(this, mQuizSize, mQuizNumber);
        mQuestionScorers = mQuizScorer.getQuestionScorers();
        //String json = new Gson().toJson(mQuizScorer);
        try {
            TextView scoreView = (TextView) findViewById(R.id.scoreTextView);
            String score = Integer.toString(mQuizScorer.scoreQuiz(mQuestionScorers));
            scoreView.setText(score);

            String json = "{ \"userid\": \"" + DataHolder.userId + "\", \"testscore\": \"" + score + "\", \"quizid\": \"101\" }";

            //
            try {

                new SendDeviceDetails().execute("http://52.173.186.160:8083/quizresult", json);
               // new SendDeviceDetails().makeRequest("http://52.173.186.160:8083/quizresult", json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //
            scoreView.setTextSize(getResources().getDimension(R.dimen.score_textsize));
            ((TextView) findViewById(R.id.quizLengthTextView)).setText(Integer.toString(mQuestionScorers.size()));
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mCardAdapter = new CardAdapter(this, mQuestionScorers);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        SnapHelper helper = new LinearSnapHelper();
        mRecyclerView.setAdapter(mCardAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        helper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);

        ((TextView)findViewById(R.id.timeright_textview)).setText(getString(R.string.format_seconds, mQuizScorer.getTimeAverageCorrect()));
        ((TextView)findViewById(R.id.timewrong_textview)).setText(getString(R.string.format_seconds, mQuizScorer.getTimeAverageWrong()));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        int curSize = mCardAdapter.getItemCount();
//        mCardAdapter.addAll(mQuestionScorers);
        //NOTE: This is not how to add new items to the adapter. Please look at https://guides.codepath.com/android/using-the-recyclerview
//        mCardAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

    private RoundedBitmapDrawable getRoundedDrawable(String filename){
        try {
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), getAssets().open(filename));
            dr.setCornerRadius(500);
            return dr;
        }
        catch (IOException e){
//            Log.d("imageHandling", e.toString());
        }
        return null;
    }


//    public class CardAdapter extends ArrayAdapter<QuestionScorer>{
//        private ArrayList<QuestionScorer> mData;
//        private Context mContext;
//
//        public CardAdapter(Context c, ArrayList<QuestionScorer> questionScorers){
//            super(c, 0, questionScorers);
//            mContext = c;
//            for (int i = 0; i < questionScorers.size(); i++){
//                if (questionScorers.get(i).getQuestionEvaluation()){
//                    questionScorers.remove(i);
//                    i-=1;
//                }
//            }
//            mData = new ArrayList<>(questionScorers.size());
//            mData.addAll(questionScorers);
//        }
//
//        @NonNull
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            ArrayList<IndividualQuestion> individualQuestions = QuestionsHandling.getInstance(mContext).getFullQuestionSet();
//            if (convertView == null){
//                convertView = LayoutInflater.from(mContext).inflate(R.layout.cardview_post_quiz, parent, false);
//                holder = new ViewHolder();
//                holder.questionTextview = (TextView)convertView.findViewById(R.id.question);
//                holder.youselectedTextview = (TextView)convertView.findViewById(R.id.you_selected);
//                holder.correctanswerTextview = (TextView)convertView.findViewById(R.id.correct_answer);
//
//                convertView.setTag(holder);
//            } else {
//                holder=(ViewHolder)convertView.getTag();
//            }
//
//            if (mData.size() <= 0){
//                holder.questionTextview.setText("No data");
//            } else {
//                QuestionScorer currentScorer = mData.get(position);
//                IndividualQuestion individualQuestion = individualQuestions.get(currentScorer.getQuestionNumber());
//                holder.questionTextview.setText(individualQuestion.question);
//                holder.youselectedTextview.setText(individualQuestion.choicesList[currentScorer.getChosenAnswer()]);
//                holder.correctanswerTextview.setText(individualQuestion.choicesList[individualQuestion.correctAnswer]);
//                if (currentScorer.getQuestionEvaluation()){
//                    holder.youselectedTextview.setTextColor(mContext.getResources().getColor(R.color.correctAnswerGreen));
//                } else {
//                    holder.youselectedTextview.setTextColor(mContext.getResources().getColor(R.color.wrongAnswerRed));
//                }
//            }
//
//            return convertView;
//        }
//
//        @Override
//        public int getCount() {
//            if(mData.size()<=0) {
//                Log.d("ActivityPostQuiz", "no data");
//                return 1;
//            }
//            return mData.size();
//        }
//
//        @Override
//        public int getPosition(QuestionScorer item) {
//            return mData.indexOf(item);
//        }
//    }
//
//    public static class ViewHolder{
//
//        public TextView questionTextview;
//        public TextView youselectedTextview;
//        public TextView correctanswerTextview;

//    }
}
