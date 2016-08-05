package com.dairyroadsolutions.accelplot;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class ChartRenderer extends AbstractRenderer {

	public Chart classChart;
	public Context mContext;
	private int iScreenBufferCount = 0;
	private boolean _mCyclic = true;

	/**
	 * Set the channel scale factor
	 * @param fChScale  Array of the channel scales
	 */
	public void setChScale(float fChScale[]){

		classChart.setChScale(fChScale);

	}

	/**
	 * Update the offset for the traces
	 * @param fChOffset Offset value for the trace
	 */
	public void setChOffset(float fChOffset[]){
		classChart.setChOffset(fChOffset);
	}

    /**
     * Set the number of columns in the chart
     * @param iNewColumnCount
     */
    public void setChartColumnCount( int iNewColumnCount ) { classChart.setChartColumnCount(iNewColumnCount);}

	public void setCyclic(boolean cyclic){
		_mCyclic = cyclic;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		super.onSurfaceChanged(gl, w, h);
		
		recalculateScreen();
	}

	/**
	 * Public constructor for the method
	 * @param context               Resource where the classChart will be rendered
	 * @param iScreenBuffCount      Length of the screen buffer
	 * @param samplesBuffer         Pointer to the buffer containing the samples
	 * @param TraceCount            Number of traces on the screen
	 */
	public ChartRenderer(Context context,  int iScreenBuffCount, SamplesBuffer[] samplesBuffer, int TraceCount) {

		// Save off the screen buffer count
		iScreenBufferCount = iScreenBuffCount;

        // Save off the context to this object
		mContext = context;

        // Call the constructor for the new classChart
		classChart = new Chart(context,iScreenBufferCount, samplesBuffer, TraceCount, _mCyclic);

	}

	private void recalculateScreen() {
		classChart.recalculate(mRatio);
	}

	@Override
	protected void draw(GL10 gl) {
		classChart.draw(gl);
	}

	void updateData()
	{
		classChart.update();
	}

    /**
     * Method to set the line thickness
     * @param fNewThick     New line thickness
     */
    public void setThickness( float fNewThick){
        classChart.setThickness(fNewThick);
    }

    /**
     * Method to retrieve the line thickness
     * @return      Returns the current classChart line thickness
     */
    public float fLineThick(){
        return classChart.fLineThick();
    }

	/**
	 * Update the horizontal division count in the grid object
	 * @param iDivisionsX_In    Number of horizontal divisions on the grid
	 * @return                  True if the value was set
	 */
	public boolean bSetDivisionsX(int iDivisionsX_In){
		classChart.bSetDivisionsX(iDivisionsX_In);
		return true;
	}

	/**
	 * Update the vertical division count in the grid object
	 * @param iDivisionsY_In    Number of vertical divisions on the grid
	 * @return                  True if the value was set
	 */
	public boolean bSetDivisionsY(int iDivisionsY_In){
        classChart.bSetDivisionsY(iDivisionsY_In);
		return true;
	}



}
