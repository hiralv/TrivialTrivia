package com.avinashdavid.trivialtrivia.web.data;

public class Score {

    private String userid;
    private String testscore;
    private String quizid;

    public Score(String userid, String testscore, String quizid) {
        this.userid = userid;
        this.testscore = testscore;
        this.quizid = quizid;
    }

    public String getUserid() {
        return userid;
    }

    public String getTestscore() {
        return testscore;
    }

    public String getQuizid() {
        return quizid;
    }
}
