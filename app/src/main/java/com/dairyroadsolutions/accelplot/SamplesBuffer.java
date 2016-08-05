package com.dairyroadsolutions.accelplot;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import android.util.Log;

/**
 * Data structure for storing samples data. 
 * Samples buffer is circular, the start time of the buffer is always set to the read pointer time
 *
 */
public class SamplesBuffer {

	private static final String strTAG = "SamplesBuffer";

	float [] fSamples;

	private int iBufferIndex=0;
	private int iBufferSize;

	boolean bCyclic = true;

	Semaphore mSemaphore;

    /**
     * This is the embedded test code. Eventually this needs to be moved to a
     * formal test plan within Android Studio.
     */
    public void TestHarness()
	{
		SamplesBuffer sb = new SamplesBuffer(10,true);
		
        // Test #1 - Does the code handle wrapping correctly?
		for(int i=0;i<sb.getBufferSize();i++)
		{
			sb.addSample((float)i);
		}
		
		sb.addSample(10.0f);
		sb.addSample(11.0f);

        float fTemp = sb.getSample(0);
		
        if( Float.compare(10.0f, sb.getSample(0)) != 0){
            Log.d(strTAG+"Test","Wrapping failed");
        }
        if( Float.compare(11.0f, sb.getSample(1)) != 0){
            Log.d(strTAG+"Test","Wrapping failed");
        }

        // Success
        Log.d(strTAG + "Test", "All tests completed successfully!!!");

	}

	/**
	 * The public constructor for the class
	 * @param bufferSize 	The number of elements in the buffer
	 * @param cyclic		Display is cyclic or floating
	 */
	public SamplesBuffer(int bufferSize, boolean cyclic) {

		Log.d(strTAG,"Constructor call");
		fSamples = new float[bufferSize];
		
		iBufferSize = bufferSize;
        iBufferIndex = 0;
		bCyclic = cyclic;
		
		mSemaphore = new Semaphore(1, true);
		
		resetBuffer();
	}

	float getSample(int iIndex)
	{
        // Wrap if needed.
        int iIndexWrapped = BufferHelper.wrap(iIndex, iBufferSize);

        // Get a lock on the thread
        try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
            Log.d(strTAG,"Semaphore acquire() for getSample failed.");
			e.printStackTrace();
		}
		
		// Variable to store the retun value
		float returnValue;

        returnValue =  fSamples[iIndexWrapped];

		mSemaphore.release();
		
		return returnValue;
	}

    /**
     * This method overwrites the current sample.
     * @param sampleValue       The new float value
     */
	public void overwriteSample(float sampleValue){

		// Get a lock on the thread
		try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
			Log.d(strTAG,"Semaphore acquire() for addSample failed.");
			e.printStackTrace();
		}

		// Add the sample
		fSamples[iBufferIndex] = sampleValue;
//		Log.d(strTAG, ":HM:                           iBufferIndex, addSample ECG: "
//				+ iBufferIndex + " : "+fSamples[iBufferIndex]);
		// Release hold on the thread
		mSemaphore.release();

	}

    /**
     * This little method adds a new sample into the array.
     * @param sampleValue       The new float value
     */
    public void addSample(float sampleValue){

		// over write the existing sample
		overwriteSample(sampleValue);

		// Increment the index
		iBufferIndex = BufferHelper.wrap(iBufferIndex + 1,iBufferSize);

	}


    /**
     * Short method to reset the buffer
     */
    private void resetBuffer() {
        Arrays.fill(fSamples, Float.MAX_VALUE);
	}

    /**
     * get the current index. This is the spot in the array that
     * will be written to on the next cycle
     * @return      Integer value of the current index
     */
    public int getCurrentIndex(){
        return iBufferIndex;
    }

    /**
     * get Buffer size
     * @return     The size of the buffer
     */
    public int getBufferSize() {
		return iBufferSize;
	}
	
}
