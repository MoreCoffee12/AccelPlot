package com.dairyroadsolutions.accelplot;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.pow;

public class MainActivity extends Activity {

    private static final int TRACE_COUNT = 4;
    private static final int CHART_COLUMN_COUNT = 1;
    private static final int SCREEN_BUFFER_COUNT = 3000;
    private static final int LINE_THICKNESS = 3;
    private static final boolean CYCLIC = true;

    // I truncated the accelerometer outputs from 2^15 bits to 2^12 bits to allow for the
    // address to included in the 2-byte structure. See "Firmware.ino" for the implementation
    // details
    private static float fAccelCountsPerG = 1024.0f;
    private static final float F_ADC_COUNTS_PER_VOLT = fAccelCountsPerG/5.0f;

    // The plot area for each trace has to be scaled to +1 to -1
    private static final float F_SCALE_FACTOR_ACC = 0.50f/2048.0f;
    private static final float F_SCALE_FACTOR_GYRO = 0.25f/1024f;

    // Grid controls. It works best if they are even numbers
    private static final int I_DIVISIONS_X = 20;
    private static final int I_DIVISIONS_Y = 4;
    private static final float V_PER_DIV =(0.5f/F_SCALE_FACTOR_GYRO)/(I_DIVISIONS_Y*F_ADC_COUNTS_PER_VOLT);
    TextView[] tvTrace = new TextView[TRACE_COUNT+1];
    private int iLabelSize;

    // Chart trace controls
    private float[] fChScale;
    private float[] fChOffset;
    private Button _bDisconnect;
    private TextView _tvControl;
    private ToggleButton _tbStream;
    private TextView _tvDataStorage;
    private ToggleButton _tbSaveData;
    private TextView _tvAudioOut;
    private ToggleButton _tbAudioOut;
    private TextView _tvCh1;
    private RadioGroup _rgCh1;
    private TextView _tvCh2;
    private RadioGroup _rgCh2;
    private TextView _tvArduino;
    private Button _bInst;
    private Spinner _spFreq;
    private Spinner _spAccRange;
    private TextView _tvFile;
    private EditText _etFileSamples;

    private final FilterHelper filter = new FilterHelper();

    // Data writing controls
    private boolean bWriteLocal = false;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();

    // Application preferences
    private static SharedPreferences sharedPref;

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

        // Populate the array with the scale factors for the accelerometer channels
        for( int idx = 0; idx < (TRACE_COUNT-1); ++idx) {
            fChScale[idx] = fScaleFactor/(TRACE_COUNT +1.0f);
        }

        // The scale factor for the gyro/ADC channel is set separately from the accelerometers
        fChScale[TRACE_COUNT-1]=F_SCALE_FACTOR_GYRO;

