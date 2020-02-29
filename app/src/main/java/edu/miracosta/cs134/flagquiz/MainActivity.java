package edu.miracosta.cs134.flagquiz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.miracosta.cs134.flagquiz.model.Country;
import edu.miracosta.cs134.flagquiz.model.JSONLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 3;
    public static final String REGIONS = "pref_numberOfChoices";
    public static final String CHOICES = "pref_regions" ;

    //Keep tracj of the current number of choices
    private int mChoices = 4;
    //Keep track of the current region selected
    private String mRegion = "All";

    private Button[] mButtons = new Button[4]; //array of 4 buttons, named button, button1
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz cryptographic not deterministic
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);

    }


    public void updateRegion(String region) //will look like North_America, in file it is North America
    {
        //only pull countries where the region matches
        mRegion = region.replaceAll("_", " ");

        //All regions are
    }

    public void updateChoices()
    {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();
      //  mButtons= findViewById(mButtons);


        // DONE: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = findViewById(R.id.questionNumberTextView);
        mFlagImageView = findViewById(R.id.flagImageView);
        mAnswerTextView = findViewById(R.id.answerTextView);

        // DONE: Put all 4 buttons in the array (mButtons)
        mButtons[0] = findViewById(R.id.button);
        mButtons[1] = findViewById(R.id.button2);
        mButtons[2] = findViewById(R.id.button3);
        mButtons[3] = findViewById(R.id.button4);



        // TODO: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        //in activity_main  Question %1$d of %2$d, d is integer, f is float, both args ints, so need two args
        //in strings.xml is "Question"
        //if we want to set text and look up string, getString
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));
        // 1 is the first arg, FLAGS-IN-Quiz consts


        // DONE: Load all the countries from the JSON file using the JSONLoader
        //hook up the buttons using onClick
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, "Error loading from JSON", e); //right way, tag is name of application
        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

        // DONE: Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }


    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {

        // DONE: Reset the number of correct guesses made
        mCorrectGuesses = 0;

        // DONE: Reset the total number of guesses the user made
        mTotalGuesses = 0;

        // DONE: Clear list of quiz countries (for prior games played)
        //leave all countries same, but change the other list
        mQuizCountriesList.clear(); //the size is zero after cleared. when add it will increase

        // DONE: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        //make sure no duplicates
        //starts at zero cuz nothing in there

        Country random;
        while(mQuizCountriesList.size() < FLAGS_IN_QUIZ)
        {
            random = mAllCountriesList.get(rng.nextInt(mAllCountriesList.size())) ; //random between 0 and 194

            // DONE: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
            if (!mQuizCountriesList.contains(random) && (random.getRegion().equals(mRegion) || mRegion.equals("All")))
                mQuizCountriesList.add(random);
            //check for dupes and if random region matches object

        }

        // DONE: Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    //next question
    private void loadNextFlag() {
        // DONE: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        //we have 10 random, first quest is first country
        mCorrectCountry = mQuizCountriesList.remove(0); //now list is size 9, remove takes it off


        // DONE: Clear the mAnswerTextView so that it doesn't show text from the previous question
        //assume still playing
        mAnswerTextView.setText("");


        // DONE: Display current question number in the mQuestionNumberTextView
        //code wrote before up there
        mQuestionNumberTextView.setText(getString(R.string.question, FLAGS_IN_QUIZ - mQuizCountriesList.size(),FLAGS_IN_QUIZ));

        // DONE: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets(); //load image from asset and put in image view

        // DONE: Get an InputStream to the asset representing the next flag
        try {
            InputStream stream = am.open(mCorrectCountry.getFileName());
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName() );  //name of the image for blind
            mFlagImageView.setImageDrawable(image);
        } catch (IOException e) {
            Log.e(TAG, "ERROR from loadNextFlag" + mCorrectCountry.getFileName(), e);
        }


        // DONE: and try to use the InputStream to create a Drawable
        // DONE: The file name can be retrieved from the correct country's file name.
        // DONE: Set the image drawable to the correct flag.

        // DONE: Shuffle the order of all the countries (use Collections.shuffle)

        //method called shuffle, like arrays.sort()
        Collections.shuffle(mAllCountriesList);
        //fill all with random and then replace 1 with correct
        // DONE: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // DONE: in the all countries list
        for (int i = 0; i < mButtons.length ; i++) {
            mButtons[i].setEnabled(true); //allows clicking - need to make sure enables
            mButtons[i].setText(mAllCountriesList.get(i).getName());
        }

        // DONE: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mButtons.length)].setText(mCorrectCountry.getName());
    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    //after the guess, all 4 buttons are checked for the right answer
    //attached to 4 buttons and need to know which button, so downcast view to button
    public void makeGuess(View v) {

        //Test for wiring of buttons
        //Toast.makeText(this, "Nope!", Toast.LENGTH_SHORT).show();

        // DONE: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;
        // DONE: Get the country's name from the text of the button
        String guess = clickedButton.getText().toString();

        mTotalGuesses++;
        // TODO: If the guess matches the correct country's name, increment the number of correct guesses,
        if (guess.equals(mCorrectCountry.getName()))
        {
            mCorrectGuesses++;
            //loop thorugh buttons and disable
            for (int i = 0; i < mButtons.length; i++) {
                mButtons[i].setEnabled(false); //disable so they don't touch again and get it wrong
            }
            mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer)); //can be different based on themes so need to go to resources first
            mAnswerTextView.setText(mCorrectCountry.getName());

            if(mCorrectGuesses < FLAGS_IN_QUIZ)
            {
                //code a delay 2000 ms = 2 sec using handler to load next flag
                //if more flags t guess, and they get it right, wait and load
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000) ; //build object on fly and need the time delay in the parameter
            }
            else //if wrong
            {
                //disable the clicked button


                //show incorrect in red
                //alert dialog
                //after all 10 flags tells percent and number of guesses and option to reset the quiz
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.results, mTotalGuesses, (double)(mTotalGuesses/mCorrectGuesses)  )); // this is wrong

                //set the positive button of the dialog
                //positive button means reset quiz - looks like a hyperlink
                //new OnClick and the Dialog comes up
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                //don't want them to be able to cancel the dialog cuz we'll be stuck at the game
                builder.setCancelable(false);
                builder.create();
                builder.show();

                mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));



            }

        }





        // DONE: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
        // TODO: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
        // TODO: with the statistics and an option to Reset Quiz

        // TODO: Else, the answer is incorrect, so display "Incorrect Guess!" in red
        // TODO: and disable just the incorrect button.



    }
    SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (key.equals(REGIONS)) //regions
                {
                    String region = sharedPreferences.getString(REGIONS, getString(R.string.default_region));
                    updateRegion(region); resetQuiz();
                }
            else if (key.equals(CHOICES)) //number of buttons
            {
                mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES, getString(R.string.default_choices)));
                updateChoices(); resetQuiz();
            }

            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show(); } };
}
