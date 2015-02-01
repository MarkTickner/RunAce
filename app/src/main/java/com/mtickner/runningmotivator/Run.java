package com.mtickner.runningmotivator;

import java.util.Date;

public class Run {
    // Private variables
    private int id;
    private User user;
    private double distanceTotal;
    private int totalTime;
    private Date dateRun;

    // Related public constants
    public final static String RUN_GSON = "com.mtickner.runningmotivator.run_gson";


    // Constructor for new run
    public Run(double distanceTotal, int totalTime) {
        SetDistanceTotal(distanceTotal);
        SetTotalTime(totalTime);
    }

    public Run(int id, User user, double distanceTotal, int totalTime, Date dateRun) {
        SetId(id);
        SetUser(user);
        SetDistanceTotal(distanceTotal);
        SetTotalTime(totalTime);
        SetDateRun(dateRun);
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


    public Double GetDistanceTotal() {
        return distanceTotal;
    }

    public void SetDistanceTotal(double distanceTotal) {
        this.distanceTotal = distanceTotal;
    }


    public int GetTotalTime() {
        return totalTime;
    }

    public void SetTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }


    public Date GetDateRun() {
        return dateRun;
    }

    public void SetDateRun(Date dateRun) {
        this.dateRun = dateRun;
    }
}