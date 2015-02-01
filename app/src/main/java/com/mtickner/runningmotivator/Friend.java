package com.mtickner.runningmotivator;

import java.util.Date;

public class Friend {
    // Private variables
    private int id;
    private User user;
    private Status status;
    private Date statusDate;

    // Related public constants
    public final static String FRIEND_GSON = "com.mtickner.runningmotivator.friend_gson";


    // Constructor for a friend
    public Friend(int id, User user, Status status, Date statusDate) {
        SetId(id);
        SetUser(user);
        SetStatus(status);
        SetStatusDate(statusDate);
    }


    // Public getters and setters
    public int GetId() {
        return id;
    }

    public void SetId(int id) {
        this.id = id;
    }


    public User GetUser() {
        return user;
    }

    public void SetUser(User user) {
        this.user = user;
    }


    public enum Status {
        ALL, PENDING, ACCEPTED
    }

    public Status GetStatus() {
        return status;
    }

    public void SetStatus(Status status) {
        this.status = status;
    }

    public Date GetStatusDate() {
        return statusDate;
    }

    public void SetStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }
}