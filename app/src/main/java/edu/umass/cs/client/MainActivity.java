package edu.umass.cs.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import group6.umass.edu.location.MapsActivity;

/**
 * <p>The first activity/UI visible upon launching the application
 *
 * @author CS390MB
 *
 */
public class MainActivity extends Activity {


    /**
     * Variable to check if accelerometer is running
     */
    private boolean accelStarted = false;

    //variable to check if mic is recording
    private boolean micStarted = false;

    //variable to check if location is started
    private boolean locStarted = false;

    /**
     * Instance of this activity
     */
    private MainActivity activity;


    /*
     * Various UI components
     */
    private TextView accelXView, accelYView, accelZView;
    private TextView statusView, stepsView, speechStatusView;
    private CompoundButton accelButton, micButton, locButton;

    /**
     * Messenger service for exchanging messages with the background service
     */
    private Messenger mService = null;
    /**
     * Variable indicating if this activity is connected to the service
     */
    private boolean mIsBound;
    /**
     * Messenger receiving messages from the background service to update UI
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    public void showMap(View view) {
        Intent theIntent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(theIntent);
    }

    /**
     * Handler to handle incoming messages
     */
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Context_Service.MSG_ACTIVITY_STATUS:
                {
                    String activity = msg.getData().getString("activity");
                    //TODO: Display activity in UI
                    statusView.setText(activity);

                    break;
                }
                case Context_Service.MSG_STEP_COUNTER:
                {
                    stepsView.setText(""+msg.arg1);
                    break;
                }
                case Context_Service.MSG_ACCEL_VALUES:
                {
                    float accX = msg.getData().getFloat("accx");
                    float accY = msg.getData().getFloat("accy");
                    float accZ = msg.getData().getFloat("accz");
                    activity.setAccelValues(accX,accY,accZ);
                    break;
                }
                case Context_Service.MSG_ACCELEROMETER_STARTED:
                {
                    if(accelButton!=null) {
                        accelButton.setChecked(true);
                        accelStarted = true;
                        statusView.setText("Accelerometer Started");
                    }
                    break;
                }
                case Context_Service.MSG_ACCELEROMETER_STOPPED:
                {
                    if(accelButton!=null) {
                        accelButton.setChecked(false);
                        accelStarted = false;
                        statusView.setText("Accelerometer Stopped");
                    }
                    break;
                }
                case Context_Service.MSG_MICROPHONE_STARTED: {
                    if (micButton != null) {
                        micButton.setChecked(true);
                        micStarted = true;
                        speechStatusView.setText("Microphone Started");
                    }
                    break;
                }
                case Context_Service.MSG_MICROPHONE_STOPPED:
                {
                    if (micButton != null) {
                        micButton.setChecked(false);
                        micStarted = false;
                        speechStatusView.setText("Microphone Stopped");
                    }
                    break;
                }
                case Context_Service.MSG_SPEECH_VALUE:
                {
                    int speech = msg.getData().getInt("speech");

                    if(speech == 1){
                        speechStatusView.setText("voice");
                    }else{
                        speechStatusView.setText("unvoiced");
                    }
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Connection with the service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            statusView.setText("Attached to the Service");
            mIsBound = true;
            try {
                Message msg = Message.obtain(null, Context_Service.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mIsBound = false;
            mService = null;
            statusView.setText("Disconnected from the Service");
        }
    };

    /* Invoked when an activity is created
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        activity = this;
        super.onCreate(savedInstanceState);
        //Set Layout
        setContentView(R.layout.main);

        //Setting up text views
        statusView = (TextView) findViewById(R.id.StatusView);
        stepsView = (TextView) findViewById(R.id.StepCountView);
        accelXView = (TextView) findViewById(R.id.AccelXView);
        accelYView = (TextView) findViewById(R.id.AccelYView);
        accelZView = (TextView) findViewById(R.id.AccelZView);


        //new!! instantiate the speechStatus text view
        speechStatusView = (TextView)findViewById(R.id.statusSpeechView);


        //Start Background Service if not already started
        if(!Context_Service.isRunning()) {
            Intent cssBg = new Intent(activity,Context_Service.class);
            startService(cssBg);
        }


        //Bind to the service if it is already running
        bindToServiceIfIsRunning();

        //Determine if the accelerometer is on
        accelStarted = false;
        if(Context_Service.isAccelerometerRunning())
            accelStarted = true;

        //Set the buttons and the text accordingly
        accelButton = (ToggleButton) findViewById(R.id.StartButton);
        accelButton.setChecked(accelStarted);
        accelButton.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton btn,boolean isChecked) {
                        accelStarted = Context_Service.isAccelerometerRunning();
                        if(!accelStarted)
                            startAccelerometer();
                        else
                            stopAccelerometer();
                        //accelStarted = !accelStarted;
                    }
                }
        );

        //Determine if mic is on
        micStarted = false;
        if(Context_Service.isMicrophoneRunning())
            micStarted = true;

        //set the micButton and text for the mic_text_view
        micButton = (ToggleButton) findViewById(R.id.MicButton);
        micButton.setChecked(micStarted);
        micButton.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton btn,boolean isChecked) {
                        micStarted = Context_Service.isMicrophoneRunning();
                        if(!micStarted)
                            startMicrophone();
                        else
                            stopMicrophone();

                    }
                }
        );

        //get the loc button set up to use
        locButton = (ToggleButton) findViewById(R.id.loc_button);
        locButton.setChecked(locStarted);
        locButton.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton btn,boolean isChecked) {
                        locStarted = Context_Service.isLocationRunning();
                        if(!locStarted)
                            startLocation();
                        else
                            stopLocation();

                    }
                }
        );


    }

    private void stopLocation() {
        if(!mIsBound) {
            doBindService();

        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_STOP_LOCATION);
        }
    }

    private void startLocation() {
        if(!mIsBound) {
            doBindService();

            locButton.setChecked(false);
        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_START_LOCATION);
        }
    }


    /**
     * Binds this activity to the service if the service is already running
     */
    private void bindToServiceIfIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (Context_Service.isRunning()) {
            doBindService();//
            statusView.setText("Request to bind service");
        }
    }


