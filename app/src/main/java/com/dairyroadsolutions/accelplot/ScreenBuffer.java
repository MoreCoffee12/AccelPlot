package com.dairyroadsolutions.accelplot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A screen buffer for drawing an Open GL graph. Inherited by the line & point screen buffer classes.
 * The screen buffer is a cyclic buffer, so adding a sample moves the write pointer while not invalidating the
 * rest of the samples. If adding a sample will cause the buffer to overflow the sample at the read position 
 * is thrown away to free space.
 */
abstract public class ScreenBuffer {

	private static final String strTag = "ScreenBuffer";
	int iScreenBufferCount = 0;
	int mBufferWritePointer;
	int mBufferReadPointer;
	float fStartX;
	float fStartY;
	float fHeight;
	float fWidth;
	float mRComponent = 0;
	float mGComponent = 1;
	float mBComponent = 0;
	protected FloatBuffer fVertexBuffer;
	protected FloatBuffer fVertexBufferMarker;

	boolean mBufferFull = false;
	
	abstract void fillVertexArrayX(float YStart);
	abstract int getAllocation(int samplesInScreen);

    abstract void putSample(float sample);
    abstract void putMarker(int iDataBufferLocation);
    abstract void drawScreenBuffer(GL10 gl);
	abstract void setThickness(float fNewThick);


    /**
	 * Constructor for the screen buffer. Called as super from LineScreenBuffer
	 * @param iScreenBuffCount_In	Number of samples to be displayed on the screen
	 * @param startX 			    Starting location in the horizontal direction
	 * @param startY			    Starting location in the vertical direction
	 * @param width				    Width of renderer
	 * @param height 			    Heigth of renderer
     * @param YStart                Vertical starting point for the trace
	 */
	ScreenBuffer(int iScreenBuffCount_In, float startX, float startY, float width, float height,
				 float YStart)
	{
		// Save off the number of samples in the buffer
		iScreenBufferCount  = iScreenBuffCount_In;

		fStartX = startX;
		fStartY = startY;
		fHeight = height;
		fWidth  = width;
		
		// These lines setup the main trace buffer
		ByteBuffer vbb = ByteBuffer.allocateDirect(getAllocation(iScreenBufferCount));
		vbb.order(ByteOrder.nativeOrder());
		fVertexBuffer = vbb.asFloatBuffer();

		fillVertexArrayX(YStart);
	}
	
		
	private void advanceReadPointer()
	{
		// Advance
		++mBufferReadPointer;

		// Wrap if needed.
		mBufferReadPointer = BufferHelper.wrap(mBufferReadPointer,getBufferSize());
	}

    private void advanceWritePointer()
    {
        // Increment the pointer
        ++mBufferWritePointer;

        // Wrap if needed.
        mBufferWritePointer = BufferHelper.wrap(mBufferWritePointer,getBufferSize());
    }

	private int getBufferSize() {
		return (iScreenBufferCount);
	}

    /**
     * Add a sample to the vertex buffer.
     * @param sample 	float to be added
     * @return 			true if it works
     */
    synchronized boolean addSample(float sample)
	{		

		putSample(sample);
        advanceWritePointer();

		return true;
	}

    /**
     * Add a marker to the plot
     * @param iDataBufferLocation   Location in the data buffer
     * @return                      True if it worked
     */
    synchronized boolean addMarker(int iDataBufferLocation){
        putMarker(iDataBufferLocation);
        return true;
    }
	
	public void reset() {
		mBufferReadPointer = 0;
		mBufferWritePointer = 0;
		mBufferFull = false;
		
	}

	void setRGB(float r, float g, float b)
	{
		mRComponent = r;
		mGComponent = g;
		mBComponent = b;
	}

	public void setColor(GL10 gl) {
		gl.glColor4f(mRComponent, mGComponent, mBComponent, 1);
	}
	
}
