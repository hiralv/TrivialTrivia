package com.avinashdavid.trivialtrivia.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avinashdavid.trivialtrivia.R;

public class UserLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences settings = getSharedPreferences("userSession", 0);
        boolean isUserLoggedIn = settings.getBoolean("LoggedIn", false);

        if(isUserLoggedIn){
            //Go directly to Homescreen.
            Intent intent = new Intent(UserLogin.this, ActivityWelcomePage.class);

            intent.putExtra("isUserLoggedIn", "true");
            intent.putExtra("username", settings.getString("username",""));
            startActivity(intent);
            finish();

        }

        ((Button)findViewById(R.id.button_register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLogin.this, UserRegistration.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.button_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = ((EditText)findViewById(R.id.username)).getText().toString();
                String password = ((EditText)findViewById(R.id.password)).getText().toString();


                if(getResultFromLoginAPI(username,password)){
                    SharedPreferences settings = getSharedPreferences("userSession", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("LoggedIn", true);
                    editor.putString("username", username);
                    editor.commit();

                    Intent intent = new Intent(UserLogin.this, ActivityWelcomePage.class);
                    intent.putExtra("isUserLoggedIn", "true");
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                }

            }
        });

    }

    private boolean getResultFromLoginAPI(String username, String password) {

        //TODO make rest call and return true or false
        //User authentication

        return true; //TODO true for now
    }

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

}
