package com.chromsicle.quizdelight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.chromsicle.quizdelight.QuizContract.*;

import java.util.ArrayList;
import java.util.List;

//this class will use the QuizContract class constants to create the sqlite db

public class QuizDbHelp extends SQLiteOpenHelper {
    //constants
    private static final String DATABASE_NAME = "QuizDelight.db";
    private static final int DATABASE_VERSION = 1;

    //variable for the sqlite database, this holds the reference to the actual database so it can be used to add values
    private SQLiteDatabase db;

    public QuizDbHelp(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //save the database to the db variable
        this.db = db;

        //sqlite commands to create the database
        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION4 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NR + " INTEGER" +
                ")";

        //use the created string to create the database
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        fillQuestionsTable();

    }

    //if changes need to be made to the database they need to happen here,
    //changing the string above will only change the database the first time the apps runs
    //the database version number will have to be increased and the string schema changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //delete old table
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        //call onCreate to update the table with the new schema
        onCreate(db);

    }

    private void fillQuestionsTable() {
        Question q1 = new Question ("A is correct", "A", "B", "C", "D", 1);
        addQuestion(q1);
        Question q2 = new Question ("C is correct", "A", "B", "C", "D", 3);
        addQuestion(q2);
        Question q3 = new Question ("D is correct", "A", "B", "C", "D", 4);
        addQuestion(q3);
        Question q4 = new Question ("B is correct", "A", "B", "C", "D", 2);
        addQuestion(q4);
    }

    //used to insert question into the database
    private void addQuestion (Question question) {
        ContentValues cv = new ContentValues();
        //where you want to put it, what you want to put there
        cv.put(QuestionsTable.COLUMN_QUESTION, question.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, question.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, question.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, question.getOption3());
        cv.put(QuestionsTable.COLUMN_OPTION4, question.getOption4());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, question.getAnswerNr());
        db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    //the quiz questions have already been saved but this method will allow them to be retrieved
    public List<Question> getAllQuestions() {
        //make an arraylist
        //a new arrayrist is made because List is just an interface so you can't create a new List
        List<Question> questionList = new ArrayList<>();

        //get the reference to the database so we can get the data out of there
        //the first time this is called it will call the database onCreate method and create the database
        db = getReadableDatabase();

        //need a cursor to query the database
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);
        //move cursor to first entry or return false if there isn't one
        //query the question in "do" and fill a question object with its data
        //move to the next entry if it exists and do it again
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                //get the question out of COLUMN_QUESTION, save it to the question object (question.) as the question string (setQuestion)
                //repeat for the answer options and the answer number
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                //take the question object that was just created and filled with data and add it to the question list (questionList)
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }
}