        // Update dependent objects
        Bluetooth.classChartRenderer.setChScale(fChScale);

    }

    /**
     * Set the channel offsets to avoid overlaying the data
     */
    public void setChOffset(){

        float fIncrement = Bluetooth.classChartRenderer.classChart.classTraceHelper.getTraceIncrement();
        float fStart = Bluetooth.classChartRenderer.classChart.classTraceHelper.getTraceStart();

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

        // Local variables
        GLSurfaceView glChartSurfaceView;

        setContentView(R.layout.activity_main);

        // Setup the gl surface
        LinearLayout llControlLayer = (LinearLayout)findViewById(R.id.Chart);
        glChartSurfaceView = new GLSurfaceView(this);
        glChartSurfaceView.setEGLConfigChooser(false);
        llControlLayer.addView(glChartSurfaceView);


        // Scale the location of the grid division labels
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int iHeightDisp = displaymetrics.heightPixels;
//        int width = displaymetrics.widthPixels;

        // We have to scale the labels for smaller displays
        iLabelSize = 15;
        if( iHeightDisp < 750 ){
            iLabelSize = (int)((float)iHeightDisp / 50.0f);
        }


        // Add the vertical axis labels
        FrameLayout flTemp = (FrameLayout)findViewById(R.id.flChartStuff);

        int idxText;
        for( idxText = 0; idxText<=TRACE_COUNT; ++idxText){
            tvTrace[idxText] = new TextView(this);
            tvTrace[idxText].setText("");
            tvTrace[idxText].setTextSize(iLabelSize);
            tvTrace[idxText].setPadding((iLabelSize/2)+1,(iLabelSize/2)+1, 0, 0);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
            flTemp.addView(tvTrace[idxText], params);

        }

        // Horizontal axis labels
        --idxText;
        tvTrace[idxText].setPadding((iLabelSize/2)+1,(iLabelSize/2)+1, (iLabelSize/2)+1, (iLabelSize/2)+1);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, (Gravity.BOTTOM | Gravity.END) );
        tvTrace[idxText].setLayoutParams(params);

        // User prefs
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        // Flags and initialization of the BlueTooth object
        Bluetooth.samplesBuffer=new SamplesBuffer[TRACE_COUNT];
        Bluetooth.vSetWriteLocal(bWriteLocal);

        // Flags for the disconnect button
        _bDisconnect = (Button)findViewById(R.id.bDisconnect);
        _bDisconnect.setEnabled(false);
        _bDisconnect.setVisibility(View.GONE);

        // Flags for the data stream button
        _tbStream = (ToggleButton)findViewById(R.id.tbStream);
        _tbStream.setEnabled(false);
        _tbStream.setVisibility(View.GONE);
        _tvControl = (TextView) findViewById(R.id.tvControl);
        _tvControl.setVisibility(View.GONE);

        // Flags for the data save button
        _tbSaveData = (ToggleButton)findViewById(R.id.tbSaveData);
        _tbSaveData.setEnabled(false);
        _tbSaveData.setVisibility(View.GONE);

        _tvDataStorage = (TextView)findViewById(R.id.tvDataStorage);
        _tvDataStorage.setVisibility(View.GONE);

        // Flags and init for the audio out
        _tbAudioOut = (ToggleButton)findViewById(R.id.tbAudioOut);
        _tbAudioOut.setEnabled(false);
        _tbAudioOut.setVisibility(View.GONE);

        _tvAudioOut = (TextView)findViewById(R.id.tvAudioOut);
        _tvAudioOut.setVisibility(View.GONE);

        // Initialze audio mappings
        _tvCh1 = (TextView)findViewById(R.id.tvCh1);
        _tvCh2 = (TextView)findViewById(R.id.tvCh2);
        _rgCh1 = (RadioGroup)findViewById(R.id.radio_ADC_to_Ch1);
        _rgCh2 = (RadioGroup)findViewById(R.id.radio_ADC_to_Ch2);

        for(int i=0;i< TRACE_COUNT;i++)
        {
            Bluetooth.samplesBuffer[i] = new SamplesBuffer(SCREEN_BUFFER_COUNT, true);
        }

        // Arduino control mappings
        _tvArduino = (TextView)findViewById(R.id.tvArduino);
        _bInst = (Button)findViewById(R.id.bInst);
        _spFreq = (Spinner)findViewById(R.id.spFreq);
        _spAccRange = (Spinner)findViewById(R.id.spAccRange);
        _tvFile = (TextView)findViewById(R.id.tvFile);
        _etFileSamples = (EditText)findViewById(R.id.etFileSamples);
        _etFileSamples.setFilters(new InputFilter[]{ new InputFilterMinMax("1","7200000")});
        List<String> listFreq = new ArrayList<>();
        for( int iOCRA = 1; iOCRA<256; ++iOCRA){
            listFreq.add(strListItem(dGetFreq(256, iOCRA), iOCRA));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                R.layout.freq_spinner, listFreq);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spFreq.setAdapter(dataAdapter);

        List<String> listAccRange = new ArrayList<>();
        listAccRange.add("MPU6050_ACCEL_FS_2");
        listAccRange.add("MPU6050_ACCEL_FS_4");
        listAccRange.add("MPU6050_ACCEL_FS_8");
        listAccRange.add("MPU6050_ACCEL_FS_16");
        ArrayAdapter<String> accAdapter = new ArrayAdapter<>(this,
                R.layout.freq_spinner, listAccRange);
        accAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        _spAccRange.setAdapter(accAdapter);
        vUpdateArduinoControls(true);

        // Set controls values
        vGetUserPrefs();
        vUpdateChMapsEnabled(false);

        Bluetooth.classChartRenderer = new ChartRenderer(this,SCREEN_BUFFER_COUNT,Bluetooth.samplesBuffer, TRACE_COUNT);
        Bluetooth.classChartRenderer.setCyclic(CYCLIC);
        Bluetooth.classChartRenderer.bSetDivisionsX(I_DIVISIONS_X);
        Bluetooth.classChartRenderer.bSetDivisionsY(I_DIVISIONS_Y);

        fChScale = new float[TRACE_COUNT];
        setChScale(F_SCALE_FACTOR_ACC);

        fChOffset = new float[TRACE_COUNT];
        setChOffset();

        // Line thickness
        Bluetooth.classChartRenderer.setThickness(LINE_THICKNESS);

        // Number of columns of chart data
        Bluetooth.classChartRenderer.setChartColumnCount(CHART_COLUMN_COUNT);

        glChartSurfaceView.setRenderer(Bluetooth.classChartRenderer);

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


    /**
     * Pass the message handler to the Bluetooth class
     */
    void init() {
        Bluetooth.gethandler(mUpdateHandler);
    }


    /**
     * When streaming stops, this need to be halted as well
     */
    private void vStopStreamDep(){

        _tbSaveData.setChecked(false);
        _tbSaveData.setEnabled(false);
        _tbSaveData.setVisibility(View.GONE);
        _tvDataStorage.setVisibility(View.GONE);
        vUpdateSaveData();

        vUpdateChMapsEnabled(false);
        _tbAudioOut.setChecked(false);
        _tbAudioOut.setEnabled(false);
        _tbAudioOut.setVisibility(View.GONE);
        _tvAudioOut.setVisibility(View.GONE);
        vUpdateAudioOut();

    }

    /**
     * Handles process that should be affected by change in the SaveData button status
     */
    private void vUpdateSaveData(){
        bWriteLocal = _tbStream.isChecked();
        Bluetooth.vSetWriteLocal(bWriteLocal);
        Bluetooth.vSetWritePending(true);
    }

    /**
     * Update the radio buttons to reflect the internal state of the Bluetooth buttons.
     */
    private void vUpdateChMaps(){

        // Channel 1
        if(Bluetooth.isbADC1ToCh1Out()){
            _rgCh1.check(R.id.radio_ADC1_Ch1);
        }
        if(Bluetooth.isbADC2ToCh1Out()){
            _rgCh1.check(R.id.radio_ADC2_Ch1);
        }
        if(Bluetooth.isbADC3ToCh1Out()){
            _rgCh1.check(R.id.radio_ADC3_Ch1);
        }

        //Channel 2
        if(Bluetooth.isbADC1ToCh2Out()){
            _rgCh2.check(R.id.radio_ADC1_Ch2);
        }
        if(Bluetooth.isbADC2ToCh2Out()){
            _rgCh2.check(R.id.radio_ADC2_Ch2);
        }
        if(Bluetooth.isbADC3ToCh2Out()){
            _rgCh2.check(R.id.radio_ADC3_Ch2);
        }

    }

    /**
     * Construct the string for the spinner control
     * @param dFreq     Frequency, double
     * @param iOCRA     Timer0 register value
     * @return String formatted for list
     */
    private String strListItem(double dFreq, int iOCRA){
        return String.format(Locale.US, "%.1f Hz (%d)", dFreq, iOCRA);
    }

    /***
     * Calculate the frequency from the Timer0 OCRA0 value
     * @param iPre      Pre-scalar value
     * @param iOCRA     Register value
     * @return REturns the frequency as a double
     */
    private double dGetFreq(int iPre, int iOCRA){
        return ( (16000000.0 / iPre)/ (double)(iOCRA+1));

    }

    /**
     * Toggles the controls associated with the Arduino configuration
     * @param bEnabled  New status value
     */
    private void vUpdateArduinoControls(boolean bEnabled){

        if( bEnabled){
            _tvArduino.setVisibility(View.VISIBLE);
            _spFreq.setVisibility(View.VISIBLE);
            _bInst.setVisibility(View.VISIBLE);
            _spAccRange.setVisibility(View.VISIBLE);
            _tvFile.setVisibility(View.VISIBLE);
            _etFileSamples.setVisibility(View.VISIBLE);
        }
        else{
            _tvArduino.setVisibility(View.GONE);
            _spFreq.setVisibility(View.GONE);
            _bInst.setVisibility(View.GONE);
            _spAccRange.setVisibility(View.GONE);
            _tvFile.setVisibility(View.GONE);
            _etFileSamples.setVisibility(View.GONE);
        }

    }

    /**
     * Toggles the enabled status of the audio channel mapping buttons
     * @param bEnabled  New enabled status value
     */
    private void vUpdateChMapsEnabled(boolean bEnabled){

        int iRadBut = _rgCh1.getChildCount();
        RadioButton rbTemp;

        // This is for the buttons
        for (int iBut=0; iBut<iRadBut; iBut++){

            // Channel 1 mappings
            rbTemp = ((RadioButton) _rgCh1.getChildAt(iBut));
            rbTemp.setEnabled( bEnabled );
            if( bEnabled ){
                rbTemp.setVisibility(View.VISIBLE);
            }
            else{
                rbTemp.setVisibility(View.GONE);
            }

            //Channel 2 mappings
            rbTemp = ((RadioButton) _rgCh2.getChildAt(iBut));
            rbTemp.setEnabled( bEnabled );
            if( bEnabled ){
                rbTemp.setVisibility(View.VISIBLE);
            }
            else{
                rbTemp.setVisibility(View.GONE);
            }
        }

        // This is for the channel labels
        if( bEnabled ){
            _tvCh1.setVisibility(View.VISIBLE);
            _tvCh2.setVisibility(View.VISIBLE);
        }else{
            _tvCh1.setVisibility(View.GONE);
            _tvCh2.setVisibility(View.GONE);
        }

    }


    /**
     * Get the user preferences
     */
    private void vGetUserPrefs(){

        Bluetooth.setbADC1ToCh1Out(sharedPref.getBoolean(getString(R.string.radio_ADC1_Ch1), true));
        Bluetooth.setbADC2ToCh1Out(sharedPref.getBoolean(getString(R.string.radio_ADC2_Ch1), true));
        Bluetooth.setbADC3ToCh1Out(sharedPref.getBoolean(getString(R.string.radio_ADC3_Ch1), true));

        Bluetooth.setbADC1ToCh2Out(sharedPref.getBoolean(getString(R.string.radio_ADC1_Ch2), true));
        Bluetooth.setbADC2ToCh2Out(sharedPref.getBoolean(getString(R.string.radio_ADC2_Ch2), true));
        Bluetooth.setbADC3ToCh2Out(sharedPref.getBoolean(getString(R.string.radio_ADC3_Ch2), true));

        _spFreq.setSelection(sharedPref.getInt("OCR0A", 248));
        _spAccRange.setSelection(sharedPref.getInt("ACCFS", 0));
        _etFileSamples.setText(String.format("%d", sharedPref.getInt("SAMPSAVE",15000)));

        vUpdateChMaps();

    }
    /**
     * Update the user preferences
     */
    private void vUpdateUserPrefs(){

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.radio_ADC1_Ch1), Bluetooth.isbADC1ToCh1Out());
        editor.putBoolean(getString(R.string.radio_ADC2_Ch1), Bluetooth.isbADC2ToCh1Out());
        editor.putBoolean(getString(R.string.radio_ADC3_Ch1), Bluetooth.isbADC3ToCh1Out());
        editor.putBoolean(getString(R.string.radio_ADC1_Ch2), Bluetooth.isbADC1ToCh2Out());
        editor.putBoolean(getString(R.string.radio_ADC2_Ch2), Bluetooth.isbADC2ToCh2Out());
        editor.putBoolean(getString(R.string.radio_ADC3_Ch2), Bluetooth.isbADC3ToCh2Out());
        editor.putInt("OCR0A",_spFreq.getSelectedItemPosition());
        editor.putInt("ACCFS",_spAccRange.getSelectedItemPosition());
        editor.putInt("SAMPSAVE", Integer.parseInt(_etFileSamples.getText().toString()));
        editor.commit();

    }


    /**
     * Handles process that change with the AudioOut button status
     */
    private void vUpdateAudioOut(){
        Bluetooth.bAudioOut = _tbAudioOut.isChecked();
        Bluetooth.classAudioHelper.vSetAudioOut(Bluetooth.bAudioOut);
    }

    /**
     * This function locks the orientation. The real problem is that my code doesn't handle
     * orientation changes while streaming data from the Bluetooth device.
     * TODO - understand what needs to be changed to allow orientation changes while streaming.
     */
    private void vLockOrient(){
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    /**
     * Update plot grid labels
     */
    private void vUpdateGridLabels(){

        // Local variables
        float dGPerDiv;
        float fTimePerDiv;

        FrameLayout flTemp = (FrameLayout)findViewById(R.id.flChartStuff);
        int iDiff = (int)((float)flTemp.getHeight()/(float)TRACE_COUNT);

        // Update the scaling values
        int iOCRA = sharedPref.getInt("OCR0A", 249);
        Bluetooth.vSetSampleFreq(dGetFreq(256, iOCRA));
        fAccelCountsPerG = (float)(2048.0f/pow(2.0f, (float)(1+sharedPref.getInt("ACCFS", 0))));
        dGPerDiv = (1.0f/F_SCALE_FACTOR_ACC)/(I_DIVISIONS_Y* fAccelCountsPerG);

        // Accelerometer labels
        int idxText;
        for( idxText = 0; idxText<TRACE_COUNT; ++idxText){

            tvTrace[idxText].setText("Ch" + String.valueOf(idxText+1) + ", " + String.valueOf(dGPerDiv) + "g's per div");
            if(idxText == (TRACE_COUNT-1)){
                tvTrace[idxText].setText("Ch" + String.valueOf(idxText+1) + ", " + String.valueOf(V_PER_DIV) + " volt per div");
            }
            tvTrace[idxText].setBackgroundColor(Color.BLACK);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
            params.setMargins((iLabelSize/2)+1,(iLabelSize/2)+1+(iDiff*idxText),0,0);
            tvTrace[idxText].setLayoutParams(params);

        }

        // Horizontal label goodness
        fTimePerDiv = SCREEN_BUFFER_COUNT / ((float)Bluetooth.dGetSampleFrequency() * (float)I_DIVISIONS_X );
        tvTrace[idxText].setText(String.format("%.1f sec. per div", fTimePerDiv));

    }

    /**
     * Open a URL link
     * @param url   String with the URL link
     */
    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    /**
     * Initialize the button controls and listeners
     */
    private void ButtonInit(){

        Button btnConnectButton;
        Button btnDiscconnectButton;

        // Configure the stream data button
        _tbStream.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Lock the orientation
                vLockOrient();

                // This section handles the thread
                if (Bluetooth.connectedThread != null)
                {
                    Bluetooth.bStreamData = _tbStream.isChecked();
                }

                // This section handles the dependant buttons
                if (_tbStream.isChecked()){

                    _tbSaveData.setVisibility(View.VISIBLE);
                    _tbSaveData.setEnabled(true);
                    _tvDataStorage.setVisibility(View.VISIBLE);

                    vUpdateChMapsEnabled(true);

                    _tbAudioOut.setVisibility(View.VISIBLE);
                    _tbAudioOut.setEnabled(true);
                    _tvAudioOut.setVisibility(View.VISIBLE);

                    vUpdateGridLabels();

                }else{
                    vStopStreamDep();
                }
            }

        });

        // Configure the Bluetooth connect button
        btnConnectButton = (Button)findViewById(R.id.bConnect);
        btnConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Update user prefs for samples to save
                vUpdateUserPrefs();
                Bluetooth.vSetFileSamples(sharedPref.getInt("SAMPSAVE",15000));

                startActivity(new Intent("android.intent.action.BT1"));

            }


        });

        // Configure the Bluetooth disconnect button
        _bDisconnect = (Button)findViewById(R.id.bDisconnect);
        _bDisconnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                _tbStream.setChecked(false);
                Bluetooth.bStreamData = false;
                Bluetooth.disconnect();
                vStopStreamDep();
            }


        });

        // Configure the save data button
        _tbSaveData = (ToggleButton)findViewById(R.id.tbSaveData);
        _tbSaveData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vUpdateSaveData();
            }
        });

        // Configure the Build It button to link to Instructables
        _bInst.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            goToUrl("https://www.instructables.com/id/Realtime-MPU-6050A0-Data-Logging-With-Arduino-and-");

            }


        });

        // Configure the Arduino frequency selection spinner
        _spFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                vUpdateUserPrefs();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configure the Accelerometer range selection spinner
        _spAccRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                vUpdateUserPrefs();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configure the channel 1 radio buttons
        _rgCh1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.radio_ADC1_Ch1:
                        Bluetooth.setbADC1ToCh1Out(true);
                        Bluetooth.setbADC2ToCh1Out(false);
                        Bluetooth.setbADC3ToCh1Out(false);
