package com.dairyroadsolutions.accelplot;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.lang.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Bluetooth extends Activity implements OnItemClickListener{

	public static void gethandler(Handler handler){//Bluetooth handler
		mHandler = handler;
	}
	static Handler mHandler = new Handler();

	static ConnectedThread connectedThread;
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int FILE_WRITE_DONE = 4;
    protected static final int SUCCESS_DISCONNECT = 3;
    protected static final int SUCCESS_CONNECT = 2;
	public static boolean bStreamData = false;
	ArrayAdapter<String> listAdapter;
	ListView listView;
	static BluetoothAdapter btAdapter;
    static BluetoothDevice selectedDevice;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter;
	BroadcastReceiver receiver;

	// debug
	private static final String _strTag = MainActivity.class.getSimpleName();

    // Sampling frequency, in hertz. This is set in the Arduino code, see "Firmware.ino" and
    // "ISR Frequency Ranges.xlsx" for details
    private static double dSampleFreq = 250.0;
    private static final float F_OFFSET_COUNT = 4095.0f;

    // This implementation is not using the filter; however I thought I would leave the code in
    // case someone wants to use it.
    public static FilterHelper DSPfilter = new FilterHelper();


    //-------------------------------------------------------------------------
	// Arduino accelerometer data
    //-------------------------------------------------------------------------
	//
	// This is the number of samples written to each file. The numeric value equals
    // the number of seconds of data to store.
	public static int iFileSampleCount = (int) dSampleFreq * 1;

	// Storage arrays for the accelerometer data
    public static float[] fX_Accel = new float[iFileSampleCount];
    public static float[] fY_Accel = new float[iFileSampleCount];
    public static float[] fZ_Accel = new float[iFileSampleCount];

	//This channel can be a gyro or the ADC, depending how how the firmware is configured
	//in the Arduino
    public static float[] fX_Gyro = new float[iFileSampleCount];

    //This describes the location for the next sample to be written in the storage arrays
	public static int idxBuff = 0;

	// Output buffer
    public static SamplesBuffer[] samplesBuffer;
    public static ChartRenderer classChartRenderer;

	//-------------------------------------------------------------------------
	// Data output
	//-------------------------------------------------------------------------
	public static final FileHelper fhelper = new FileHelper(iFileSampleCount);
    private static boolean bWriteLocal = true;
    private static boolean bWritePending = true;

	//-------------------------------------------------------------------------
	// Audio output
	//-------------------------------------------------------------------------
	public static boolean bAudioOut = false;
	public static AudioHelper classAudioHelper = new AudioHelper();

    private static boolean bADC1ToCh1Out = false;
    private static boolean bADC2ToCh1Out = false;
    private static boolean bADC3ToCh1Out = false;
    private static boolean bADC4ToCh1Out = false;
    private static boolean bADC1ToCh2Out = false;
    private static boolean bADC2ToCh2Out = false;
    private static boolean bADC3ToCh2Out = false;
    private static boolean bADC4ToCh2Out = false;


    // Getter and setter goodness below
    public static boolean isbADC1ToCh1Out() {
        return bADC1ToCh1Out;
    }

    public static void setbADC1ToCh1Out(boolean bADC1ToCh1Out) {
        Bluetooth.bADC1ToCh1Out = bADC1ToCh1Out;
    }

    public static boolean isbADC2ToCh1Out() {
        return bADC2ToCh1Out;
    }

    public static void setbADC2ToCh1Out(boolean bADC2ToCh1Out) {
        Bluetooth.bADC2ToCh1Out = bADC2ToCh1Out;
    }

    public static boolean isbADC3ToCh1Out() {
        return bADC3ToCh1Out;
    }

    public static void setbADC3ToCh1Out(boolean bADC3ToCh1Out) {
        Bluetooth.bADC3ToCh1Out = bADC3ToCh1Out;
    }

    public static boolean isbADC4ToCh1Out() {
        return bADC4ToCh1Out;
    }

    public static void setbADC4ToCh1Out(boolean bADC4ToCh1Out) {
        Bluetooth.bADC4ToCh1Out = bADC4ToCh1Out;
    }

    public static boolean isbADC1ToCh2Out() {
        return bADC1ToCh2Out;
    }

    public static void setbADC1ToCh2Out(boolean bADC1ToCh2Out) {
        Bluetooth.bADC1ToCh2Out = bADC1ToCh2Out;
    }

    public static boolean isbADC2ToCh2Out() {
        return bADC2ToCh2Out;
    }

    public static void setbADC2ToCh2Out(boolean bADC2ToCh2Out) {
        Bluetooth.bADC2ToCh2Out = bADC2ToCh2Out;
    }

    public static boolean isbADC3ToCh2Out() {
        return bADC3ToCh2Out;
    }

    public static void setbADC3ToCh2Out(boolean bADC3ToCh2Out) {
        Bluetooth.bADC3ToCh2Out = bADC3ToCh2Out;
    }

    public static boolean isbADC4ToCh2Out() {
        return bADC4ToCh2Out;
    }

    public static void setbADC4ToCh2Out(boolean bADC4ToCh2Out) {
        Bluetooth.bADC4ToCh2Out = bADC4ToCh2Out;
    }

    public static double dGetSampleFrequency() {return dSampleFreq; }
    public static void vSetSampleFreq(double dFreq) {dSampleFreq = dFreq;}

    public static void vSetWritePending( boolean bIn) {bWritePending = bIn;}

    /**
     * Initialize the storage arrays and samples. This also sets the
     * idxBuff pointer back zero.
     * *
     * @param iSamples Number of samples in each array.
     */
    public static void vSetFileSamples(int iSamples ){
        iFileSampleCount = iSamples;
        fX_Accel = new float[iFileSampleCount];
        fY_Accel = new float[iFileSampleCount];
        fZ_Accel = new float[iFileSampleCount];
        fX_Gyro = new float[iFileSampleCount];
        idxBuff=0;
        fhelper.vSetSamples(iSamples);
    }



    /**
     * Terminate the Bluetooth connection
     */
    public static void disconnect()
    {
        if (connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    /**
     * Interface method to set the write local flag
     * @param bNewFlag  Set this to true to start writing data locally, false to stop.
     */
    public static void vSetWriteLocal (boolean bNewFlag){
        bWriteLocal = bNewFlag;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		init();
		if (btAdapter==null){
			Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_LONG).show();
			finish();
		}else{
			if (!btAdapter.isEnabled()){
				turnOnBT();
			}
			getPairedDevices();
			startDiscovery();
		}

	}

	private void startDiscovery()
    {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}

	private void turnOnBT() {
		// TODO Something in here crashes when no rfcomm enabled devices are paired.
		// Need to dig into this and figure out why.
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}

	private void getPairedDevices() {
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size()>0){
			for(BluetoothDevice device:devicesArray){
				pairedDevices.add(device.getName());
			}
		}
	}

	private void init(){

		// Initialize the data buffer
		for (int idx=0; idx< iFileSampleCount; ++idx)
		{
			fX_Accel[idx] = 0.0f;
			fY_Accel[idx] = 0.0f;
		}

        Log.d(_strTag, ":HM:                   Init");

		listView = (ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
		listView.setAdapter(listAdapter);

		// Get the Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(_strTag, ":HM:                   btAdapter" + btAdapter.getName());
        Log.d(_strTag, ":HM:                   btAdapter Enabled: " + btAdapter.isEnabled());

        // Enable Bluetooth
        int REQUEST_ENABLE_BT = 0;
        if (!btAdapter.isEnabled()) {
            // Create system request to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Queue the request to pop up the Bluetooth enable/disable dialog
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Standby for device found, Bluetooth may still be disabled at this point
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        // Set lists for device id and name
        pairedDevices = new ArrayList<String>();
		devices = new ArrayList<BluetoothDevice>();

		// Get paired devices.
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

		// Pull up the list of paired devices
        if (pairedDevices.size() > 0 ) {

            // Found paired devices
            Log.d(_strTag, ":HM:                   Found paired devices");
            for (BluetoothDevice device : pairedDevices){

                devices.add(device);
                listAdapter.add(device.getName() + " " + "\n" + device.getAddress());

                // Document the paired devices found
                Log.d(_strTag, ":HM:                   Paired device: " + device.getName() );

            }

            // Summarize the devices
            Log.d(_strTag, ":HM:                   Number of devices: " + devices.size());
            Log.d(_strTag, ":HM:            Number of paired devices: " + pairedDevices.size());

		};

		registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {

		super.onPause();
        if( receiver != null){
            unregisterReceiver(receiver);
        }
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED)
        {
			Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		if (btAdapter.isDiscovering()){
			btAdapter.cancelDiscovery();
		}

		// Instantiate the connection thread
        selectedDevice = devices.get(arg2);
        ConnectThread connect = new ConnectThread(selectedDevice);
        connect.start();
	}

    /**
     * Thread to connect to socket
     */
    private class ConnectThread extends Thread
	{
		private final BluetoothSocket mmSocket;

		ConnectThread(BluetoothDevice device)
        {

			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try{
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			}
            catch (IOException e){
                Toast.makeText(getApplicationContext(), "Failed to create rfcomm socket", Toast.LENGTH_SHORT).show();
            }
			mmSocket = tmp;
            Log.i("AccelPlot","ConnectThread constructor");
		}

        /**
         * Try to connect the socket
         */
        public void run()
		{
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();

			try
            {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
                mmSocket.connect();
			}
            catch (IOException connectException)
            {
                Log.e("AccelPlot","ConnectThread.run() IO Exception - failed to connect", connectException);
                // Unable to connect; close the socket and get out
				try
                {

                    // This approach is described in a StackOverflow question:
                    // http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                    Log.i("AccelPlot","Trying fallback connection");

                    // Use a temporary object that is later assigned to mmSocket,
                    // because mmSocket is final
                    BluetoothSocket tmp = null;

                    try{
                        tmp =(BluetoothSocket)selectedDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(selectedDevice,1);
                    }catch (Exception e) {
                        Log.e("AccelPlot","Failed to create fallback socket", e);
                    }

                    try{
                        tmp.connect();
                    }
                    catch (IOException closeException){
                        Log.e("AccelPlot","Fallback failed", closeException);
                        tmp.close();
                        return;
                    }
                    mHandler.obtainMessage(SUCCESS_CONNECT, tmp).sendToTarget();
                    Log.i("AccelPlot", "Successfully opened socket and sent message");
                    return;

				}
                catch (IOException fallbackException)
                {
                    Toast.makeText(getApplicationContext(), "Unable to connect and close bt socket", Toast.LENGTH_SHORT).show();
                }
				return;
			}

			// Do work to manage the connection (in a separate thread)
			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
            Log.i("AccelPlot", "Successfully opened socket and sent message");
		}

	}

    /**
     * Connect to the Bluetooth socket
     */
    static class ConnectedThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final DataInputStream dInStream;

		ConnectedThread(BluetoothSocket socket)
		{
			mmSocket = socket;
			InputStream tmpIn = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try
			{
				tmpIn = socket.getInputStream();
			}
			catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


			dInStream = new DataInputStream(tmpIn);
		}

        /**
         * Masks the bytes and return integer value
         * @param btLow     Low byte
         * @param btHigh    High byte
         * @return          Integer value
         */
        private int iGetValue(byte btLow, byte btHigh)
        {

            int iHigh;
            iHigh = (btHigh & 0x1F);
            return ((btLow & 0xFF )+(iHigh<<8));

        }

        /**
         * Extract the address from the high byte
         * @param btHigh    High byte
         * @return          Integer value of the address
         */
        private int iGetAddr(byte btHigh)
        {
            return (btHigh >> 5 ) & 0x07;

        }

        /**
         * This is the main data acquisition loop. In its current form, it can also provide digital
         * filtering, but it is not applied to any of the signals.
         */
        public void run()
		{
			Thread.currentThread().setPriority(MAX_PRIORITY);
            byte[] buffer = new byte[160];  // buffer store for the stream
			int[] iAddr = new int[4];
			int iErrorCount;
			float fCh1Out;
			float fCh2Out;

            // Filter coefficient arrays
            double dA[] =  new double[FilterHelper.MAXKERNELSIZE];
            double dB[] =  new double[FilterHelper.MAXKERNELSIZE];

            // Setup the filter
            DSPfilter.bSetSamplingFrequency(dSampleFreq);
            DSPfilter.bSetLPCorner(20.0);
            DSPfilter.setLowPass(true);
            DSPfilter.setHighPass(false);
            DSPfilter.bSetNumberPoles(2);
            DSPfilter.setPercentRipple(0.0);

            // Retrieve the coefficients
            for( int idx=0; idx< FilterHelper.MAXKERNELSIZE; ++idx){
                dA[idx] = DSPfilter.dGet_A(idx);
                dB[idx] = DSPfilter.dGet_B(idx);
            }

			// Keep listening to the InputStream until an exception occurs
			while (true)
			{

				try	{

					// Read from the InputStream
					dInStream.readFully(buffer,0,16);

					// Parse through the bytes and validate
					if(  bStreamData ){

                        // X_Accel, address 0x0000
						iAddr[0] =  iGetAddr(buffer[1]);

						// The hypothesis is that the data is valid, this will be checked
						// at each step and set to false if any of the tests fail
						iErrorCount = iAddr[0];
                        fX_Accel[idxBuff] = (float)(iGetValue(buffer[0], buffer[1]));

                        // Y_Accel, address 0x0001
                        iAddr[1] =  iGetAddr(buffer[3]);
                        if( iAddr[1] != 0x01){
                            ++iErrorCount;
                        }
                        fY_Accel[idxBuff] = (float)(iGetValue(buffer[2], buffer[3]));

                        // Z_Accel, address 0x0002
                        iAddr[2] =  iGetAddr(buffer[5]);
                        if( iAddr[2] != 0x02){
                            ++iErrorCount;
                        }
                        fZ_Accel[idxBuff] = (float)(iGetValue(buffer[4], buffer[5]));

                        // Gyro or ADC, address 0x0003
                        iAddr[3] =  iGetAddr(buffer[7]);
                        if( iAddr[3] != 0x03){
                            ++iErrorCount;
                        }
                        fX_Gyro[idxBuff] = (float)(iGetValue(buffer[6], buffer[7]));

                        if( iErrorCount == 0)
                        {
                            // Throw the data to the renderer subtracting off the offset so we get
                            // positive and negative traces
                            classChartRenderer.classChart.addSample(fX_Accel[idxBuff]-F_OFFSET_COUNT, 0);
                            classChartRenderer.classChart.addSample(fY_Accel[idxBuff]-F_OFFSET_COUNT, 1);
                            classChartRenderer.classChart.addSample(fZ_Accel[idxBuff]-F_OFFSET_COUNT, 2);
                            classChartRenderer.classChart.addSample(fX_Gyro[idxBuff]-F_OFFSET_COUNT, 3);

							// Throw the data to the audio output
							fCh1Out = ((bADC1ToCh1Out ? 1.0f: 0.0f) *fX_Accel[idxBuff])+
                                ((bADC2ToCh1Out ? 1.0f: 0.0f) *fY_Accel[idxBuff])+
                                ((bADC3ToCh1Out ? 1.0f: 0.0f) *fZ_Accel[idxBuff])+
                                ((bADC4ToCh1Out ? 1.0f: 0.0f) *fX_Gyro[idxBuff]);
                            fCh2Out = ((bADC1ToCh2Out ? 1.0f: 0.0f) *fX_Accel[idxBuff])+
                                ((bADC2ToCh2Out ? 1.0f: 0.0f) *fY_Accel[idxBuff])+
                                ((bADC3ToCh2Out ? 1.0f: 0.0f) *fZ_Accel[idxBuff])+
                                ((bADC4ToCh2Out ? 1.0f: 0.0f) *fX_Gyro[idxBuff]);
                            classAudioHelper.setFreqOfTone(1000.0f+fCh1Out/2.0f,
									1000.0f+fCh2Out/2.0f);


                            // Save the data off to the sd card / local directory
                            if( bWriteLocal ){
                                Log.d(_strTag, ":HM:                          Begin Writing: ");
                                if( idxBuff == (iFileSampleCount-1) && bWritePending){
                                    Log.d(_strTag, ":HM:                   Write files, idxBuff: " + idxBuff);
                                    Log.d(_strTag, ":HM:                             fX_Gyro[0]: " + fX_Gyro[0]);
//                                    Log.i(_strTag, ":HM:                          X_Accel Error: " + iErrorCount);
                                    fhelper.bFileToSD(fX_Accel, fY_Accel, fZ_Accel, fX_Gyro);
                                    bWritePending = false;
                                    mHandler.obtainMessage(FILE_WRITE_DONE, mmSocket).sendToTarget();

                                }
                            }

                            // Increment the data buffer index
                            idxBuff = ++idxBuff % iFileSampleCount;

                        }
                        else
                        {

                            // Skip a byte to see if we can get back in sync
                            dInStream.readByte();

                        }


					}

				}
				catch (IOException e){
					break;
				}
			}
		}

        /**
         * all this from the main activity to shutdown the connection
         */
        void cancel() {

			try
            {
				mmSocket.close();
			}
            catch (IOException e)
            {
                e.printStackTrace();
            }

            mHandler.obtainMessage(SUCCESS_DISCONNECT).sendToTarget();

        }
	}

}
