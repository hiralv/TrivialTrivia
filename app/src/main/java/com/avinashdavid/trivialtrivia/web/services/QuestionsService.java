package com.avinashdavid.trivialtrivia.web.services;

import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;

import org.json.JSONObject;

import java.util.List;

public interface QuestionsService {

    List<IndividualQuestion> getQuestions(int numberOfQuestions);

    List<IndividualQuestion> getFullQuestionSet();

    void setJsonObject(JSONObject jsonObject);

}