    /**
     * This method is required to send a request to the background service.
     * In current application, we are not sending any message yet.
     * @param message
     */
    private void sendMessageToService(int message) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, message);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /**
     * Display accelerometer values in UI
     * @param accX
     * @param accY
     * @param accZ
     */
    public void setAccelValues(float accX, float accY, float accZ) {
        String text = String.format("%2.2f", accX);
        accelXView.setText(text);
        text = String.format("%2.2f", accY);
        accelYView.setText(text);
        text = String.format("%2.2f", accZ);
        accelZView.setText(text);
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }

    /**
     * Binds the activity to the background service
     */
    void doBindService() {
        bindService(new Intent(this, Context_Service.class), mConnection, Context.BIND_AUTO_CREATE);
        statusView.setText("Binding to Service");
    }

    /**
     * Unbind this activity from the background service
     */
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, Context_Service.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            statusView.setText("Unbinding from Service");
        }
    }

    /**
     * Sends Accelerometer Start Request
     */
    private void startAccelerometer() {
        if(!mIsBound) {
            doBindService();
            //In this case, start accelerometer won't work because service is not bound
            accelButton.setChecked(false);
        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_START_ACCELEROMETER);
        }
    }

    /**
     * Sends Accelerometer Stop Request
     */
    private void stopAccelerometer() {
        if(!mIsBound) {
            doBindService();
        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_STOP_ACCELEROMETER);
        }
    }

    /*
        Sends Microphone Start request
     */
    private void startMicrophone() {
        if(!mIsBound) {
            doBindService();
        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_START_MICROPHONE);
        }
    }

    /*
        Sends Microphone Stop request
     */
    private void stopMicrophone(){
        if(!mIsBound) {
            doBindService();
        }
        if(mIsBound) {
            sendMessageToService(Context_Service.MSG_STOP_MICROPHONE);
        }
    }

}