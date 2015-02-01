package com.mtickner.runningmotivator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MiscHelper {
    // Methods that format
    // Method that returns a formatted time from seconds for display to the user
    public static String FormatSecondsToHoursMinutesSeconds(int seconds) {
        // Get minutes
        int minutes = seconds / 60;

        // Get hours
        int hours = minutes / 60;

        // Get modulus of seconds and minutes so that 59 is not exceeded
        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Method that formats doubles for distance and pace
    public static String FormatDouble(double doubleToFormat) {
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");

        return decimalFormat.format(doubleToFormat);
    }

    // Method that returns a Date object from database date string
    public static Date FormatDateFromDatabase(String date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method that returns a formatted date string for display to the user
    public static String FormatDateForDisplay(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm");
        return dateFormat.format(date);
    }


    // Methods that calculate and convert
    // Method that calculates running pace
    public static double CalculatePaceInMinutesPerKilometre(int timeTotalInSeconds, double distanceTotalInKilometres) {
        return (timeTotalInSeconds / distanceTotalInKilometres) / 60;
    }

    // Method that converts kilometres to miles for distance
    public static double ConvertKilometresToMiles(double distanceTotalInKilometres) {
        return (distanceTotalInKilometres * 0.621371192);
    }

    // Method that converts minutes per kilometre to minutes per mile for pace
    public static double ConvertMinutesPerKilometreToMinutesPerMile(double paceInMinutesPerKilometre) {
        return (paceInMinutesPerKilometre / 0.621371192);
    }


    // Methods that are used for challenge notifications
    // Method that starts the challenges service
    public static void StartChallengeService(Context context) {
        // Create the alarm pending intent
        Intent intent = new Intent(context, ChallengeReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule the repeating alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    // Method that returns an array list of unnotified challenges
    public static ArrayList<Challenge> GetUnnotifiedChallenges(ArrayList<Challenge> challengeArrayList) {
        ArrayList<Challenge> unnotifiedChallengeArrayList = new ArrayList<>();

        for (int i = 0; i < challengeArrayList.size(); i++) {
            Challenge challenge = challengeArrayList.get(i);

            if (!challenge.IsNotified()) {
                unnotifiedChallengeArrayList.add(challenge);
            }
        }

        return unnotifiedChallengeArrayList;
    }


    // Methods that log
    // Method that creates a log file
    public static BufferedWriter OpenLogFile(Context context) {
        // Get location logging preference
        if (Preferences.GetSettingLocationLoggingEnabled(context)) {
            try {
                Calendar now = Calendar.getInstance();

                // http://stackoverflow.com/questions/5726519/creating-and-storing-log-file-on-device-in-android
                File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mtickner.runningmotivator/RunAceLog" + now.getTimeInMillis() + ".txt");
                if (!logFile.exists()) {
                    logFile.getParentFile().mkdirs();
                    logFile.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(logFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                bufferedWriter.write("Logged at " + now.getTime() + "\n");
                bufferedWriter.write("name,latitude,longitude\n");
                bufferedWriter.flush();

                return bufferedWriter;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Method that appends a message to a log file
    public static void AppendLogFile(Context context, BufferedWriter bufferedWriter, String message) {
        // Get location logging preference
        if (Preferences.GetSettingLocationLoggingEnabled(context)) {
            try {
                bufferedWriter.write(message + "\n");
                bufferedWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method that closes a log file
    public static void CloseLogFile(Context context, BufferedWriter bufferedWriter) {
        // Get location logging preference
        if (Preferences.GetSettingLocationLoggingEnabled(context)) {
            try {
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Miscellaneous methods
    // Method that generates a random string of the specified length
    public static String GenerateRandomString(int length) {
        // Source: http://stackoverflow.com/a/18070256/1164058
        char[] characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        Random random = new SecureRandom();
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            // Picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(characterSet.length);
            result[i] = characterSet[randomCharIndex];
        }

        return new String(result);
    }

    // Create a notification
    public static void CreateNotification(Context context, Intent intent, int notificationId, String contentText) {
        // Define the notification intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        Notification notification = new Notification.Builder(context)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(contentText)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .build();

        // Set notification sound and vibration to defaults
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build and issue the notification
        notificationManager.notify(notificationId, notification);
    }

    // Method that creates a header text view for a list view
    public static View CreateListViewHeader(Context context, String text) {
        TextView headerText = new TextView(context);
        headerText.setText(text);
        headerText.setPadding(0, 50, 0, 50);
        headerText.setGravity(Gravity.CENTER_HORIZONTAL);
        headerText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        return headerText;
    }
}