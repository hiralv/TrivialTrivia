package com.avinashdavid.trivialtrivia.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.web.data.Login;
import com.avinashdavid.trivialtrivia.web.services.RemoteService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        ((TextView)findViewById(R.id.or)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLogin.this, UserRegistration.class);
                startActivity(intent);
            }
        });

        ((Button)findViewById(R.id.button_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = ((EditText)findViewById(R.id.username)).getText().toString();
                final String password = ((EditText)findViewById(R.id.password)).getText().toString();

                RemoteService remoteService = new RemoteService();
                remoteService.getWiseService().login(new Login(username, password)).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if(response.body().booleanValue()) {
                            next(username);
                        } else {
                            invalidUser("Invalid Username/Password");
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        invalidUser("Unable to connect to server");
                    }
                });

            }
        });

        ((TextView)findViewById(R.id.continue_as_guest)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLogin.this, ActivityWelcomePage.class);
                intent.putExtra("guest", "true");
                startActivity(intent);
                finish();
            }
        });

    }

    private void invalidUser(String error) {
        ((TextView)findViewById(R.id.textview_error)).setText(error);
    }

    private void next(String username) {
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

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

}
