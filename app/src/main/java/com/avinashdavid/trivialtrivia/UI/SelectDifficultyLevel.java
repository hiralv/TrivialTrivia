package com.avinashdavid.trivialtrivia.UI;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.Toolbar;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.Spinner;

        import com.avinashdavid.trivialtrivia.R;
        import com.avinashdavid.trivialtrivia.data.DataHolder;

public class SelectDifficultyLevel extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner difficultyLevelSpinner;
    ArrayAdapter<CharSequence> difficultyLevelAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_difficulty_level);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        difficultyLevelSpinner = (Spinner)findViewById(R.id.difficulty_level_dd);
        difficultyLevelAdapter = ArrayAdapter.createFromResource(this, R.array.difficulty_level, R.layout.spinner_text);
        difficultyLevelAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        difficultyLevelSpinner.setAdapter(difficultyLevelAdapter);
        difficultyLevelSpinner.setOnItemSelectedListener(this);

        String str = difficultyLevelSpinner.getSelectedItem().toString();
       // difficultyLevelSpinner.setOnItemSelectedListener(this);

        ((Button)findViewById(R.id.button_select_difficulty_level)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectDifficultyLevel.this, ActivityQuiz.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
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
        DataHolder.difficultyLevel = difficultyLevelSpinner.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