//                        Log.d(strTag, ":HM:                     ADC1_Ch1 Active: ");
                        break;
                    case R.id.radio_ADC2_Ch1:
                        Bluetooth.setbADC1ToCh1Out(false);
                        Bluetooth.setbADC2ToCh1Out(true);
                        Bluetooth.setbADC3ToCh1Out(false);
//                        Log.d(strTag, ":HM:                     ADC2_Ch1 Active: ");
                        break;
                    case R.id.radio_ADC3_Ch1:
                        Bluetooth.setbADC1ToCh1Out(false);
                        Bluetooth.setbADC2ToCh1Out(false);
                        Bluetooth.setbADC3ToCh1Out(true);
//                        Log.d(strTag, ":HM:                     ADC3_Ch1 Active: ");
                        break;
                }

                vUpdateUserPrefs();

            }
        });

        // Configure the channel 2 radio buttons
        _rgCh2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.radio_ADC1_Ch2:
                        Bluetooth.setbADC1ToCh2Out(true);
                        Bluetooth.setbADC2ToCh2Out(false);
                        Bluetooth.setbADC3ToCh2Out(false);
//                        Log.d(strTag, ":HM:                     ADC1_Ch2 Active: ");
                        break;
                    case R.id.radio_ADC2_Ch2:
                        Bluetooth.setbADC1ToCh2Out(false);
                        Bluetooth.setbADC2ToCh2Out(true);
                        Bluetooth.setbADC3ToCh2Out(false);
