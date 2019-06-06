package com.avinashdavid.trivialtrivia.web.services;

import com.avinashdavid.trivialtrivia.web.data.Login;
import com.avinashdavid.trivialtrivia.web.data.Questions;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WiseService {

    @GET("/questions")
    Call<Questions> getQuestions();

    @POST("/login")
    Call<Boolean> login(@Body Login login);
}
