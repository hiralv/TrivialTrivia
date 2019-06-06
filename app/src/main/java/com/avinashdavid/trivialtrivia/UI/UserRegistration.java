package com.avinashdavid.trivialtrivia.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.web.data.Login;
import com.avinashdavid.trivialtrivia.web.data.Registration;
import com.avinashdavid.trivialtrivia.web.services.RemoteService;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRegistration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Spinner coursesSpinner = (Spinner)findViewById(R.id.reg_course);
        ArrayAdapter<CharSequence> coursesAdapter = ArrayAdapter.createFromResource(this, R.array.courses, R.layout.spinner_text);
        coursesAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        coursesSpinner.setAdapter(coursesAdapter);

        Spinner genderSpinner = (Spinner)findViewById(R.id.reg_gender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_text);
        genderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        genderSpinner.setAdapter(genderAdapter);

        Spinner gradeSpinner = (Spinner)findViewById(R.id.reg_gradeLevel);
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(this, R.array.gradeLevel, R.layout.spinner_text);
        gradeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        gradeSpinner.setAdapter(gradeAdapter);
        gradeSpinner.setOnItemSelectedListener(this);

        TextView errorMsgView = (TextView) findViewById(R.id.reg_invalid);
        errorMsgView.setVisibility(View.INVISIBLE);
        TextView passwordErrorMsgView = (TextView) findViewById(R.id.reg_invalid_password);
        passwordErrorMsgView.setVisibility(View.INVISIBLE);

        ((Button)findViewById(R.id.button_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText textbox_username = (EditText) findViewById(R.id.reg_username);
                EditText textbox_password = (EditText) findViewById(R.id.reg_password);
                EditText textbox_confirmPassword = (EditText) findViewById(R.id.reg_confirm_password);
                EditText textbox_studentID = (EditText) findViewById(R.id.reg_studentID);
                EditText textbox_firstName = (EditText) findViewById(R.id.reg_firstName);
                EditText textbox_middleInitial = (EditText) findViewById(R.id.reg_MiddleInitial);
                EditText textbox_lastName = (EditText) findViewById(R.id.reg_lastName);
                EditText textbox_dateOfBirth = (EditText) findViewById(R.id.reg_dateOfBirth);
                Spinner spinner_gender = (Spinner) findViewById(R.id.reg_gender);
                Spinner spinner_gradeLevel = (Spinner) findViewById(R.id.reg_gradeLevel);
                Spinner spinner_course = (Spinner) findViewById(R.id.reg_course);

                String username = textbox_username.getText().toString();
                String password = textbox_password.getText().toString();
                String confirm_password = textbox_confirmPassword.getText().toString();
                String studentID = textbox_studentID.getText().toString();
                String firstName = textbox_firstName.getText().toString();
                String middleInitial = textbox_middleInitial.getText().toString();
                String lastName = textbox_lastName.getText().toString();
                String dateOfBirth = textbox_dateOfBirth.getText().toString();
                String gender = spinner_gender.getSelectedItem().toString();
                String gradeLevel = spinner_gradeLevel.getSelectedItem().toString();
                String course = spinner_course.getSelectedItem().toString();

                TextView errorMsgView = (TextView) findViewById(R.id.reg_invalid);
                TextView passwordErrorMsgView = (TextView) findViewById(R.id.reg_invalid_password);

                Boolean isValid = false;
                if (username.isEmpty() || password.isEmpty() || confirm_password.isEmpty() ||
                        studentID.isEmpty() || firstName.isEmpty() || middleInitial.isEmpty() ||
                        lastName.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty() ||
                        gradeLevel.isEmpty() || course.isEmpty() ) {
                    errorMsgView.setVisibility(View.VISIBLE);
                }
                else {
                    if (password.equals(confirm_password)) {
                        isValid = true;
                    }
                    else {
                        passwordErrorMsgView.setVisibility(View.VISIBLE);
                    }
                }

                if (isValid) {
                    try {
                        RemoteService remoteService = new RemoteService();
                        remoteService.getWiseService().register(new Registration(username, password, confirm_password, studentID, firstName, middleInitial,
                                lastName, dateOfBirth, gender, gradeLevel, course)).enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if(response.body().booleanValue()) {
                                    Intent intent = new Intent(UserRegistration.this, UserLogin.class);
                                    startActivity(intent);
                                } else {

                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                String str = "failed";
                            }
                        });

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else {

                }

            }
        });
    }

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
