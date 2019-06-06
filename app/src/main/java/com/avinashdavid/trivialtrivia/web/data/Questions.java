package com.avinashdavid.trivialtrivia.web.data;

import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class Questions {

    List<IndividualQuestion> questions;

    public List<IndividualQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<IndividualQuestion> questions) {
        this.questions = questions;
    }

    public static class DataStateDeserializer implements JsonDeserializer<IndividualQuestion> {

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
