package com.dairyroadsolutions.accelplot;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class Chart {

    private static final String strTag = "Chart";
	private int iTraceCount;
    private int iScreenBufferCount = 0;
	private int iDivisionsX;
	private int iDivisionsY;
    private int iChartColumnCount = 1;
	SamplesBuffer samplesBuffer[];
	Grid mGrid[];
	Context mContext;
	ScreenBuffer screenBuffer[];
	GraphColor graphColor[];
	private boolean boolCyclic = false;
	float fChartWidth;
	float fChartHeight;
	float fScreenHeight;
    private float _fLineThick;
    private float _fChScale[];
    private float _fOffset[];

    public TraceHelper classTraceHelper;



    class GraphColor
	{
		int R;
		int G;
		int B;
		
		GraphColor(int inR,int inG,int inB)
		{
			R = inR;
			G = inG;
			B = inB;
		}
	}

    /**
     * Set the scale factors
     * @param fChScale The array with scale factors
     */
    public void setChScale(float fChScale[]){

        System.arraycopy( fChScale, 0, _fChScale, 0, fChScale.length );

    }

    /**
     * Set the vertical offset of the channel (used to stack the traces)
     * @param fChOffset     Channel offset amount (vertical direction)
     */
    public void setChOffset( float fChOffset[]){

        System.arraycopy( fChOffset, 0, _fOffset, 0, fChOffset.length );

	}

    /**
     * Method to set the line thickness
     * @param fNewThick         New line thickness
     */
    public void setThickness( float fNewThick){
        _fLineThick = fNewThick;
    }

    /**
     * Method to retrieve the line thickness
     * @return      Return the trace line thickness
     */
    public float fLineThick(){
        return _fLineThick;
    }

    /**
     * Sets the number of columns for the data
     * @param iNewColumnCount
     */
    public void setChartColumnCount( int iNewColumnCount ){
        iChartColumnCount = iNewColumnCount;
        if(iChartColumnCount <1 ){iChartColumnCount=1;}
    }

    /**
     * This method creates the color profile for each trace
     */
	void setColors()
	{

        for( int idx = 0; idx< iTraceCount; ++idx){
            graphColor[idx]=new GraphColor( 255-(200*( (int)((float)(idx+1)/1.0f) % 2)),
                    255-(255*( (int)((float)(idx+1)/2.0f) % 2)),
                    255*( (int)((float)(idx+1)/3.0f) % 2));
        }

	}


    /**
     * Constructor for the Chart object
     * @param context               Resource where the classChart will be rendered
     * @param iScreenBuffCount      Length of the screen buffer
     * @param samplesBuffer         Pointer to the buffer containing the samples
     * @param iTraceCountIn         Number of traces on the screen
     * @param bCyclic               Does the data roll over or scroll?
     */
    public Chart(Context context, int iScreenBuffCount, SamplesBuffer[] samplesBuffer,
                 int iTraceCountIn, boolean bCyclic) {

        // Save off the screen buffer count
        iScreenBufferCount = iScreenBuffCount;

		// Save off the trace count
		iTraceCount = iTraceCountIn;

        // Save off the display flag
        boolCyclic = bCyclic;

        // Setup the trace helper
        classTraceHelper = new TraceHelper(iTraceCountIn);

        // Set grid divisions to default values
        iDivisionsX = 20;
        iDivisionsY = 20;

		// Initialize the colors
		graphColor = new GraphColor[iTraceCount];

        // Set the colors array
		setColors();

        // Default is one columnn
        iChartColumnCount = 1;
		
		// Save the sample buffers
		this.samplesBuffer = samplesBuffer;

		mContext = context;

		// Create the grid
		mGrid = new Grid[iTraceCount];
		for( int idx=0; idx< iTraceCount; ++idx){
			mGrid[idx] = new Grid();
		}

        // Setup the arrays for scaling and offset
        _fChScale = new float[iTraceCount];
        _fOffset = new float[iTraceCount];
		for(int i=0;i< iTraceCount;++i)
		{
            _fChScale[i] = 1.0f;
            _fOffset[i] = 0.0f;

		}

        // Default line thickness
        this.setThickness(1.0f);

	}

	/**
	 * Update the entire screen buffer. This is not used in the current
     * implementation, but can come in handy if the entire buffer is updated.
	 */
	public void update() {

		for(int i=0;i< iTraceCount;i++)
		{

			// Scale the array and dump it to the vertex calculation method
			for( int idx=0; idx<samplesBuffer[i].getBufferSize(); ++idx){

                addSample(samplesBuffer[i].getSample(idx), i);

			}

			// Create a mark
			screenBuffer[i].addMarker(samplesBuffer[i].getCurrentIndex());

		}
	}

    /**
     * This method places a data point on the next vertex in the trace
     * defined by iTraceNumber
     * @param fData             Data value
     * @param iTraceNumber      Trace that the data will be added to
     */
    public void addSample( float fData, int iTraceNumber ){

        float fScaledSample;

        fScaledSample = (fData*_fChScale[iTraceNumber]);
        fScaledSample = (fScaledSample + _fOffset[iTraceNumber]);

        //Add the sample to screen buffer
        screenBuffer[iTraceNumber].addSample(fScaledSample);

    }

    /**
     * This is the main rendering call
     * @param gl    gl context
     */
    public void draw(GL10 gl) {

        for(int idx=0; idx< iTraceCount; ++idx){
            mGrid[idx].draw(gl);
        }

		gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);

		for(int i=0;i< screenBuffer.length;i++)
		{
			screenBuffer[i].setColor(gl);
			screenBuffer[i].drawScreenBuffer(gl);
		}

		gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
	}


    /**
     * This forces a complete re-calculation of the plot area. This also places the traces in their
     * rows and columns.
     * @param ratio     Screen ratio
     */
    public void recalculate(float ratio) {


        fChartWidth = 2.0f*ratio/(float)iChartColumnCount;
		fChartHeight = ( 2.0f / (float) iTraceCount)*(float)iChartColumnCount;
        fScreenHeight = ( 2.0f);

        float fIncrement = classTraceHelper.getTraceIncrement();
        float fStart = 1.0f;
        float fTraceStart = classTraceHelper.getTraceStart();

        // Calculate the starting points for the classChart
        float fStartX = -((fChartWidth /4.0f)*(float)iChartColumnCount);
        float fStartY = (fScreenHeight /2.0f);

		for(int idx=0;idx< iTraceCount; ++idx){

            mGrid[idx].setBounds(-ratio, fStart , fChartWidth, fChartHeight,
                    iDivisionsX, (int)((float)iDivisionsY/(float) iTraceCount));
            fStart = fStart + fIncrement;

        }

		screenBuffer = new ScreenBuffer[iTraceCount];

		// zero-based indexing for the columns
        int iColCurrent = 0;

        // Configure the traces
		for(int i=0;i< iTraceCount;i++)
		{
			screenBuffer[i] =  new LineScreenBuffer(iScreenBufferCount,
                    fStartX+((fChartWidth/1.9f)*(float)iColCurrent),
                    fStartY+((fChartHeight/2.0f)*(float)iColCurrent),
                    fChartWidth, fScreenHeight, fTraceStart+((float)i * fIncrement));

			screenBuffer[i].setRGB(graphColor[i].R, graphColor[i].G, graphColor[i].B);

			// Set the line thickness
			screenBuffer[i].setThickness(this.fLineThick());

            // Move onto the next column
            ++iColCurrent;
            if(iColCurrent >= iChartColumnCount ) { iColCurrent=0;}

            //Log.d(strTag, ":HM: iColCurrent,iChartColumnCount: " + iColCurrent + "," + iChartColumnCount);

		}

	}

    /**
     * Update the horizontal division count in the grid object
     * @param iDivisionsX_In        Number of horizontal divisions
     * @return                      True if the value was set
     */
    public boolean bSetDivisionsX(int iDivisionsX_In){
        iDivisionsX = iDivisionsX_In;
        return true;
    }

    /**
     * Update the vertical division count in the grid object
     * @param iDivisionsY_In        Number of vertical divisions in the grid
     * @return                      True of the value was set
     */
    public boolean bSetDivisionsY(int iDivisionsY_In){
        iDivisionsY = iDivisionsY_In;
        return true;
    }


}


