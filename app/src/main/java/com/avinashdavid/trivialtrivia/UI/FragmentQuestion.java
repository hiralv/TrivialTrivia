package com.avinashdavid.trivialtrivia.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;

/**
 * Created by avinashdavid on 11/27/16.
 */

public class FragmentQuestion extends Fragment {
    private IndividualQuestion question;

    private TextView mQuestionView;
    private Button mChoice1TextView;
    private Button mChoice2TextView;
    private Button mChoice3TextView;
    private Button mChoice4TextView;

    public static Fragment getInstance(IndividualQuestion question){
        FragmentQuestion fragmentQuestion = new FragmentQuestion();
        fragmentQuestion.question = question;
        return fragmentQuestion;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_question, container, false);

        mQuestionView = (TextView)rootview.findViewById(R.id.question_textview);
        mChoice1TextView = (Button)rootview.findViewById(R.id.choice1);
        mChoice2TextView = (Button)rootview.findViewById(R.id.choice2);
        mChoice3TextView = (Button)rootview.findViewById(R.id.choice3);
        mChoice4TextView = (Button)rootview.findViewById(R.id.choice4);


        setAndUpdateTextViews();

        return rootview;
    }

    private void setAndUpdateTextViews(){
        mQuestionView.setText(question.question);
        mChoice1TextView.setText(question.choicesList[0]);
        mChoice2TextView.setText(question.choicesList[1]);
        mChoice3TextView.setText(question.choicesList[2]);
        mChoice4TextView.setText(question.choicesList[3]);
    }

//    private void submitAnswer(int choiceIndex){
//        ((ActivityQuiz)getActivity()).addQuestionScorer(choiceIndex);
//    }
}
