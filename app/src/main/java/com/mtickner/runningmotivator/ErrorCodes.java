package com.mtickner.runningmotivator;

import android.content.Context;

import java.util.HashMap;

public class ErrorCodes {

    // Method that returns the error message for the specified error code
    public static String GetErrorMessage(Context context, int errorCode) {
        // Initialise hash map of error codes and messages
        HashMap<Integer, String> errorCodes = GetErrorHashMap(context);

        return errorCodes.get(errorCode);
    }

    // Method that creates a hash map of error codes and messages
    private static HashMap<Integer, String> GetErrorHashMap(Context context) {
        // Initialise hash map of error codes and messages
        HashMap<Integer, String> errorCodes = new HashMap<>();

        // Codes starting with 1 are request related
        errorCodes.put(100, context.getString(R.string.error_message_100)); // Unauthorised request
        errorCodes.put(101, context.getString(R.string.error_message_101)); // Internet connection warning
        errorCodes.put(102, context.getString(R.string.error_message_102)); // Internet connection error
        errorCodes.put(103, context.getString(R.string.error_message_103)); // Generic app error

        // Codes starting with 2 are database related
        errorCodes.put(200, context.getString(R.string.error_message_200)); // Database connection error occurred
        errorCodes.put(201, context.getString(R.string.error_message_201)); // Database error occurred

        // Codes starting with 3 are user related
        errorCodes.put(300, context.getString(R.string.error_message_300)); // User ID not valid
        errorCodes.put(301, context.getString(R.string.error_message_301)); // Email not entered
        errorCodes.put(302, context.getString(R.string.error_message_302)); // Email not valid
        errorCodes.put(303, context.getString(R.string.error_message_303)); // Password not entered
        errorCodes.put(304, context.getString(R.string.error_message_304)); // Email and password match not found
        errorCodes.put(305, context.getString(R.string.error_message_305)); // Name not entered
        errorCodes.put(306, context.getString(R.string.error_message_306)); // Password must be at least 8 characters long
        errorCodes.put(307, context.getString(R.string.error_message_307)); // Email is already registered

        // Codes starting with 4 are friend related
        errorCodes.put(400, context.getString(R.string.error_message_400)); // Friend user ID not valid
        errorCodes.put(401, context.getString(R.string.error_message_401)); // Verification string not valid
        errorCodes.put(402, context.getString(R.string.error_message_402)); // Friend email not entered
        errorCodes.put(403, context.getString(R.string.error_message_403)); // Friend email not valid
        errorCodes.put(404, context.getString(R.string.error_message_404)); // Friend request already accepted

        // Codes starting with 5 are run related
        errorCodes.put(500, context.getString(R.string.error_message_500)); // Run ID not valid
        errorCodes.put(501, context.getString(R.string.error_message_501)); // Distance not valid
        errorCodes.put(502, context.getString(R.string.error_message_502)); // Time not valid

        // Codes starting with 6 are challenge related
        errorCodes.put(600, context.getString(R.string.error_message_600)); // Challenge ID not valid
        errorCodes.put(601, context.getString(R.string.error_message_601)); // Can only send challenges to friends

        return errorCodes;
    }
}