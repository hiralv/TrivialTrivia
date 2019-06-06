package com.avinashdavid.trivialtrivia.web.data;

public class Login {

    private String loginID;
    private String passwd;

    public Login(String loginID, String passwd) {
        this.loginID = loginID;
        this.passwd = passwd;
    }

    public String getLoginID() {
        return loginID;
    }

    public String getPasswd() {
        return passwd;
    }

}
