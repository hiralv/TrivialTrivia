package com.avinashdavid.trivialtrivia.web.services;

import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RemoteQuestionService implements QuestionsService {

    private final QuestionService questionService;

    public RemoteQuestionService() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(IndividualQuestion.class, new DataStateDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://52.173.186.160:8083")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        questionService = retrofit.create(QuestionService.class);
    }

    @Override
    public List<IndividualQuestion> getQuestions(int numberOfQuestions) {
        try {
            return questionService.getQuestions().execute().body().getQuestions();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void getQuestions(Callback<Questions> callback) {
        questionService.getQuestions().enqueue(callback);
    }

    private interface QuestionService {
        @GET("/questions")
        Call<Questions> getQuestions();
    }

    public class Questions {

        List<IndividualQuestion> questions;

        public List<IndividualQuestion> getQuestions() {
            return questions;
        }

        public void setQuestions(List<IndividualQuestion> questions) {
            this.questions = questions;
        }

    }

    class DataStateDeserializer implements JsonDeserializer<IndividualQuestion> {

        String KEY_CATEGORY = "category";
        String KEY_QUESTION = "question";
        String KEY_CHOICES = "choices";
        String KEY_CORRECTANSWER = "correctAnswer";
        String KEY_RATIONALE = "rationale";

        @Override
        public IndividualQuestion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject thisQuestion = json.getAsJsonObject();
            String category = thisQuestion.get(KEY_CATEGORY).getAsString();
            String question = thisQuestion.get(KEY_QUESTION).getAsString();
            JsonArray choiceArray = thisQuestion.get(KEY_CHOICES).getAsJsonArray();
            String[] choicesList = new String[choiceArray.size()];
            for (int l = 0; l < choiceArray.size(); l++) {
                choicesList[l] = choiceArray.get(l).getAsString();
            }
            int correctAnswer = thisQuestion.get(KEY_CORRECTANSWER).getAsInt();
            String rationale = thisQuestion.get(KEY_RATIONALE).getAsString();
            return new IndividualQuestion(category, question, choicesList, correctAnswer, rationale);
        }
    }
}
