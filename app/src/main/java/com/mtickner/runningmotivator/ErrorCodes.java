package com.mtickner.runningmotivator;

import java.util.HashMap;

public class ErrorCodes {

    private static final HashMap<Integer, String> errorCodes = new HashMap<>();

    //todo string resource

    static {
        // Codes starting with 1 are request related
        errorCodes.put(100, "Unauthorised request");

        // Codes starting with 2 are database related
        errorCodes.put(200, "Database connection error occurred");
        errorCodes.put(201, "Database error occurred");

        // Codes starting with 3 are user related
        errorCodes.put(300, "User ID not valid");

        // Codes starting with 4 are friend related


        // Codes starting with 5 are run related
        errorCodes.put(500, "Run ID not valid");
        errorCodes.put(501, "Distance not valid");
        errorCodes.put(502, "Time not valid");

        // Codes starting with 6 are challenge related
        errorCodes.put(600, "Challenge ID not valid");
    }

    //todo
    public static String GetErrorMessage(int errorCode) {
        return errorCodes.get(errorCode);
    }
}