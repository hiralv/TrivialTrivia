package com.avinashdavid.trivialtrivia.web.services;

import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class RemoteWiseServiceTest {

    @Test
    public void test() throws IOException {
        RemoteService remoteService = new RemoteService();
        List<IndividualQuestion> questions = remoteService.getWiseService().getQuestions().execute().body().getQuestions();
        System.out.println(questions);
    }
}