//                        Log.d(strTag, ":HM:                     ADC2_Ch2 Active: ");
                        break;
                    case R.id.radio_ADC3_Ch2:
                        Bluetooth.setbADC1ToCh2Out(false);
                        Bluetooth.setbADC2ToCh2Out(false);
                        Bluetooth.setbADC3ToCh2Out(true);
//                        Log.d(strTag, ":HM:                     ADC3_Ch2 Active: ");
                        break;
                }

                vUpdateUserPrefs();

            }
        });

        // Configure the audio out button
        _tbAudioOut = (ToggleButton)findViewById(R.id.tbAudioOut);
        _tbAudioOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vUpdateAudioOut();
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
                    _bDisconnect.setEnabled(false);
                    _bDisconnect.setVisibility(View.GONE);
                    _tvControl.setVisibility(View.GONE);
                    _tbStream.setVisibility(View.GONE);
                    _tbStream.setEnabled(false);
                    vUpdateArduinoControls(true);
                    break;

                case Bluetooth.SUCCESS_CONNECT:
                    Bluetooth.connectedThread = new Bluetooth.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                    Bluetooth.connectedThread.start();
                    _bDisconnect.setVisibility(View.VISIBLE);
                    _bDisconnect.setEnabled(true);
                    _tvControl.setVisibility(View.VISIBLE);
                    _tbStream.setVisibility(View.VISIBLE);
                    _tbStream.setEnabled(true);
                    vUpdateArduinoControls(false);
                    break;

                case Bluetooth.FILE_WRITE_DONE:
                    _tbSaveData.setChecked(false);
                    _tbSaveData.setEnabled(false);
                    break;
            }

        }

    };

}