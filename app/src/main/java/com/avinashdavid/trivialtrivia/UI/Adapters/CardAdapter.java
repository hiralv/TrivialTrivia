package com.avinashdavid.trivialtrivia.UI.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.avinashdavid.trivialtrivia.scoring.QuestionScorer;

import java.util.ArrayList;

/**
 * Created by avinashdavid on 11/29/16.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private static final String LOG_TAG = "CardAdapterClass";
    private Context mContext;
    private ArrayList<QuestionScorer> mData;

    private static final int VIEW_TYPE_FIRST = 0;
    private static final int VIEW_TYPE_ALL_OTHERS = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView questionTextview;
        public TextView youselectedTextview;
        public TextView correctanswerTextview;
        public TextView rationaleTextview;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            if (itemView.findViewById(R.id.question)!=null) {
                questionTextview = (TextView) itemView.findViewById(R.id.question);
                youselectedTextview = (TextView) itemView.findViewById(R.id.you_selected);
                correctanswerTextview = (TextView) itemView.findViewById(R.id.correct_answer);
                rationaleTextview = (TextView) itemView.findViewById(R.id.rationale);
            }
        }
    }

    public static class FirstViewHolder extends RecyclerView.ViewHolder{


        public FirstViewHolder(View itemView){
            super(itemView);


        }
    }

    public CardAdapter(Context c, ArrayList<QuestionScorer> questionScorers){
//        super(c, 0, questionScorers);
        mContext = c;
//        for (int i = 0; i < questionScorers.size(); i++){
//            if (questionScorers.get(i).getQuestionEvaluation()){
//                questionScorers.remove(i);
//                i-=1;
//            }
//        }
        mData = new ArrayList<>(questionScorers.size());
        mData.addAll(questionScorers);
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);


        View convertView;
        if (viewType == VIEW_TYPE_ALL_OTHERS) {
           convertView =inflater.inflate(R.layout.cardview_post_quiz, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.item_swipe_for_answers, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mData.size() <= 0){
            holder.questionTextview.setText("No data");
        }
        else if (position == 0){
            return;
        }
        else {
            QuestionScorer currentScorer = mData.get(position - 1);
            IndividualQuestion individualQuestion = currentScorer.getQuestion();
            holder.questionTextview.setText(individualQuestion.question);
            holder.correctanswerTextview.setText(individualQuestion.choicesList[individualQuestion.correctAnswer]);
            if (currentScorer.getChosenAnswer() != -1) {
                holder.youselectedTextview.setText(individualQuestion.choicesList[currentScorer.getChosenAnswer()]);
            } else {
                holder.youselectedTextview.setText(mContext.getString(R.string.no_choice));
            }
            if (currentScorer.getQuestionEvaluation()){
                holder.youselectedTextview.setTextColor(mContext.getResources().getColor(R.color.correctAnswerGreen));
            } else {
                holder.youselectedTextview.setTextColor(mContext.getResources().getColor(R.color.wrongAnswerRed));
            }
            holder.rationaleTextview.setText(individualQuestion.rationale);
            holder.rationaleTextview.setTextColor(mContext.getResources().getColor(R.color.black));
        }
    }



    @Override
    public int getItemCount() {
        if(mData.size()<=0) {
//            Log.d(LOG_TAG, "no data");
            return 1;
        }
        return (mData.size()+1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_FIRST;
        else
            return VIEW_TYPE_ALL_OTHERS;
    }
}
