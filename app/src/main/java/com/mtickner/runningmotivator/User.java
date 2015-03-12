package com.mtickner.runningmotivator;

import java.util.Date;

public class User {
    // Private variables
    private int id;
    private String name;
    private String email;
    private String password;
    private Date dateRegistered;
    private UserType userType;

    // Related public constants
    public final static String USER_GSON = "com.mtickner.runningmotivator.user_gson";


    // Constructor for user registration
    public User(String name, String email, String password) {
        SetName(name);
        SetEmail(email);
        SetPassword(password);
    }

    // Constructor for user pre-login
    public User(String email, String password) {
        SetEmail(email);
        SetPassword(password);
    }

    // Constructor for user post-login
    public User(int id, String name, String email, Date dateRegistered, UserType userType) {
        SetId(id);
        SetName(name);
        SetEmail(email);
        SetPassword(null);
        SetDateRegistered(dateRegistered);
        SetUserType(userType);
    }

    // Constructor for other constructors
    public User(int id, String name, String email, Date dateRegistered) {
        SetId(id);
        SetName(name);
        SetEmail(email);
        SetPassword(null);
        SetDateRegistered(dateRegistered);
    }


    // Public getters and setters
    public int GetId() {
        return id;
    }

    public void SetId(int id) {
        this.id = id;
    }


    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }


    public String GetEmail() {
        return email;
    }

    public void SetEmail(String email) {
        this.email = email;
    }


    public String GetPassword() {
        return password;
    }

    public void SetPassword(String password) {
        this.password = password;
    }


    public Date GetDateRegistered() {
        return dateRegistered;
    }

    public void SetDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }


    public UserType GetUserType() {
        return userType;
    }

    public void SetUserType(UserType userType) {
        this.userType = userType;
    }
}