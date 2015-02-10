package com.mtickner.runningmotivator;

import java.util.Date;

public class Badge {
    // Private variables
    private Type type;
    private int level;
    private Date dateAwarded;


    // Constructor for an awarded badge
    public Badge(Type type, int level) {
        SetType(type);
        SetLevel(level);
    }

    // Constructor for a badge
    public Badge(Type type, int level, Date dateAwarded) {
        SetType(type);
        SetLevel(level);
        SetDateAwarded(dateAwarded);
    }


    // Public getters and setters
    public enum Type {
        RUN, CHALLENGE
    }

    public Type GetType() {
        return type;
    }

    public void SetType(Type type) {
        this.type = type;
    }


    public int GetLevel() {
        return level;
    }

    public void SetLevel(int level) {
        this.level = level;
    }


    public Date GetDateAwarded() {
        return dateAwarded;
    }

    public void SetDateAwarded(Date dateAwarded) {
        this.dateAwarded = dateAwarded;
    }
}