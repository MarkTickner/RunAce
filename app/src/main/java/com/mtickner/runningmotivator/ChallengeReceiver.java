package com.mtickner.runningmotivator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ChallengeReceiver extends BroadcastReceiver {

    // Called when the challenge alarm manager is received
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the challenge service
        Intent challengeService = new Intent(context, ChallengeService.class);
        context.startService(challengeService);
    }
}