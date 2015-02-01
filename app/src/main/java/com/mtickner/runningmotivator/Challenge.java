package com.mtickner.runningmotivator;

import java.util.Date;

public class Challenge {
    // Private variables
    private int id;
    private Run run;
    private String message;
    private Date dateCreated;
    private boolean isNotified;
    private boolean isRead;
    private Date dateCompleted;


    // Related public constants
    public final static String CHALLENGE_GSON = "com.mtickner.runningmotivator.challenge_gson";
    public final static String CHALLENGE_COMPLETE = "com.mtickner.runningmotivator.challenge_complete";


    // Constructor for new challenge
    public Challenge(int id, Run run, String message, Date dateCreated, boolean isNotified, boolean isRead, Date dateCompleted) {
        SetId(id);
        SetRun(run);
        SetMessage(message);
        SetDateCreated(dateCreated);
        SetNotified(isNotified);
        SetRead(isRead);
        SetDateCompleted(dateCompleted);
    }


    // Public getters and setters
    public int GetId() {
        return id;
    }

    public void SetId(int id) {
        this.id = id;
    }


    public Run GetRun() {
        return run;
    }

    public void SetRun(Run run) {
        this.run = run;
    }


    public String GetMessage() {
        return message;
    }

    public void SetMessage(String message) {
        this.message = message;
    }


    public Date GetDateCreated() {
        return dateCreated;
    }

    public void SetDateCreated(Date dateTime) {
        this.dateCreated = dateTime;
    }


    public boolean IsNotified() {
        return isNotified;
    }

    public void SetNotified(boolean notified) {
        this.isNotified = notified;
    }


    public boolean IsRead() {
        return isRead;
    }

    public void SetRead(boolean read) {
        this.isRead = read;
    }


    public Date GetDateCompleted() {
        return dateCompleted;
    }

    public void SetDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}