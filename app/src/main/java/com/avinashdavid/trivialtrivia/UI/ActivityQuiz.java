package com.avinashdavid.trivialtrivia.UI;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avinashdavid.trivialtrivia.APIConstants;
import com.avinashdavid.trivialtrivia.R;
import com.avinashdavid.trivialtrivia.Utils.HttpUtils;
import com.avinashdavid.trivialtrivia.data.QuizDBContract;
import com.avinashdavid.trivialtrivia.questions.IndividualQuestion;
import com.avinashdavid.trivialtrivia.questions.QuestionsHandling;
import com.avinashdavid.trivialtrivia.scoring.QuestionScorer;
import com.avinashdavid.trivialtrivia.scoring.QuizScorer;
import com.avinashdavid.trivialtrivia.services.InsertRecordsService;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ActivityQuiz extends AppCompatActivity {
    private int QUIZ_NUMBER;
    private static final String KEY_QUIZ_NUMBER = "quizNumber";
    private static final String KEY_QUIZ_SIZE = "quizSize";
    private static final String KEY_QUESTION_NUMBER = "questionNumber";
    private static final String KEY_CURRENT_SECONDS = "currentSeconds";

    private ListView mListView;
    private CardView mCardView;
    private TextView mNumberTextView;
    private TextView mCategoryTextView;
    private static List<IndividualQuestion> sIndividualQuestions;
    private List<String> mCurrentDisplayQuestion;
    private int mQuestionNumber;
    private Button mNextQuestionButton;
    private Button mPreviousQuestionButton;
    private int mQuizSize;
    private int currentVersionCode;

    private FrameLayout mFrameLayout;

    private ProgressBar mProgressBar;
    private TextView mSecondsTextview;

    private int maxTime;

    private CountDownTimer mCountDownTimer;
    public int mCurrentSeconds;

    private static QuizScorer sQuizScorer;

    private boolean hasVibrator;
    private Vibrator mVibrator;
    private static final int vibrationMillis = 50;

    public static QuestionsHandling questionsHandling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        currentVersionCode = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_quiz);
        setupWindowAnimations();
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        hasVibrator = ((Vibrator)getSystemService(VIBRATOR_SERVICE)).hasVibrator();
        if (hasVibrator){
            mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        }

        maxTime = 20;

        if (savedInstanceState!=null){
            mQuizSize = savedInstanceState.getInt(KEY_QUIZ_SIZE);
            mQuestionNumber = savedInstanceState.getInt(KEY_QUESTION_NUMBER);
            QUIZ_NUMBER = savedInstanceState.getInt(KEY_QUIZ_NUMBER);
            mCurrentSeconds = savedInstanceState.getInt(KEY_CURRENT_SECONDS);
        } else {
            mQuizSize = 10;
            Cursor c = getContentResolver().query(QuizDBContract.QuizEntry.CONTENT_URI, new String[]{QuizDBContract.QuizEntry._ID}, null, null, null);
            if (c.moveToFirst()){
                QUIZ_NUMBER = c.getCount() + 1;
            } else {
                QUIZ_NUMBER = QuizScorer.sQuizNumber + 1;
            }
            c.close();
            mQuestionNumber = 0;
            mCurrentSeconds = maxTime;
        }

        sQuizScorer = QuizScorer.getInstance(this, mQuizSize, QUIZ_NUMBER);

        HttpUtils.get(APIConstants.QUESTION,null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("ActivityQuiz", response.toString());
                start(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("ActivityQuiz", responseString.toString());
                //TODO Throw exception

            }
        });


    }

    private void start(JSONObject response) {

        String resp = "{\"questions\" : [{\"category\":\"general\",\"question\": \"Grand Central Terminal, Park Avenue, New York is the world's\", \"choices\": [\"largest railway station\",\"highest railway station\",\"longest railway station\",\"None of the above\"], \"correctAnswer\":0},\n" +
                "    {\"category\":\"science\",\"question\": \"Entomology is the science that studies\", \"choices\": [\"Behavior of human beings\",\"Insects\",\"The origin and history of technical and scientific terms\",\"the formation of rocks\"], \"correctAnswer\":1},\n" +
                "    {\"category\":\"world\",\"question\": \"Eritrea, which became the 182nd member of the UN in 1993, is in the continent of\", \"choices\": [\"Asia\",\"Europe\",\"Africa\",\"Australia\"], \"correctAnswer\":2},\n" +
                "    {\"category\":\"world\",\"question\": \"Garampani sanctuary is located in the Indian town of\", \"choices\": [\"Diphu, Assam\",\"Junagarh, Gujarat\",\"Gangtok, Sikkim\",\"Kohima, Nagaland\"], \"correctAnswer\":0},\n" +
                "    {\"category\":\"general\",\"question\": \"For which of the following disciplines is Nobel Prize awarded?\", \"choices\": [\"Physics and Chemistry\",\"Literature, Peace and Economics\",\"Physiology or Medicine\",\"All of the above\"], \"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\": \"Hitler's party, which came into power in 1933, is known as\", \"choices\": [\"Ku-Klux-Klan\",\"Labour Party\",\"Democratic Party\",\"Nazi Party\"], \"correctAnswer\":3},\n" +
                "    {\"category\":\"science\",\"question\": \"Galileo was an Italian astronomer who\", \"choices\": [\"discovered four satellites of Jupiter\",\"discovered that the movement of pendulum produces a regular time measurement\",\"developed the telescope\",\"All of the above\"], \"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\": \"First China War was fought between\", \"choices\": [\"China and Britain\",\"China and France\",\"China and Egypt\",\"China and Greece\"], \"correctAnswer\":0},\n" +
                "    {\"category\":\"world\",\"question\": \"Famous Indian sculptures depicting art of love built some time in 950 AD - 1050 AD are at\", \"choices\": [\"Mahabalipuram temples\",\"Jama Masjid\",\"Khajuraho temples\",\"Sun temple\"], \"correctAnswer\":2},\n" +
                "    {\"category\":\"science\",\"question\":\"Friction can be reduced by changing from\",\"choices\":[\"sliding to rolling\",\"rolling to sliding\",\"potential energy to kinetic energy\",\"dynamic to static\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\",\"question\":\"The ozone layer restricts which of the following types of radiation?\",\"choices\":[\"Visible light\",\"Infrared radiation\",\"X-rays and gamma rays\",\"Ultraviolet radiation\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\":\"During World War II, when did Germany attack France?\",\"choices\":[\"1915\",\"1940\",\"1943\",\"1962\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\",\"question\":\"Eugenics is the study of\",\"choices\":[\"people of European origin\",\"different races of mankind\",\"altering human beings by changing their genetic components\",\"genetics of plants\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\",\"question\":\"Escape velocity of a rocket fired from the earth towards the moon is a velocity to get rid of the\",\"choices\":[\"Moon's gravitational pull\",\"Earth's gravitational pull\",\"Centripetal force due to the earth's rotation\",\"Pressure of the atmosphere\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\",\"question\":\"Hamid Karzai was chosen president of Afghanistan in\",\"choices\":[\"2002\",\"1978\",\"2010\",\"1899\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\",\"question\":\"Headquarters of UNO are located at\",\"choices\":[\"Geneva (Switzerland)\",\"Paris (France)\",\"Hague (Netherlands)\",\"New York (USA)\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\",\"question\":\"For seeing objects at the surface of water from a submarine under water, the instrument used is\",\"choices\":[\"Telescope\",\"Spectroscope\",\"Periscope\",\"No scope 360\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which 1990's TV series won the Emmy Award as best comedy in five consecutive years?\",\"choices\":[\"Friends\",\"Days of our Lives\",\"Ally McBeal\",\"Frasier\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\":\"Which US holiday came to exist as a result of a presidential Proclamation in 1863?\",\"choices\":[\"Thanksgiving\",\"Labor Day\",\"Veteran's Day\",\"4th of July\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Who wrote 'The Raven'?\",\"choices\":[\"Daniel Defoe\",\"Mark Twain\",\"Edgar Allan Poe\",\"Nathaniel Hawthorne\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\",\"question\":\"After Los Angeles, which California city not starting with an 'S' has the highest population?\",\"choices\":[\"Long Beach\",\"Fresno\",\"Oakland\",\"Fremont\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"In which year was the first talkie film, 'The Jazz Singer', released?\",\"choices\":[\"1905\",\"1916\",\"1927\",\"1939\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\",\"question\":\"Which American state has the closest proximity to Russia?\",\"choices\":[\"Hawaii\",\"Alaska\",\"California\",\"Oregon\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"science\",\"question\":\"Which word describes a living being that is both male and female?\",\"choices\":[\"Androgynous\",\"Gynandromorph\",\"Hermaphrodite\",\"Gonochorous\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"In the famous movie 'Rocky', what was Sylvester Stallone's character's last name?\",\"choices\":[\"Marciano\",\"Colavito\",\"Lynch\",\"Balboa\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which female artist has the most top ten Billboard hits?\",\"choices\":[\"Madonna\",\"Whitney Houston\",\"Britney Spears\",\"Janet Jackson\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\",\"question\":\"Who was the last president of the Soviet Union?\",\"choices\":[\"Nikita Khrushchev\",\"Gennady Yanayev\",\"Vladimir Lenin\",\"Mikhail Gorbachev\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which 1990s musical group was heavily criticized for releasing a song written by Charles Manson?\",\"choices\":[\"Pearl Jam\",\"Marilyn Manson\",\"Guns and Roses\",\"The Beastie Boys\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which is the largest Sesame Street Puppet?\",\"choices\":[\"Mr. Snuffleupagus\",\"Big Bird\",\"Elmo\",\"Blake\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\",\"question\":\"Who discovered penicillin?\",\"choices\":[\"Louis Pasteur\",\"Joseph Lister\",\"Robert Koch\",\"Alexander Fleming\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\",\"question\":\"Approximately how long does light from the sun need to reach the earth?\",\"choices\":[\"8 minutes\",\"24 hours\",\"45 seconds\",\"365 days\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\",\"question\":\"What is the smallest country in the world?\",\"choices\":[\"Grenada\",\"Monaco\",\"Vatican\",\"Tuvalu\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\",\"question\":\"Which disease is the focus of oncology?\",\"choices\":[\"Glaucoma\",\"HIV\",\"Diabetes\",\"Cancer\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\",\"question\":\"What is the most popular sport in the world?\",\"choices\":[\"Hockey\",\"Football/soccer\",\"Basketball\",\"Tennis\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\",\"question\":\"What is the hardest substance found in nature?\",\"choices\":[\"Iron\",\"Wurtzite boron nitride\",\"Diamond\",\"Alumina\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\",\"question\":\"What is the capital of Egypt?\",\"choices\":[\"Lagos\",\"Casablanca\",\"Cairo\",\"Durban\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"In the US TV show 'Friends', what is Chandler's last name?\",\"choices\":[\"Parsons\",\"Bing\",\"Owen\",\"Riggs\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\",\"question\":\"What was ship on which the first Pilgrims came to America called?\",\"choices\":[\"HMS Victory\",\"Santa Maria\",\"Mayflower\",\"Baltimore\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which TV character said 'Live long and Prosper?'\",\"choices\":[\"Leonard McCoy\",\"Luke Skywalker\",\"James Sully\",\"Mr. Spock\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\",\"question\":\"What is the common name for a black leopard?\",\"choices\":[\"Panther\",\"Cougar\",\"Jaguar\",\"Snow leopard\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\",\"question\":\"When did India and Pakistan gain official independence from Great Britain?\",\"choices\":[\"1950\",\"1776\",\"1961\",\"1947\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which rock and roll frontman was knighted in 2003 in a move which was criticized by his bandmate Keith Richards?\",\"choices\":[\"Steven Tyler\",\"Mick Jagger\",\"Robert Plant\",\"Bruce Springsteen\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\",\"question\":\"Which planet in our solar system is named after the Roman god of war?\",\"choices\":[\"Mars\",\"Jupiter\",\"Saturn\",\"Neptune\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Who sang the popular song 'Rolling in the Deep'?\",\"choices\":[\"Ziggy Marley\",\"Alicia Keys\",\"Lady Gaga\",\"Adele\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which author first wrote the three laws of robotics?\",\"choices\":[\"Douglas Adams\",\"William Gibson\",\"Isaac Asimov\",\"Arther C. Clarke\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\",\"question\":\"How many gallons of beer is in a firkin?\",\"choices\":[\"4\",\"50\",\"9\",\"0.6\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\":\"Which popular fast foot chain was started in the year 1955?\",\"choices\":[\"McDonald's\",\"Burger King\",\"Wendy's\",\"Chipotle\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\",\"question\":\"If dogs are canine, what are sheep?\",\"choices\":[\"Feline\",\"Ovine\",\"Equine\",\"Ursine\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"science\",\"question\":\"What is the energy source in hydrogen bombs?\",\"choices\":[\"Nuclear fission\",\"Chemical\",\"Nuclear fusion\",\"Solar\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\",\"question\":\"The largest unicellular organism, which can grow up to 20cm in diameter, belongs to which kingdom?\",\"choices\":[\"Plants\",\"Archaebacteria\",\"Fungi\",\"Protists\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\",\"question\":\"How is the number 50 written in Roman numerals?\",\"choices\":[\"L\",\"V\",\"X\",\"M\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\",\"question\":\"Who assissinated President John F. Kennedy?\",\"choices\":[\"John Wilkes Booth\",\"Lee Harvey Oswald\",\"James Earl Ray\",\"Marcus Julius Brutus\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Which of these popular bands had no bass guitar?\",\"choices\":[\"The Doors\",\"Pink Floyd\",\"The Beatles\",\"Jefferson Airplane\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\",\"question\":\"When was the first Nobel prize awarded?\",\"choices\":[\"1901\",\"1920\",\"1940\",\"1890\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\",\"question\":\"Statistically, what is the most deadly animal in Australia?\",\"choices\":[\"Jellyfish\",\"Honey bee\",\"Horse\",\"Bull shark\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\",\"question\":\"What is a baby seal called?\",\"choices\":[\"Calf\",\"Kid\",\"Cub\",\"Pup\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"What actor plays the role of Wolverine in modern X-Men movies from 2001-2014?\",\"choices\":[\"Jack Nickolson\",\"Channing Tatum\",\"Hugh Jackman\",\"Brad Pitt\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"What was Disney's first sci-fi animation?\",\"choices\":[\"Lilo and Stitch\",\"Atlantis\",\"Treasure Planet\",\"Wall E\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\",\"question\":\"Who is the author of the Harry Potter series?\",\"choices\":[\"George R.R. Martin\",\"Robin Hobb\",\"J.K. Rowling\",\"Glen Cook\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\",\"question\":\"What was Henry Ford's profession?\",\"choices\":[\"Electrician\",\"Enterpreneur\",\"Scientist\",\"Architect\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\",\"question\":\"In which country was Protestantism born?\",\"choices\":[\"England\",\"France\",\"Spain\",\"Germany\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\",\"question\":\"When did the Titanic sink?\",\"choices\":[\"1912\",\"1918\",\"1925\",\"1908\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\",\"question\":\"How long was the Hundred Years War?\",\"choices\":[\"93\",\"100\",\"116\",\"105\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\",\"question\":\"Which chemical element is the primary ingredient in semiconductors?\",\"choices\":[\"Carbon\",\"Silicon\",\"Nitrogen\",\"Barium\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"science\",\"question\":\"Which of these body parts is located in the abdomen?\",\"choices\":[\"Liver\",\"Kidney\",\"Heart\",\"Hypothalamus\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\",\"question\":\"What do you call the rate of change of velocity?\",\"choices\":[\"Speed\",\"Momentum\",\"Friction\",\"Acceleration\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\",\"question\":\"How many volcanos are on the surface of the earth (approximately)?\",\"choices\":[\"550\",\"1250\",\"300\",\"10200\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\",\"question\":\"Which country has the highest population density in the world?\",\"choices\":[\"China\",\"Monaco\",\"India\",\"Vatican City\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\",\"question\":\"In which ocean is the Mariana trench?\",\"choices\":[\"Atlantic\",\"Indian\",\"Pacific\",\"Antarctic\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\",\"question\":\"Which is the driest continent in the world?\",\"choices\":[\"Australia\",\"Africa\",\"North America\",\"Antarctica\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\",\"question\":\"What is the largest lake in the world?\",\"choices\":[\"Lake Superior\",\"Lake Michigan\",\"Lake Victoria\",\"Caspian Sea\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\",\"question\":\"What is the total number of time zones in the world?\",\"choices\":[\"24\",\"10\",\"4\",\"50\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\",\"question\":\"Which metropolitan city has the highest population?\",\"choices\":[\"Shanghai\",\"Tokyo\",\"New York\",\"Delhi\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\",\"question\":\"Which of the Seven Wonders of the World can be seen from space?\",\"choices\":[\"Machu Picchu\",\"Great Pyramid of Giza\",\"Great Wall of China\",\"Your mom\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\",\"question\":\"What is the longest river in the world?\",\"choices\":[\"Amazon\",\"Nile\",\"Yangtze\",\"Mississippi\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\",\"question\":\"How many total dots are on a pair of dice?\",\"choices\":[\"12\",\"54\",\"42\",\"21\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\",\"question\":\"What was the name of the first successfully launched man-made satellite?\",\"choices\":[\"Sputnik 1\",\"Explorer 1\",\"Echo 1\",\"Vanguard TV3\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\",\"question\":\"Which teeth are used to grind food?\",\"choices\":[\"Incisors\",\"Canines\",\"Comb\",\"Molar\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\",\"question\":\"How many of the solar system's planets (maximum) can be placed edge-to-edge into the space between the earth and the moon?\",\"choices\":[\"3\",\"4\",\"6\",\"8\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\",\"question\":\"Whose 100-meter dash record was broken by Usain Bolt in 2008?\",\"choices\":[\"Asafa Powell\",\"Maurice Greene\",\"Carl Lewis\",\"Justin Gatlin\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which NBA team won 72 out of 82 games in a season?\",\"choices\":[\"Los Angeles Lakers\",\"Boston Celtics\",\"Chicago Bulls\",\"San Antonio Spurs\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which city hosted the Summer Olympics in 2008\",\"choices\":[\"London\",\"Beijing\",\"Seoul\",\"Athens\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\",\"question\":\"Who is the oldest boxer to win a major heavyweight title?\",\"choices\":[\"George Foreman\",\"Muhammad Ali\",\"Mike Tyson\",\"Joe Frazier\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which team suffered the worst ever defeat (in goal-differential) in a semifinal at the FIFA World Cup?\",\"choices\":[\"South Korea\",\"United States\",\"Brazil\",\"Belgium\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\",\"question\":\"In which country was the first FIFA World Cup held?\",\"choices\":[\"Brazil\",\"Uruguay\",\"Italy\",\"United States\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\",\"question\":\"Who won the first ever Super Bowl?\",\"choices\":[\"Green Bay Packers\",\"Kansas City Chiefs\",\"New England Patriots\",\"Pittsburgh Steelers\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\",\"question\":\"What kind of cue sport involves only awards points if the target ball is rebounded off the table cushion into a designated pocket?\",\"choices\":[\"8-ball pool\",\"Speed pool\",\"Straight pool\",\"Bank pool\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\",\"question\":\"Who is the only athlete to play in both the Super Bowl and the Baseball World Series?\",\"choices\":[\"Russell Wilson\",\"Michael Jordan\",\"Deion Sanders\",\"Drew Brees\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which coach of the Los Angeles Lakers trademarked the term 'three-peat'?\",\"choices\":[\"Pat Riley\",\"Phil Jackson\",\"Byron Scott\",\"Jerry West\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\",\"question\":\"When was the last time England participated in the men's FIFA World Cup final match?\",\"choices\":[\"2006\",\"1966\",\"1986\",\"1974\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which tennis player has won the most Grand Slams in history?\",\"choices\":[\"Andre Agassi\",\"Pete Sampras\",\"John McEnroe\",\"Roger Federer\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\",\"question\":\"What is the only rule that has remained unchanged in cricket laws since 1744 AD?\",\"choices\":[\"Number of players per team\",\"Type of ball\",\"Length of the pitch\",\"Size of the field\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\",\"question\":\"Which cricketer has the most 'Man of the Match' awards in one-day international matches?\",\"choices\":[\"Ricky Ponting\",\"Sanath Jayasurya\",\"Sachin Tendulkar\",\"Jacques Kallis\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\",\"question\":\"In table tennis, a typical game ends when a player scores how many points?\",\"choices\":[\"21\",\"10\",\"15\",\"11\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\",\"question\":\"In the longest tennis Grand Slam final ever, Novak Djokovic beat which top tennis talent to win the 2012 Australian Open?\",\"choices\":[\"Roger Federer\",\"Rafael Nadal\",\"Andy Murray\",\"David Ferrer\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\", \"question\":\"What is the diameter of the Earth?\", \"choices\":[\"8000 miles\",\"12875 kilometers\",\"6952 nautical miles\",\"All of the above\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\", \"question\":\"What is the capital city of Spain?\", \"choices\":[\"Barcelona\",\"Madrid\",\"Seville\",\"Valencia\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\", \"question\":\"Which country hosted a Formula 1 race for the first time in 2011?\", \"choices\":[\"Russia\",\"India\",\"Brazil\",\"Australia\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"When did Margaret Thatcher become Prime Minister?\", \"choices\":[\"1968\",\"1992\",\"1985\",\"1979\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"Which is the 4th planet from the Sun?\", \"choices\":[\"Mars\",\"Pluto\",\"Jupiter\",\"Earth\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Who played the role of 'Neo' in the Matrix trilogy?\", \"choices\":[\"Pierce Brosnan\",\"Keanu Reaves\",\"Russell Crowe\",\"Laurence Fishburne\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\", \"question\":\"Which was the first team sport to be added to the Olympics?\", \"choices\":[\"Baseball\",\"Lacrosse\",\"Football/soccer\",\"Field hockey\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\", \"question\":\"Formerly East Pakistan, which nation won independence in 1971?\", \"choices\":[\"Iraq\",\"Nepal\",\"Sri Lanka\",\"Bangladesh\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which Batman villain was played by Arnold Schwarzenegger?\", \"choices\":[\"Dr. Doom\",\"Mr. Freeze\",\"Scarecrow\",\"The Joker\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\", \"question\":\"Of which country is Kabul the capital?\", \"choices\":[\"Afghanistan\",\"Pakistan\",\"Syria\",\"Bangladesh\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\", \"question\":\"What was the city of Chennai formerly known as?\", \"choices\":[\"Bombay\",\"Bangalore\",\"Cheyenne\",\"Madras\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"How many chambers are in a normal human heart?\", \"choices\":[\"12\",\"2\",\"4\",\"1\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\", \"question\":\"G-15 is an economic grouping of \", \"choices\":[\"First world countries\",\"Second world countries\",\"Third world countries\",\"All of the above\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\", \"question\":\"What does a fathometer measure?\", \"choices\":[\"Sound intensity\",\"Ocean depth\",\"Atmospheric pressure\",\"The speed of love\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\", \"question\":\"Where are the headquarters of Amnesty International?\", \"choices\":[\"London\",\"New York\",\"Cairo\",\"Paris\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\", \"question\":\"Pre-senile dementia is named after which neuropathologist?\", \"choices\":[\"Dr. Parkinson\",\"Dr. Alzheimer\",\"Dr. Amnesia\",\"Dr. Strange\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\", \"question\":\"Where is the Sea of Tranquility?\", \"choices\":[\"The Moon\",\"Brazil\",\"Australia\",\"India\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\", \"question\":\"What is responsible for performing computations in computers?\", \"choices\":[\"Random Access Memory\",\"Flash Memory\",\"Central Processing Unit\",\"Accelerometer\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\", \"question\":\"Which of these animals can vomit?\", \"choices\":[\"Horse\",\"Rat\",\"Guinea pig\",\"House cat\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\", \"question\":\"In a deck of cards, which is the only 'King' without a mustache?\", \"choices\":[\"Hearts\",\"Spades\",\"Clubs\",\"Diamonds\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\", \"question\":\"What is the common name for a groundnut?\", \"choices\":[\"Walnut\",\"Peanut\",\"Pistachio\",\"Almond\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\", \"question\":\"Venison comes from which animal?\", \"choices\":[\"Quail\",\"Cow\",\"Lamb\",\"Deer\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"general\", \"question\":\"Where are the headquarters of the African Union located?\", \"choices\":[\"Cairo\",\"Johannesburg\",\"Addis Ababa\",\"Lagos\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"general\", \"question\":\"Who invented vulcanized rubber?\", \"choices\":[\"Jethro Tull\",\"Karl Benz\",\"Édouard Michelin\",\"Charles Goodyear\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\", \"question\":\"Which two European countries are separated by the Pyrenees mountain range?\", \"choices\":[\"France and Spain\",\"Germany and Austria\",\"Sweden and Norway\",\"Belgium and Netherlands\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\", \"question\":\"What is the second largest country by area in Europe?\", \"choices\":[\"Russia\",\"Germany\",\"France\",\"Italy\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\", \"question\":\"Name the largest land-locked country in the world?\", \"choices\":[\"Paraguay\",\"Kazakhstan\",\"Mongolia\",\"Chad\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\", \"question\":\"Sierra Leone is in which continent?\", \"choices\":[\"South America\",\"Asia\",\"Europe\",\"Africa\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\", \"question\":\"The Suez Canal separates the Mediterranean Sea and the ________ sea\", \"choices\":[\"Red\",\"Dead\",\"Redemption\",\"Black\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\", \"question\":\"Victoria Falls borders which of these countries?\", \"choices\":[\"Mexico\",\"Zimbabwe\",\"Kenya\",\"Australia\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"Which is the oldest surviving printed book in the world?\", \"choices\":[\"Diamond Sutra\",\"Gutenberg Bible\",\"Madrid Codex\",\"Etruscan Gold Book\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\", \"question\":\"Who was the first American President to appear on TV?\", \"choices\":[\"Abraham Lincoln\",\"Franklin D. Roosevelt\",\"Harry S. Truman\",\"Woodrow Wilson\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"Which empire was ruled around 450BC by Xerxes I?\", \"choices\":[\"Babylonia\",\"Assyria\",\"Macrobia\",\"Achaemenid\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\", \"question\":\"When did Namibia officially gain independence from South Africa?\", \"choices\":[\"1990\",\"1915\",\"1968\",\"1856\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\", \"question\":\"Which country was never officially under colonial rule?\", \"choices\":[\"Kenya\",\"Liberia\",\"Mali\",\"Egypt\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"Which famous Mexican rebel was killed in 1923?\", \"choices\":[\"Ricardo Flores Magón\",\"Emiliano Zapata\",\"Pancho Villa\",\"Enrique Gorostieta\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\", \"question\":\"Which NBA team has won the most championships in history?\", \"choices\":[\"Chicago Bulls\",\"Los Angeles Lakers\",\"Boston Celtics\",\"San Antonio Spurs\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\", \"question\":\"This great boxer was known as the Brown Bomber: \", \"choices\":[\"Muhammad Ali\",\"Joe Louis\",\"George Foreman\",\"Joe Frazier\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\", \"question\":\"Name the horse that famously won the Triple Crown in 1973, becoming the first winner in 25 years.\", \"choices\":[\"Affirmed\",\"Man o' War\",\"Assault\",\"Secretariat\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\", \"question\":\"Who created the game of basketball?\", \"choices\":[\"James Naismith\",\"Walter Camp\",\"William Webb Ellis\",\"Alexander Cartwright\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\", \"question\":\"Which country has played in every FIFA World Cup?\", \"choices\":[\"Germany\",\"Uruguay\",\"Brazil\",\"Italy\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\", \"question\":\"The soccer match dubbed 'El Clásico' is played between: \", \"choices\":[\"Arsenal and Chelsea\",\"Brazil and Argentina\",\"Spain v Everybody\",\"Real Madrid and Barcelona\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\", \"question\":\"Who became the youngest ever winner of the Wimbledon men's single title in 1985?\", \"choices\":[\"Bjorn Borg\",\"Boris Becker\",\"John McEnroe\",\"Stefan Edburg\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\", \"question\":\"After their relocation, the Houston Oilers became which current team?\", \"choices\":[\"Tennessee Titans\",\"Houston Texans\",\"Jacksonville Jaguars\",\"Dallas Cowboys\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\", \"question\":\"The Stanley Cup is a championship trophy in which sport?\", \"choices\":[\"Rugby\",\"Ice Hockey\",\"Soccer\",\"Baseball\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"sports\", \"question\":\"What is the highest possible score in 10-pin bowling?\", \"choices\":[\"100\",\"190\",\"300\",\"250\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\", \"question\":\"Who has won the most Stanley Cups?\", \"choices\":[\"Henri Richard\",\"Jean Beliveau\",\"Wayne Gretzky\",\"Red Kelly\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"sports\", \"question\":\"In the US, which sport is overseen by the NCAA?\", \"choices\":[\"Professional curling\",\"Cricket\",\"High school football\",\"College basketball\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\", \"question\":\"A shuttlecock is used in _______: \", \"choices\":[\"Tennis\",\"Table tennis\",\"Volleyball\",\"Badminton\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"sports\", \"question\":\"Which American college basketball team did Michael Jordan play for?\", \"choices\":[\"University of Illinois\",\"Kansas\",\"University of North Carolina (Chapel Hill)\",\"Kentucky\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"sports\", \"question\":\"How many holes are in a full round of golf?\", \"choices\":[\"12\",\"1\",\"15\",\"18\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Saying which word allows Billy Batson to turn into Captain Marvel?\", \"choices\":[\"Abracadabra\",\"Alachazam\",\"Shazam\",\"Booyah\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"In the Toy Story series, what is Buzz Lightyear's catchphrase?\", \"choices\":[\"Reach for the sky!\",\"Someone's poisoned the waterhole!\",\"You're my favorite deputy!\",\"To infinty and beyond!\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"In which year was the oldest ever film made?\", \"choices\":[\"1925\",\"1888\",\"1904\",\"1897\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which movie featured a spaceship named 'Nostromo'?\", \"choices\":[\"Event Horizon\",\"Star Wars: The Empire Strikes Back\",\"Independence Day\",\"Alien\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Who was the first character to fart in a Disney film?\", \"choices\":[\"Aladdin\",\"Mulan\",\"Pumbaa\",\"Sebastian\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"In the Harry Potter series, what is Luna Lovegood's Patronus?\", \"choices\":[\"Beaver\",\"Rabbit\",\"Stag\",\"Owl\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Who killed Darth Vader?\", \"choices\":[\"Emperor Palpatine\",\"Luke Skywalker\",\"C-3PO\",\"Han Solo\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which  'Pirates of the Caribbean' character said: 'Compelled by greed we were, but now we are consumed by it'?\", \"choices\":[\"Jack Sparrow\",\"Murtogg\",\"Elizabeth Swann\",\"Hector Barbossa\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"What is the main antagonist in the 'Saw' series called?\", \"choices\":[\"Sawtooth\",\"Jigsaw\",\"Freddy Kruger\",\"Chucky\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"What was the first album released by rock band AC/DC?\", \"choices\":[\"Highway to Hell\",\"Back in Black\",\"High Voltage\",\"T.N.T.\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which of these actors was cast as James Bond?\", \"choices\":[\"Timothy Dalton\",\"Brent Briscoe\",\"James Cosmo\",\"Tom Cruise\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which famous detective made his debut in A Story in Scarlet?\", \"choices\":[\"C. Auguste Dupin\",\"Sherlock Holmes\",\"Tintin\",\"Rorschach\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Who came to be known as the Queen of Soul?\", \"choices\":[\"Whitney Houston\",\"Aretha Franklin\",\"Nina Simone\",\"Diana Ross\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Name the drummer of Led Zeppelin who died in 1980.\", \"choices\":[\"Ringo Starr\",\"Neil Peart\",\"Keith Moon\",\"John Bonham\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"entertainment\", \"question\":\"Which singer accompanied 50 Cent in this hit 'Candy Shop'?\", \"choices\":[\"Olivia\",\"Dido\",\"Rihanna\",\"Mary J Blige\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\", \"question\":\"In humans, which function is regulated by the medulla oblongata?\", \"choices\":[\"Breathing\",\"Memory retention\",\"Memory creation\",\"Emotion\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\", \"question\":\"'Pb' is the chemical symbol of which element?\", \"choices\":[\"Phosphorus\",\"Platinum\",\"Lead\",\"Zinc\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the most abundant gas in the Earth's atmosphere?\", \"choices\":[\"Oxygen\",\"Nitrogen\",\"carbon dioxide\",\"Hydrogen sulfide\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"science\", \"question\":\"How may planets in the solar system have moons?\", \"choices\":[\"8\",\"3\",\"6\",\"5\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\", \"question\":\"Who coined the three laws of motion?\", \"choices\":[\"Albert Einstein\",\"Gottfried Leibniz\",\"Marie Curie\",\"Isaac Newton\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the SI unit for intensity of light?\", \"choices\":[\"Candela\",\"Lumen\",\"Lux\",\"Talbot\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the densest naturally occurring metal?\", \"choices\":[\"Lead\",\"Osmium\",\"Tungsten\",\"Plutonium\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\", \"question\":\"What is the name of the Hindu holy river?\", \"choices\":[\"Brahmaputra\",\"Ganges\",\"Godavari\",\"Indus\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"general\", \"question\":\"Where was the first subway in the world built?\", \"choices\":[\"London\",\"New York\",\"Buenos Aires\",\"Paris\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"general\", \"question\":\"How many heads are carved into Mount Rushmore?\", \"choices\":[\"3\",\"5\",\"6\",\"4\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\", \"question\":\"What is the largest non-polar desert in the world?\", \"choices\":[\"Atacama\",\"Arabian\",\"Gobi\",\"Sahara\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\", \"question\":\"Which is the world's highest waterfall?\", \"choices\":[\"Johannesburg Falls\",\"Angel Falls\",\"Niagara Falls\",\"Vinnufossen\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\", \"question\":\"What is the oldest capital city in the Americas?\", \"choices\":[\"Brasília\",\"Buenos Aires\",\"Mexico City\",\"Washington DC\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\", \"question\":\"Which is the shallowest ocean?\", \"choices\":[\"Arctic\",\"Indian\",\"Pacific\",\"Atlantic\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\", \"question\":\"What is the oldest mountain range in the world?\", \"choices\":[\"Hamersley Range\",\"Waterburg Mountains\",\"Barberton Greenstone Belt\",\"Appalachian Mountains\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"world\", \"question\":\"Which two countries share the longest border in the world?\", \"choices\":[\"Russia-Kazakhstan\",\"USA-Canada\",\"Argentina-Chile\",\"India-Bangladesh\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"world\", \"question\":\"'Quetzal' is the currency of which South American country?\", \"choices\":[\"Peru\",\"Brazil\",\"Costa Rica\",\"Guatemala\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"world\", \"question\":\"Where is the tallest mountain in Africa located?\", \"choices\":[\"Tanzania\",\"Kenya\",\"Ethiopia\",\"Mozambique\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"world\", \"question\":\"Which is the only city located in two continents?\", \"choices\":[\"Mexico City\",\"Cairo\",\"Istanbul\",\"Tehran\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\", \"question\":\"Which US President was known for championing a manned moon mission?\", \"choices\":[\"Lyndon B Johnson\",\"Franklin D. Roosevelt\",\"John F. Kennedy\",\"Ronald Reagan\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\", \"question\":\"In 1973, the Commonwealth of the Bahamas gained independence from which country?\", \"choices\":[\"Great Britain\",\"Spain\",\"Italy\",\"USA\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\", \"question\":\"In which city did famed civil rights activist Rosa Parks refuse to give up her bus seat?\", \"choices\":[\"Greensboro, North Carolina\",\"Montgomery, Alabama\",\"Nashville, Tennessee\",\"Washington, D.C.\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"Which mystical faith healer was said to have close relations with the last Tsar of Russia?\", \"choices\":[\"El Nino Fidencio\",\"Nostradamus\",\"Theodore Zeldin\",\"Grigori Rasputin\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"history\", \"question\":\"When was the state of Israel founded?\", \"choices\":[\"around 5000 BC\",\"1867\",\"1948\",\"1776\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\", \"question\":\"Who was the first astronaut to visit space twice?\", \"choices\":[\"Gus Grissom\",\"Neil Armstrong\",\"Yuri Gagarin\",\"Buzz Aldrin\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"history\", \"question\":\"Who was the first astronaut to visit space?\", \"choices\":[\"Gus Grissom\",\"Neil Armstrong\",\"Yuri Gagarin\",\"Buzz Aldrin\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"history\", \"question\":\"When did the first manned lunar mission land on the moon?\", \"choices\":[\"1964\",\"1969\",\"1973\",\"NEVER\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"history\", \"question\":\"Who was the first American billionaire?\", \"choices\":[\"Andrew Carnegie\",\"W.K. Kellogg\",\"John Pierpont Morgan\",\"John D. Rockefeller\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"Other then mercury, which element is liquid at exactly room temperature?\", \"choices\":[\"Gallium\",\"Chlorine\",\"Bromine\",\"Cesium\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the hardest substance in the human body?\", \"choices\":[\"Collagen\",\"Cementum\",\"Dentin\",\"Enamel\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the rarest blood type in humans?\", \"choices\":[\"AB-\",\"A-\",\"B+\",\"O+\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\", \"question\":\"Which organelle is known as the 'powerhouse of the cell'?\", \"choices\":[\"Cytoplasm\",\"Mitochondrion\",\"Lysosome\",\"Chloroplast\"],\"correctAnswer\":1},\n" +
                "    {\"category\":\"science\", \"question\":\"What is the hottest planet in the solar system?\", \"choices\":[\"Mercury\",\"Jupiter\",\"Venus\",\"Mars\"],\"correctAnswer\":2},\n" +
                "    {\"category\":\"science\", \"question\":\"What is another name for 'black hole evaporation'?\", \"choices\":[\"Gravitational waves\",\"CMB radiation\",\"Schwarzschild radius\",\"Hawking radiation\"],\"correctAnswer\":3},\n" +
                "    {\"category\":\"science\", \"question\":\"Which animal creates the loudest sound produced by a living thing?\", \"choices\":[\"Blue whale\",\"Baleen whale\",\"Lion\",\"Your mom\"],\"correctAnswer\":0},\n" +
                "    {\"category\":\"science\", \"question\":\"What is known as the 'master gland' of the human body?\", \"choices\":[\"Thyroid gland\",\"Pituitary gland\",\"Pineal gland\",\"Pancreas\"],\"correctAnswer\":1}\n" +
                "  ]}\n";

        String resp2 = "{\n" +
                "\t\"questions\": [{\n" +
                "\t\t\t\"category\": \"I\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. 401(k) plan.\", \"2. investment portfolio.\", \"3. insurance plan.\", \"4. savings account.\"],\n" +
                "\t\t\t\"correctAnswer\": 0\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"II\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. dividends being charged. \", \"2. degree of liquidity.\", \"3. limit on withdrawals.\", \"4. higher potential return.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"III\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. Get a copy of the person’s three credit reports each year.\", \"2. Know the location of the banks where accounts are held.\", \"3. Have expenses that are not greater than income.\", \"4. Earn a degree from a post-secondary education institution.\"],\n" +
                "\t\t\t\"correctAnswer\": 2\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. Sales tax.\", \"2. City tax.\", \"3. Federal income tax.\", \"4. Property tax.\"],\n" +
                "\t\t\t\"correctAnswer\": 2\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"V\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. corporate bonds.\", \"2. growth stocks.\", \"3. stock mutual funds.\", \"4. municipal bonds.\"],\n" +
                "\t\t\t\"correctAnswer\": 3\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"V\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. issue a bond.\", \"2. issue common stock.\", \"3. borrow money from a commercial bank.\", \"4. borrow money from the government.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"V\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. an investment portfolio managed by the investor.\", \"2. an investment that holds a wide range of different investments instruments, providing diversification.\", \"3. usually less risky than investing in a savings account.\", \"4. guaranteed to increase in value.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"V\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. Preferred stock.\", \"2. Speculative stock.\", \"3. Common stock.\", \"4. Capital growth stock.\"],\n" +
                "\t\t\t\"correctAnswer\": 0\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"V\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. gain.\", \"2. loss.\", \"3. surplus.\", \"4. dividend.\"],\n" +
                "\t\t\t\"correctAnswer\": 0\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}, {\n" +
                "\t\t\t\"category\": \"IV\",\n" +
                "\t\t\t\"question\": \"Sample question?\",\n" +
                "\t\t\t\"choices\": [\"1. The lower the risk, the higher the cost of the insurance premium.\", \"2. The higher the risk, the higher the cost of the insurance premium.\", \"3. The higher the risk, the lower the cost of the insurance premium.\", \"4. There is no relationship between the risk and the cost of the insurance premium.\"],\n" +
                "\t\t\t\"correctAnswer\": 1\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        try {
            response = new JSONObject(resp2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        questionsHandling = QuestionsHandling.getInstance(this.getApplicationContext(), QUIZ_NUMBER);
        questionsHandling.setJsonObject(response);
        sIndividualQuestions = questionsHandling.getRandomQuestionSet(mQuizSize, QUIZ_NUMBER);
        mCurrentDisplayQuestion = questionsHandling.makeDisplayQuestionObject(sIndividualQuestions.get(mQuestionNumber));
//        mCardView = (CardView) findViewById(R.id.card_view);
//        mListView = (ListView)rootview.findViewById(R.id.choices_listview);
        mNumberTextView = (TextView)findViewById(R.id.questionNumber_textview);
        mCategoryTextView = (TextView)findViewById(R.id.category_textview);
        mSecondsTextview = (TextView)findViewById(R.id.seconds_display);
        mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
        mFrameLayout = (FrameLayout)findViewById(R.id.card_framelayout);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setMax(maxTime);
        mProgressBar.setProgress(mCurrentSeconds);
        mCountDownTimer = new CountDownTimer((mCurrentSeconds+2)*1000,1000) {
            int mTicknumber = 0;
            @Override
            public void onTick(long l) {
                mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
                mProgressBar.setProgress(mCurrentSeconds);
//                Log.d("timer", "ontick" + Integer.toString(mTicknumber++) + ": " + Integer.toString(mCurrentSeconds));
                if (mCurrentSeconds<=0){
                    mSecondsTextview.setTextColor(getResources().getColor(R.color.wrongAnswerRed));
                    if (mCurrentSeconds<0){
                        mFrameLayout.setClickable(false);
                    }
                }
                mCurrentSeconds -= 1;
            }

            @Override
            public void onFinish() {
                mSecondsTextview.setTextColor(getResources().getColor(R.color.darker_gray));
                mProgressBar.setProgress(0);
                IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
                sQuizScorer.addQuestionScorer(currentQuestion.questionNumber, currentQuestion.category, currentQuestion.correctAnswer, QuestionScorer.NO_ANSWER);
                goToNextQuestion();
                mTicknumber=0;
            }
        };

//        mNextQuestionButton = (Button)findViewById(R.id.buttonNextQuestion);
//        mPreviousQuestionButton = (Button)findViewById(R.id.buttonPreviousQuestion);

        setAndUpdateChoiceTextViews(mQuestionNumber);


//        mNextQuestionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goToNextQuestion();
//            }
//        });
//        mPreviousQuestionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goToPreviousQuestion();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mCountDownTimer!=null) {
            mCountDownTimer.cancel();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mCountDownTimer!=null){
            mCountDownTimer.cancel();
        }
        mCountDownTimer = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_QUESTION_NUMBER, mQuestionNumber);
        outState.putInt(KEY_QUIZ_NUMBER, QUIZ_NUMBER);
        outState.putInt(KEY_QUIZ_SIZE, mQuizSize);
        outState.putInt(KEY_CURRENT_SECONDS, mCurrentSeconds);
        super.onSaveInstanceState(outState);
    }

    @TargetApi(21)
    private void setupWindowAnimations(){
        Slide slide = (Slide)TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
        getWindow().setEnterTransition(slide);
    }

    //updates the mCurrentDisplayQuestion object and text of the respective textviews
    private void setAndUpdateChoiceTextViews(int questionNumber){
        mCurrentDisplayQuestion = questionsHandling.makeDisplayQuestionObject(sIndividualQuestions.get(questionNumber));
//        mQuestionView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_QUESTION));
//        mChoice1TextView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_CHOICE_1));
//        mChoice2TextView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_CHOICE_2));
//        mChoice3TextView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_CHOICE_3));
//        mChoice4TextView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_CHOICE_4));
        if (currentVersionCode>=13){
            updateFragmentAnimated();
        } else {
            updateFragmentTraditional();
        }
        mNumberTextView.setText(Integer.toString(mQuestionNumber+1));
        mCategoryTextView.setText(mCurrentDisplayQuestion.get(QuestionsHandling.INDEX_CATEGORY));

        if (mCountDownTimer==null){
            mCountDownTimer = new CountDownTimer((mCurrentSeconds+2)*1000,1000) {
                @Override
                public void onTick(long l) {
                    mProgressBar.setProgress(mCurrentSeconds);
                    mSecondsTextview.setText(Integer.toString(mCurrentSeconds));
                    mCurrentSeconds -= 1;
                }

                @Override
                public void onFinish() {
                    mProgressBar.setProgress(0);
                    IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
                    sQuizScorer.addQuestionScorer(currentQuestion.questionNumber, currentQuestion.category, currentQuestion.correctAnswer, QuestionScorer.NO_ANSWER);
                    goToNextQuestion();
//                    if (mQuestionNumber < mQuizSize) {
//                        this.start();
//                    }
                }
            };
        } else {
            mCountDownTimer.cancel();
        }

        mCountDownTimer.start();
    }

    //updates question number by +=1
    private void goToNextQuestion() {
        doVibration(hasVibrator);
        if (mQuestionNumber<mQuizSize-1) {
            mQuestionNumber += 1;
            setAndUpdateChoiceTextViews(mQuestionNumber);
            mSecondsTextview.setTextColor(getResources().getColor(R.color.darker_gray));
            mCurrentSeconds=maxTime;
            mCountDownTimer.cancel();
            mCountDownTimer.start();
        } else {
            endQuiz();
        }
    }

    //updates question number by -=1
    private void goToPreviousQuestion(){
        if (mQuestionNumber>0) {
            mQuestionNumber-=1;
            setAndUpdateChoiceTextViews(mQuestionNumber);
        }
    }

    private void updateFragmentTraditional(){
        android.support.v4.app.Fragment fragmentQuestion = FragmentQuestion.getInstance(mCurrentDisplayQuestion);
        getSupportFragmentManager().beginTransaction().replace(R.id.card_framelayout, fragmentQuestion).commit();
    }

    @TargetApi(13)
    private void updateFragmentAnimated(){
        android.app.Fragment fragmentQuestion = FragmentQuestionHoneycomb.getInstance(mCurrentDisplayQuestion);
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (mQuestionNumber>0) {
            fragmentTransaction

                    // Replace the default fragment animations with animator resources
                    // representing rotations when switching to the back of the card, as
                    // well as animator resources representing rotations when flipping
                    // back to the front (e.g. when the system Back button is pressed).
                    .setCustomAnimations(
                            R.animator.card_flip_right_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out);
        }
        fragmentTransaction
                // Replace any fragments currently in the container view with a
                // fragment representing the next page (indicated by the
                // just-incremented currentPage variable).
                .replace(R.id.card_framelayout, fragmentQuestion)

//                // Add this transaction to the back stack, allowing users to press
//                // Back to get to the front of the card.
//                .addToBackStack(null)

                // Commit the transaction.
                .commit();
    }

    public void addQuestionScorer(View v){
        int chosenAnswer = -1;
        switch (v.getId()){
            case R.id.choice1:
                chosenAnswer = 0;
                break;
            case R.id.choice2:
                chosenAnswer = 1;
                break;
            case R.id.choice3:
                chosenAnswer = 2;
                break;
            case R.id.choice4:
                chosenAnswer = 3;
                break;
        }
        IndividualQuestion currentQuestion = sIndividualQuestions.get(mQuestionNumber);
        int timeTaken = maxTime - mCurrentSeconds;
        try {
            sQuizScorer.addQuestionScorer(currentQuestion.questionNumber, currentQuestion.category, timeTaken, currentQuestion.correctAnswer, chosenAnswer);
//            Log.d("quizTracker", Integer.toString(mQuestionNumber) + ": chosen answer is " + Integer.toString(chosenAnswer) + " correct answer is " +Integer.toString(currentQuestion.correctAnswer));
        } finally {
            goToNextQuestion();
        }
    }

    public void endQuiz(){
        if (sQuizScorer.getQuestionScorers().size()==mQuizSize) {
            ArrayList<QuestionScorer> questionScorers = sQuizScorer.getQuestionScorers();
            if (questionScorers != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
                try {
                    Intent intent = new Intent(this, InsertRecordsService.class);
                    intent.putExtra(InsertRecordsService.EXTRA_SERICE_QUIZ_SIZE, mQuizSize);
                    intent.putExtra(InsertRecordsService.EXTRA_SERVICE_QUIZ_NUMBER, QUIZ_NUMBER);
                    startService(intent);
                } finally {
                    Intent intent = new Intent(this, ActivityPostQuiz.class);
                    intent.putExtra(ActivityPostQuiz.KEY_QUIZ_SIZE, mQuizSize);
                    intent.putExtra(ActivityPostQuiz.KEY_QUIZ_NUMBER, QUIZ_NUMBER);
                    startActivity(intent);
                }
            } else {
//                Log.d("ActivityQuiz", "null questionScorers object");
            }
        } else {
//            Log.d("ActivityQuiz","quiz size is too small");
        }
    }

    public void doVibration(boolean hasVibrator){
        if (hasVibrator){
            if (mVibrator == null){
                mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
            }
            mVibrator.vibrate(vibrationMillis);
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onPause();
    }

}
