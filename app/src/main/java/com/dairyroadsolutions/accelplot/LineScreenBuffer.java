package com.dairyroadsolutions.accelplot;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class LineScreenBuffer extends ScreenBuffer {

	private static final String strTag = "LineScreenBuffer";
	private float _fLineThick;

	@Override
	int getAllocation(int samplesInScreen)
	{
		return iScreenBufferCount*2 * 2 * 4 * 2;
	}

    /**
     * Constructor for the LineScreenBuffer object
     * @param iScreenBuffCount  Number of samples in the screen buffer
     * @param fStartX           Horizontal starting point for the renderer
     * @param fStartY           Vertical starting point for the rendered line
     * @param fWidth            Screen width
     * @param fHeight           Screen height
     */
    LineScreenBuffer(int iScreenBuffCount, float fStartX, float fStartY,
			float fWidth, float fHeight, float YStart) {
				super(iScreenBuffCount, fStartX, fStartY, fWidth, fHeight, YStart);

                this.setThickness(3.0f);
			}
	
	@Override
    /**
     * Fill the vertex array with initial data. All x,y positions set to 0.
     * This code uses the GL_LINES vertex buffer so each pair of vertexes
     * is treated as an independent line segment. For example, if you want
     * to plot the array [1,2,3] it is implemented in the vertex buffer as:
     * [0,1] [1,2] [1,2] [2,3]
     * If the data array has n elements then there are (n-1)*4 elements in
     * the vertex buffer array.
     */
    void fillVertexArrayX(float YStart)
	{
        // Locals used to generate the default vertex buffers
		float fValueX_Last = 0.0f;
        float fValueX;
        float fValueY_Last = 0.0f;
        float fValueY;
        int iIdx;

		// Fill all the rest, each point twice
		for(int i=0;i<iScreenBufferCount;++i)
		{
            // This sets the horizontal coordinates for the data. These values do not change.
            fValueX = (fWidth*(float)(i+1))/(float)(iScreenBufferCount-1);

            // Uncomment this line to calculate the new points in the curve as a function of the
			// horizontal distance. This is useful for checking vertex calculations since it
			// doesn't require a connection to BlueTooth.
//            fValueY = (fValueX * 0.1f)+0.5f;
            fValueY = YStart;

            // Index into the data buffer
            iIdx = i<<2;

            // Starting (x,y) pair
			fVertexBuffer.put(iIdx,fValueX_Last);
			fVertexBuffer.put(iIdx+1,fValueY_Last);

            // Ending (x,y) pair
            fVertexBuffer.put(iIdx+2,fValueX);
            fVertexBuffer.put(iIdx+3,fValueY);

            // Save off the new point to the old point
            fValueX_Last = fValueX;
            fValueY_Last = fValueY;
		}

        // Debug
        //dumpBuffer();
	}

	@Override
	void drawScreenBuffer(GL10 gl) {

		float xOffset = 2*(fStartX - fVertexBuffer.get(0));
        //Log.d(strTag, ":HM:                             fStartX: " + fStartX);
        //Log.d(strTag, ":HM:               fVertexBuffer.get(0): " + fVertexBuffer.get(0));
        //Log.d(strTag, ":HM:                             xOffset: " + xOffset);

        // Set the trace line thickness
		gl.glLineWidth(this.fLineThick());

        // This is annoying, but to keep the buffer add method simple,
        // there is no way to write to the first element. This makes the
        // first vertex the same as the second vertex so that the starting
        // point is not always zero.
		fVertexBuffer.put(1, fVertexBuffer.get(3));

        // Place the starting point
		gl.glTranslatef(xOffset, fStartY - fHeight / 2.f, 0);

        // Sets the position of this buffer. If the mark is set and it is greater than the new
        // position, then it is cleared.
        fVertexBuffer.position(0);

        // Marks the current position, so that the position may return to this point later
        // by calling reset().
		fVertexBuffer.mark();

        // Draw the trace
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, fVertexBuffer);
		gl.glDrawArrays(GL10.GL_LINES, 0, (iScreenBufferCount - 1) * 2);

        // Reset the starting point
		gl.glTranslatef(-xOffset, -fStartY + fHeight / 2.0f, 0);
		
		fVertexBuffer.reset();

        // Log results
        //Log.d(strTag, ":HM:                  Completed bufferDraw");

	}

    /**
     * Method to set the line thickness
     * @param fNewThick 	New line thickness
     */
    public void setThickness( float fNewThick){
        _fLineThick = fNewThick;
    }

    /**
     * Method to retrieve the line thickness
     * @return 				Returns line thickness
     */
    public float fLineThick(){
        return _fLineThick;
    }

	/**
	 * Use this to explore the contents of the vertex buffer
	 */
	void dumpBuffer()
	{
		int current = 0;//mBufferReadPointer;

        Log.d(strTag, ":HM:                 Vertex Buffer Start: ");

		for (int i = 0; i < iScreenBufferCount * 4; i++) {
            Log.i("BD",""+i+":"+ fVertexBuffer.get(current));

            Log.d(strTag, ":HM:       i,fVertexBuffer.get(current): " + i + "," + fVertexBuffer.get(current));

			current++;

		}
	}
	
	@Override
	void putSample(float sample)
	{
        fVertexBuffer.put((mBufferWritePointer<<2)+3,sample);
        fVertexBuffer.put((mBufferWritePointer<<2)+5,sample);
        //Log.d(strTag, ":HM:                              sample: " + sample);

	}

    /**
     * Draw a few line segments off screen to make it appear as though
     * there is a blank on the screen
     * @param iDataBufferLocation   Location of the marker in the data buffer
     */
    void putMarker(int iDataBufferLocation){

        int iTemp;
		int iMarkerCenter = (iDataBufferLocation<<2);

        for( int idx=1; idx<=145; idx=idx+2){

            iTemp = BufferHelper.wrap(iMarkerCenter+idx, fVertexBuffer.capacity());
            fVertexBuffer.put(iTemp,10.0f);

        }

    }
}
