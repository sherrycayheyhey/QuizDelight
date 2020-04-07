package com.chromsicle.quizdelight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    //used to send the score result to the main activity
    public static final String EXTRA_SCORE = "extraScore";

    //keys to identify the saved values used when rotating the device
    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    //timer stuff, time and color
    private static final long COUNTDOWN_IN_MILLIS = 15000; //seconds*1000
    private ColorStateList textColorDefaultCd;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    //create variables to get references to all the views in the layout
    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rb4;
    private Button buttonConfirmNext;

    //radio button coloring
    private ColorStateList textColorDefaultRb;

    //variable for the ArrayList of questions
    private ArrayList<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;

    private int score;
    private boolean answered;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        //assign the variables to their views
        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.radio_button1);
        rb2 = findViewById(R.id.radio_button2);
        rb3 = findViewById(R.id.radio_button3);
        rb4 = findViewById(R.id.radio_button4);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        //stores the default color of the radio button and timer
        textColorDefaultRb = rb1.getTextColors();
        textColorDefaultCd = textViewCountDown.getTextColors();

        //only do these things if there isn't already an instance saved
        if (savedInstanceState == null) {
            //initialize the QuizDBHelper
            QuizDbHelp dbHelper = new QuizDbHelp(this);
            //take question list and fill it with data
            //calling this the first time will create the database, which didn't exist yet
            questionList = dbHelper.getAllQuestions();
            //get the total number of questions
            questionCountTotal = questionList.size();
            //shuffle the questions
            Collections.shuffle(questionList);

            //show the first question as soon as the activity starts
            showNextQuestion();
        } else {
            //restore the previous state
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);

            if (!answered) {
                startCountDown();
            } else {
                //have to update the correct colors
                updateCountDownText();
                showSolution();
            }
        }

        //button onclick listener
        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the question hasn't been answered
                if (!answered) {
                    //lock and check the answer
                    if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                        //one of the radio buttons was selected, so check the answer
                        checkAnswer();
                    } else {
                        //no answer was selected, so notify the user
                        Toast.makeText(QuizActivity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //the question was already answered, so show the next question
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion() {
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        rb4.setTextColor(textColorDefaultRb);
        //unselect all radio buttons when the next question is shown
        rbGroup.clearCheck();

        //display the next question if one exists
        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());
            rb4.setText(currentQuestion.getOption4());

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            //when "confirm next" button is clicked we want it to lock the answer instead of moving into the next question
            answered = false;
            //when the answer is locked, change the text to "next" but if it's not (false) chang it to "confirm"
            buttonConfirmNext.setText("confirm");

            //when the question starts, give the timer 30 seconds
            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            //there's no more questions
            finishQuiz();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                //the time skips the last onTick so it displays 1 even though it's finished, this fixes that
                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer(); //locks an answer if one is selected when timer runs out
            }
        }.start();
    }

    private void updateCountDownText() {
        //get the minutes and seconds
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        //make a string with the minutes and seconds
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        //set the timer to the minutes and seconds string
        textViewCountDown.setText(timeFormatted);

        //change text color if below 10 seconds
        if (timeLeftInMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    private void checkAnswer() {
        answered = true;

        countDownTimer.cancel();

        //get the selected radio button
        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        //turn the selected button into a number so it can be compared to our stored answer int
        //returs the index of the radiobutton that was selected and adds 1 since it starts at 0 and our answers were 1-4
        int answerNr = rbGroup.indexOfChild(rbSelected) + 1;
        //compare with entry in the database table
        if (answerNr == currentQuestion.getAnswerNr()){
            //it was correct, add to the score, change score textview
            score++;
            textViewScore.setText("Score: " + score);
        }
        //show the correct answer whether correct or incorrect
        showSolution();
    }

    private void showSolution() {
        //set all the answers to red then change the correct one to green
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);
        rb4.setTextColor(Color.RED);

        switch (currentQuestion.getAnswerNr()) {
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 1 is correct!");
                break;
            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 2 is correct!");
                break;
            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 3 is correct!");
                break;
            case 4:
                rb4.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 4 is correct!");
                break;
        }

        if (questionCounter < questionCountTotal) {
            //there's another question
            buttonConfirmNext.setText("Next");
        } else {
            //no more questions
            buttonConfirmNext.setText("Finish");
        }
    }

    private void finishQuiz() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        //later the result will be used in the main activity to check if it's a high score
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    //deal with the back button being pressed
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //if the time between pressing it twice is less than 2 seconds
            finishQuiz();
        } else {
            //more than 2 seconds have passed since back button was pressed
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    //when activity is finished, cancel the countdown timer so it's not running in the background
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //time has to have actually been started in order to cancel it, otherwise app will crash
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
}
