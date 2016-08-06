package com.dairyroadsolutions.accelplot;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static final int TRACE_COUNT = 3;
    private static final int CHART_COLUMN_COUNT = 1;
    private static final int SCREEN_BUFFER_COUNT = 3000;
    private static final int LINE_THICKNESS = 3;
    private static final boolean CYCLIC = true;
    private static final float F_SCALE_FACTOR = 1.0f/4096.0f;

    // Grid controls. It works best if they are even numbers
    private int iDivisionsX = 4;
    private int iDivisionsY = 10;

    // Chart trace controls
    private GLSurfaceView mChartSurfaceView;
    private TraceHelper classTraceHelper;
    private float fChScale[];
    private float fChOffset[];
    private static ToggleButton mStreamToggleButton;
    private static ToggleButton tbSaveData;
    private static ToggleButton tbAudioOut;

    private FilterHelper filter = new FilterHelper();

    // Data writing controls
    private boolean bWriteLocal = false;

    // Audio out controls
    private boolean bAudioOut = false;
    private AudioHelper mAudioHelper = new AudioHelper();
    // debug
    private static final String strTag = MainActivity.class.getSimpleName();



    // Arduino values are stored in the Bluetooth.java object

    // NOTE: All sampling and filtering information is defined
    // in the Bluetooth.java class.

    // Data write is in the Bluetooth class

    /**
     * Set the channel scale factor.  Each trace could have a different scale
     * factor, but for this instance all traces will be set to the same value.
     * @param fScaleFactor Universal scale factor
     */
    public void setChScale(float fScaleFactor){

        // Populate the array with the scale factors
        for( int idx = 0; idx< TRACE_COUNT; ++idx) {
            fChScale[idx] = fScaleFactor/(TRACE_COUNT +1.0f);
        }

        // Update dependent objects
        Bluetooth.classChartRenderer.setChScale(fChScale);

    }

    /**
     * Set the channel offsets to avoid overlaying the data
     */
    public void setChOffset(){

        float fIncrement = classTraceHelper.getTraceIncrement();
        float fStart = classTraceHelper.getTraceStart();

        // Populate the array with the scale factors
        for( int idx = 0; idx< TRACE_COUNT; ++idx) {
            fChOffset[idx] = fStart;
            fStart = fStart + fIncrement;
        }

        // Update the dependent objects
        Bluetooth.classChartRenderer.setChOffset(fChOffset);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mControlLayer;

        setContentView(R.layout.activity_main);

        mControlLayer = (LinearLayout)findViewById(R.id.Chart);

        mChartSurfaceView = new GLSurfaceView(this);

        mChartSurfaceView.setEGLConfigChooser(false);

        mControlLayer.addView(mChartSurfaceView);

        // Flags and initialization of the BlueTooth object
        Bluetooth.samplesBuffer=new SamplesBuffer[TRACE_COUNT];
        Bluetooth.vSetWriteLocal(bWriteLocal);

        // Flags for the data save button
        tbSaveData = (ToggleButton)findViewById(R.id.tbSaveData);
        tbSaveData.setEnabled(false);

        // Flags and init for the audio out
        tbAudioOut = (ToggleButton)findViewById(R.id.tbAudioOut);
        tbAudioOut.setEnabled(false);

        classTraceHelper = new TraceHelper(TRACE_COUNT);

        for(int i=0;i< TRACE_COUNT;i++)
        {
            Bluetooth.samplesBuffer[i] = new SamplesBuffer(SCREEN_BUFFER_COUNT, true);
        }

        Bluetooth.classChartRenderer = new ChartRenderer(this,SCREEN_BUFFER_COUNT,Bluetooth.samplesBuffer, TRACE_COUNT);
        Bluetooth.classChartRenderer.setCyclic(CYCLIC);
        Bluetooth.classChartRenderer.bSetDivisionsX(iDivisionsX);
        Bluetooth.classChartRenderer.bSetDivisionsY(iDivisionsY);

        fChScale = new float[TRACE_COUNT];
        setChScale(F_SCALE_FACTOR);

        fChOffset = new float[TRACE_COUNT];
        setChOffset();

        // Line thickness
        Bluetooth.classChartRenderer.setThickness(LINE_THICKNESS);

        // Number of columns of chart data
        Bluetooth.classChartRenderer.setChartColumnCount(CHART_COLUMN_COUNT);

        mChartSurfaceView.setRenderer(Bluetooth.classChartRenderer);

        // Initialize the Bluetooth object
        init();

        // Initialize the buttons
        ButtonInit();

        // Debug code to test display of screen buffers
        for( int idx=0; idx<SCREEN_BUFFER_COUNT; ++idx){
            Bluetooth.samplesBuffer[1].addSample((float)(idx>>2));
        }

        // Testing calls here. Remove comments to run them.
        filter.TestHarness();
        Bluetooth.samplesBuffer[0].TestHarness();

    }
    void init() {
        Bluetooth.gethandler(mUpdateHandler);
    }

    private void ButtonInit(){

        Button btnConnectButton;
        Button btnDiscconnectButton;
        final ToggleButton tglbtnScroll;

        // Configure the stream data button
        mStreamToggleButton = (ToggleButton)findViewById(R.id.tbStream);
        mStreamToggleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Bluetooth.connectedThread != null)
                {
                    Bluetooth.bStreamData = mStreamToggleButton.isChecked();
                }
            }

        });

        // Configure the Bluetooth connect button
        btnConnectButton = (Button)findViewById(R.id.bConnect);
        btnConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent("android.intent.action.BT1"));

            }


        });

        // Configure the Bluetooth disconnect button
        btnDiscconnectButton = (Button)findViewById(R.id.bDisconnect);
        btnDiscconnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mStreamToggleButton.setChecked(false);
                Bluetooth.bStreamData = false;
                Bluetooth.disconnect();
            }


        });

        // Configure the save data button
        tbSaveData = (ToggleButton)findViewById(R.id.tbSaveData);
        tbSaveData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bWriteLocal = mStreamToggleButton.isChecked();
                Bluetooth.vSetWriteLocal(bWriteLocal);
//                Log.d(strTag, ":HM:                  Toggling bWriteLocal");

            }
        });

        // Configure the audio out button
        tbAudioOut = (ToggleButton)findViewById(R.id.tbAudioOut);
        tbAudioOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bAudioOut = mStreamToggleButton.isChecked();
                mAudioHelper.vSetAudioOut(bAudioOut);
//                Log.d(strTag, ":HM:                    Toggling bAudioOut");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    Handler mUpdateHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){

                case Bluetooth.SUCCESS_DISCONNECT:
                    Toast.makeText(getApplicationContext(), "Disconnected!", Toast.LENGTH_LONG).show();
                    tbSaveData.setEnabled(false);
                    tbAudioOut.setEnabled(false);
                    break;

                case Bluetooth.SUCCESS_CONNECT:
                    Bluetooth.connectedThread = new Bluetooth.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                    Bluetooth.connectedThread.start();
                    tbSaveData.setEnabled(true);
                    tbAudioOut.setEnabled(true);
                    break;
            }

        }

    };


    /**
     * Added this to ensure the connection to the Bluetooth
     * device is closed.
     */
    protected void OnDestroy(){
        if( Bluetooth.connectedThread != null){
            Bluetooth.disconnect();
        }
    }


}