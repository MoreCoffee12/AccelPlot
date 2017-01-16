package com.dairyroadsolutions.accelplot;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Draws a grid for a graph
 *
 */
public class Grid {

    // Location of the grid on the canvas
	private float _fStartX;
	private float _fStartY;

	// Grid divisions
	private int _iDivisionsX;
	private int _iDivisionsY;

	private FloatBuffer mFVertexBuffer;//Regular lines vertex buffer
	private FloatBuffer mFVertexBufferPrimary;//Primary lines vertex buffer
	private FloatBuffer mMiddleVertexBuffer;//Axes vertex buffer
	private FloatBuffer mFrameVertexBuffer;//Frame vertex buffer

	/**
	 * Draw the grid in the current context
	 * @param gl  OpenGL handle
	 */
	public void draw(GL10 gl) {

        if( _iDivisionsX >0 && _iDivisionsY >0){

            // Grid color of dark blue
            gl.glColor4f(0.16f, 0.16f, 0.16f, 1f);


            // Thin lines width
            gl.glLineWidth(0.1f);

            // Move below graph level
            gl.glTranslatef(_fStartX, _fStartY, 0.01f);

            gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mFVertexBuffer);//Select regular lines
            gl.glDrawArrays(GL10.GL_LINES, 0, (_iDivisionsX + _iDivisionsY)*2);//Draw regular lines

            int numberOfPrimaryPointsX = (_iDivisionsX +5)/5;
            int numberOfPrimaryPointsY = (_iDivisionsY +5)/5+1;

            gl.glLineWidth(2f);

            gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);//Draw Primary
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mFVertexBufferPrimary);
            gl.glDrawArrays(GL10.GL_LINES, 0, (numberOfPrimaryPointsX+numberOfPrimaryPointsY)*2);

            gl.glLineWidth(4.0f);//Draw axes
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mMiddleVertexBuffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);

            // Light gray for the frame
            gl.glColor4f(0.3f, 0.3f, 0.3f, 1f);

            gl.glLineWidth(3.0f);//Draw the frame
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mFrameVertexBuffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 5);
            gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);

            gl.glTranslatef(-_fStartX, -_fStartY, -0.01f);

        }

	}

	/**
	 * Regenerates the grid when graph display changes
	 * 
	 * @param startX  		Starting location of grid
	 * @param startY 		Ending location of grid
	 * @param graphWidth 	Grid width
	 * @param graphHeight 	Grid height
	 * @param divisionsX 	Number of horizontal divisions
	 * @param divisionsY 	Number of vertical divisions
	 */
	public void setBounds(float startX, float startY, float graphWidth,
			float graphHeight, int divisionsX, int divisionsY) {


        // Line coordinates
        float[] _fCoords;
        float[] _fCoordsPrimary;
        float[] _fCoordsMiddle;

        // Frame coordinates
        float[] frameCoords =  new float[10];


        // Save off the starting coordinates
        _fStartX = startX;
		_fStartY = startY;

		// Save the number of divisions
        _iDivisionsX = divisionsX;
		_iDivisionsY = divisionsY;

		// Regular lines
		_fCoords = new float[(_iDivisionsX *4+ _iDivisionsY +2)*4];
		
		for(int i = 0; i< _iDivisionsX; i++)
		{
			_fCoords[i*4] = (graphWidth*i)/ _iDivisionsX;
			_fCoords[i*4+1] = 0.0f;
			_fCoords[i*4+2] = (graphWidth*i)/ _iDivisionsX;
			_fCoords[i*4+3] = -graphHeight;
		}
		
		
		//X Axis
		_fCoordsMiddle = new float[4];
		
		_fCoordsMiddle[0] = 0.0f;
		_fCoordsMiddle[1] = graphHeight / -2;
		_fCoordsMiddle[2] = graphWidth;
		_fCoordsMiddle[3] = graphHeight / -2;
		
		
		// Primary lines
		int numberOfPrimaryPointsX = (_iDivisionsX +5)/5;
		int numberOfPrimaryPointsY = (_iDivisionsY +5)/5+1;
		_fCoordsPrimary = new float[(numberOfPrimaryPointsX+numberOfPrimaryPointsY)*4];
				
		for(int i=0; i<numberOfPrimaryPointsX;i++)
		{
			_fCoordsPrimary[i*4] = (graphWidth*i*5)/ _iDivisionsX;
			_fCoordsPrimary[i*4+1] = 0.0f;
			_fCoordsPrimary[i*4+2] = (graphWidth*i*5)/ _iDivisionsX;
			_fCoordsPrimary[i*4+3] = -graphHeight;
		}

		frameCoords[0]=0.0f;
		frameCoords[1]=0.0f;
		frameCoords[2]=graphWidth;
		frameCoords[3]=0.0f;
		frameCoords[4]=graphWidth;;
		frameCoords[5]=-graphHeight;
		frameCoords[6]=0.0f;
		frameCoords[7]=-graphHeight;
		frameCoords[8]=0.0f;
		frameCoords[9]=0.0f;
				
		for(int i = 0; i<(_iDivisionsY /2+1); i++)
		{
			_fCoords[_iDivisionsX *4+i*4] = 0.0f;
			_fCoords[_iDivisionsX *4+i*4+1] = -graphHeight/2+(graphHeight*i)/(_iDivisionsY);
			_fCoords[_iDivisionsX *4+i*4+2] = graphWidth;
			_fCoords[_iDivisionsX *4+i*4+3] = -graphHeight/2+(graphHeight*i)/(_iDivisionsY);;
		}
		
		int secondaryOffset = (_iDivisionsY /2+1)*4;

		for(int i = 0; i<(_iDivisionsY /2+1); i++)
		{
			_fCoords[secondaryOffset + _iDivisionsX *4+i*4] = 0.0f;
			_fCoords[secondaryOffset + _iDivisionsX *4+i*4+1] = -graphHeight/2-(graphHeight*(i+1))/ _iDivisionsY;
			_fCoords[secondaryOffset + _iDivisionsX *4+i*4+2] = graphWidth;
			_fCoords[secondaryOffset + _iDivisionsX *4+i*4+3] = -graphHeight/2-(graphHeight*(i+1))/ _iDivisionsY;;
		}
		
		int primaryArrayOffset= numberOfPrimaryPointsX*4;
		
		for(int i=0; i<(numberOfPrimaryPointsY/2+1);i++)
		{
			_fCoordsPrimary[primaryArrayOffset+i*4] = 0.0f;
			_fCoordsPrimary[primaryArrayOffset+i*4+1] = -graphHeight/2+(graphHeight*i*5)/ _iDivisionsY;
			_fCoordsPrimary[primaryArrayOffset+i*4+2] = graphWidth;
			_fCoordsPrimary[primaryArrayOffset+i*4+3] = -graphHeight/2+(graphHeight*i*5)/ _iDivisionsY;
		}
		
		primaryArrayOffset=(numberOfPrimaryPointsX+numberOfPrimaryPointsY/2)*4;
		
		for(int i=0; i<(numberOfPrimaryPointsY/2);i++)
		{
			_fCoordsPrimary[primaryArrayOffset+i*4] = 0.0f;
			_fCoordsPrimary[primaryArrayOffset+i*4+1] = -graphHeight/2-(graphHeight*i*5)/ _iDivisionsY;
			_fCoordsPrimary[primaryArrayOffset+i*4+2] = graphWidth;
			_fCoordsPrimary[primaryArrayOffset+i*4+3] = -graphHeight/2-(graphHeight*i*5)/ _iDivisionsY;;
		}
		
		
		ByteBuffer vbb = ByteBuffer.allocateDirect((_iDivisionsX + _iDivisionsY +2)*2 * 2 * 4);
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();
		
		ByteBuffer vbbPrimary = ByteBuffer.allocateDirect((numberOfPrimaryPointsX+numberOfPrimaryPointsY)*2 * 2 * 4);
		vbbPrimary.order(ByteOrder.nativeOrder());
		mFVertexBufferPrimary = vbbPrimary.asFloatBuffer();
		
		
		ByteBuffer vbbFrame = ByteBuffer.allocateDirect(10 * 2 * 4);
		vbbFrame.order(ByteOrder.nativeOrder());
		mFrameVertexBuffer = vbbFrame.asFloatBuffer();
		
		ByteBuffer vbbMiddle = ByteBuffer.allocateDirect(4 * 2 * 4);
		vbbMiddle.order(ByteOrder.nativeOrder());
		mMiddleVertexBuffer = vbbMiddle.asFloatBuffer();
		
				
		for (int i = 0; i < (_iDivisionsX + _iDivisionsY)*2; i++) {
			for(int j = 0; j < 2; j++) {
				mFVertexBuffer.put(_fCoords[i*2+j]);
			}
		}
		
		int loopValue = (numberOfPrimaryPointsX+numberOfPrimaryPointsY)*2;
		
		for (int i = 0; i < loopValue; i++) {
			mFVertexBufferPrimary.put(_fCoordsPrimary[i*2]);
			mFVertexBufferPrimary.put(_fCoordsPrimary[i*2+1]);
			}
	
		
		for(int i=0;i<10;i++)
		{
			mFrameVertexBuffer.put(frameCoords[i]);
		}
		
		for(int i=0;i<4;i++)
		{
			mMiddleVertexBuffer.put(_fCoordsMiddle[i]);
		}
		
		mFVertexBuffer.position(0);
		mFrameVertexBuffer.position(0);
		mFVertexBufferPrimary.position(0);
		mMiddleVertexBuffer.position(0);
	}

}

