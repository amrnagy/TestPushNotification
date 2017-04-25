package com.example.mostafa.testpushnotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import java.net.MalformedURLException;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MFPPush push; // Push client
    private MFPPushNotificationListener notificationListener; // Notification listener to handle a push sent to the phone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView buttonText = (TextView) findViewById(R.id.button_text);

        try {
            // initialize SDK with IBM Bluemix application ID and route
            // You can find your backendRoute and backendGUID in the Mobile Options section on top of your Bluemix application dashboard
            // TODO: Please replace <APPLICATION_ROUTE> with a valid ApplicationRoute and <APPLICATION_ID> with a valid ApplicationId
            BMSClient.getInstance().initialize(this,
                    "https://vy301-b77-menofpush.mybluemix.net/",
                    "812ddb86-e207-4046-b523-86c2b5c02057", BMSClient.REGION_US_SOUTH);
        }
        catch (MalformedURLException mue) {
            this.setStatus("Unable to parse Application Route URL\n Please verify you have entered your Application Route and Id correctly", false);
            buttonText.setClickable(false);
        }

        // Grabs push client sdk instance
        push = MFPPush.getInstance();
        // Initialize Push client, TODO: Please replace <APPLICATION_ID> with a valid ApplicationId
        push.initialize(this, "812ddb86-e207-4046-b523-86c2b5c02057","c3d9749f-329c-461f-b33b-92fa89cf12a7");

        // Create notification listener and enable pop up notification when a message is received
        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive(final MFPSimplePushNotification message) {
                Log.i(TAG, "Received a Push Notification: " + message.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Received a Push Notification")
                                .setMessage(message.getAlert())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                    }
                });
            }
        };

    }

    /**
     * Called when the register device button is pressed.
     * Attempts to register the device with your push service on Bluemix.
     * If successful, the push client sdk begins listening to the notification listener.
     *
     * @param view the button pressed
     */
    public void registerDevice(View view) {

        TextView buttonText = (TextView) findViewById(R.id.button_text);
        buttonText.setClickable(false);

        TextView responseText = (TextView) findViewById(R.id.response_text);
        responseText.setText("Registering for notifications");

        Log.i(TAG, "Registering for notifications");

        // Creates response listener to handle the response when a device is registered.
        MFPPushResponseListener registrationResponselistener = new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                setStatus("Device Registered Successfully", true);
                Log.i(TAG, "Successfully registered for push notifications, " + response);
                push.listen(notificationListener);
            }

            @Override
            public void onFailure(MFPPushException exception) {
                String errMessage = " " + exception.getErrorMessage();

                setStatus("Error registering for push notifications:" + errMessage, false);
                Log.e(TAG, errMessage);
                push = null;
            }
        };

        // Attempt to register device using response listener created above
        push.registerDevice(registrationResponselistener);

    }

    // If the device has been registered previously, hold push notifications when the app is paused
    @Override
    protected void onPause() {
        super.onPause();

        if (push != null) {
            push.hold();
        }
    }

    // If the device has been registered previously, ensure the client sdk is still using the notification listener from onCreate when app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        if (push != null) {
            push.listen(notificationListener);
        }
    }

    /**
     * Manipulates text fields in the UI based on initialization and registration events
     * @param messageText String main text view
     * @param wasSuccessful Boolean dictates top 2 text view texts
     */
    private void setStatus(final String messageText, boolean wasSuccessful){
        final TextView responseText = (TextView) findViewById(R.id.response_text);
        final TextView topText = (TextView) findViewById(R.id.top_text);
        final TextView bottomText = (TextView) findViewById(R.id.bottom_text);
        final TextView buttonText = (TextView) findViewById(R.id.button_text);
        final String topStatus = wasSuccessful ? "Yay!" : "Bummer";
        final String bottomStatus = wasSuccessful ? "You Are Connected" : "Something Went Wrong";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonText.setClickable(true);
                responseText.setText(messageText);
                topText.setText(topStatus);
                bottomText.setText(bottomStatus);
            }
        });
    }
}