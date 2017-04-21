package re.notifica.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import re.notifica.util.Log;

/**
 * Created by joris on 21/04/17.
 */

public class InstallListener extends BroadcastReceiver {
    private static final String TAG = InstallListener.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String rawReferrerString = intent.getStringExtra("referrer");
        Log.i(TAG, "Received the following intent " + intent);
        if (rawReferrerString != null) {
            Log.i(TAG, "Received the following referrer " + rawReferrerString);
        }
    }
}
