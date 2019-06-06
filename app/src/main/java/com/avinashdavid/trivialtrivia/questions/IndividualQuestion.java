package com.avinashdavid.trivialtrivia.questions;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by avinashdavid on 10/31/16.
 */

public class IndividualQuestion implements Parcelable {
    public String categoryText;
    public int category;
    public String question;
    public String[] choicesList;
    public int correctAnswer;
    public String rationale;

    public static final int CATEGORY_GENERAL = 0;
    public static final int CATEGORY_SCIENCE = 1;
    public static final int CATEGORY_WORLD = 2;
    public static final int CATEGORY_HISTORY = 3;
    public static final int CATEGORY_ENTERTAINMENT = 4;
    public static final int CATEGORY_SPORTS = 5;

    public static final ArrayList<String> categoryList = new ArrayList<String>(Arrays.asList("general","science","world","history","entertainment","sports"));

    public static ArrayList<String> getCategoryList() {
        return categoryList;
    }

    public IndividualQuestion(String category, String question, String[] answersList, int correctAnswer, String rationale) {
        this.categoryText = category;
        this.category = categoryList.indexOf(category);
        this.question = question;
        this.choicesList = answersList;
        this.correctAnswer = correctAnswer;
        this.rationale = rationale;
    }

    public IndividualQuestion(Parcel parcel){
        this.category = parcel.readInt();
        this.question = parcel.readString();
        String[] choicesList = new String[4];
        parcel.readStringArray(choicesList);
        this.choicesList = choicesList;
        this.correctAnswer = parcel.readInt();
        this.rationale = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(category);
        parcel.writeString(question);
        parcel.writeStringArray(choicesList);
        parcel.writeInt(correctAnswer);
        parcel.writeString(rationale);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new IndividualQuestion(parcel);
        }

        @Override
        public Object[] newArray(int i) {
            return new IndividualQuestion[i];
        }
    };
}
