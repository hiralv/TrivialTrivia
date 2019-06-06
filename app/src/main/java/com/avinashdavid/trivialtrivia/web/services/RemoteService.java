package com.avinashdavid.trivialtrivia.web.services;

import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.avinashdavid.trivialtrivia.web.data.Questions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RemoteService {

    private final WiseService wiseService;

    public RemoteService() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(IndividualQuestion.class, new Questions.DataStateDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://52.173.186.160:8083")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        wiseService = retrofit.create(WiseService.class);
    }

    public WiseService getWiseService() {
        return wiseService;
    }
}
