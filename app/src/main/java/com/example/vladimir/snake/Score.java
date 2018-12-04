package com.example.vladimir.snake;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Score extends RealmObject {
    private int scoreValue;
    @Ignore
    private int highestScore;

    public Score() {
        this.scoreValue = 0;
    }


    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public void increaseScoreValue(int i) {
        this.scoreValue += i;
    }
}
