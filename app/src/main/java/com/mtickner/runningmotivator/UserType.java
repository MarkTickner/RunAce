package com.mtickner.runningmotivator;

public class UserType {
    // Private variables
    private int id;
    private String name;
    private String description;


    // Constructor
    public UserType(int id, String name, String description) {
        SetId(id);
        SetName(name);
        SetDescription(description);
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


    public String GetDescription() {
        return description;
    }

    public void SetDescription(String description) {
        this.description = description;
    }
}