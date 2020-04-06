package com.chromsicle.quizdelight;

//a contract class, holds the constants for the database table
//this class only provides the constants
//the open helper class will use these constants to create the actual sqlite database


import android.provider.BaseColumns;

public final class QuizContract {
    //final means this can't be subclassed
    //this private constructor means we can't create an object of it, it's like an insurance policy
    //this class is only a container for these constants
    private QuizContract() {}


    //BaseColumns is an interface, Ctrl + B to see the declaration
    //BaseCoulmns provides two more constants, id and count
    //we'll only use id for creating another column in the database which will store the id
    //this id constant could have been created like the ones below but some Android framework classes
    //need this exact naming convention
    public static class QuestionsTable implements BaseColumns {
        //public because I want to access them outside this class
        //static because I want to access them without needing an instance of this QuestionsTable
        //final because I don't want to change them
        public static final String TABLE_NAME = "quiz_questions";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_OPTION1 = "option1";
        public static final String COLUMN_OPTION2 = "option2";
        public static final String COLUMN_OPTION3 = "option3";
        public static final String COLUMN_OPTION4 = "option4";
        public static final String COLUMN_ANSWER_NR = "answer_nr";
    }

}

