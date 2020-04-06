package com.chromsicle.quizdelight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

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
    private List<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;

    private int score;
    private boolean answered;

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

        //stores the default color of the radio button
        textColorDefaultRb = rb1.getTextColors();

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
        } else {
            //there's no more questions
            finishQuiz();
        }
    }

    private void finishQuiz() {
        finish();
    }
}
