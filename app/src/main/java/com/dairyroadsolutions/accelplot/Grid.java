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
	
	float fStartX;
	float fStartY;
	float fWidth;
	float fHeight;
	
	
	private int iDivisionsX;
	private int iDivisionsY;
	
	float[] mCoords; //Regular line coordinates
	float[] mCoordsPrimary;//Primary line coordinates
	float[] mCoordsMiddle;//Axes coordinates
	
	private FloatBuffer mFVertexBuffer;//Regular lines vertex buffer
	private FloatBuffer mFVertexBufferPrimary;//Primary lines vertex buffer
	private FloatBuffer mMiddleVertexBuffer;//Axes vertex buffer
	private FloatBuffer mFrameVertexBuffer;//Frame vertex buffer

	/**
	 *
	 * @param gl  OpenGL handle
	 */
	public void draw(GL10 gl) {

        if( iDivisionsX>0 && iDivisionsY>0){

            // grid color of dark blue
            gl.glColor4f(0.16f, 0.16f, 0.16f, 1f);


            gl.glLineWidth(0.1f);//Thin lines width

            gl.glTranslatef(fStartX, fStartY, 0.01f);//Move below graph level

            gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mFVertexBuffer);//Select regular lines
            gl.glDrawArrays(GL10.GL_LINES, 0, (iDivisionsX + iDivisionsY)*2);//Draw regular lines

            int numberOfPrimaryPointsX = (iDivisionsX +5)/5;
            int numberOfPrimaryPointsY = (iDivisionsY +5)/5+1;

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

            gl.glTranslatef(-fStartX, -fStartY, -0.01f);

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
		fStartX = startX;
		fStartY = startY;
		fWidth = graphWidth;
		fHeight = graphHeight;
				
		startX=startY=0;
				
		iDivisionsX = divisionsX;
		iDivisionsY = divisionsY;
		
		
		//Regular lines
		mCoords = new float[(iDivisionsX *4+ iDivisionsY +2)*4];
		
		for(int i=0; i< iDivisionsX;i++)
		{
			mCoords[i*4] = startX+(graphWidth*i)/ iDivisionsX;
			mCoords[i*4+1] = startY;			
			mCoords[i*4+2] = startX+(graphWidth*i)/ iDivisionsX;
			mCoords[i*4+3] = startY-graphHeight;
		}
		
		
		//X Axis
		mCoordsMiddle = new float[4];
		
		mCoordsMiddle[0] = startX;
		mCoordsMiddle[1] = startY - fHeight /2;
		mCoordsMiddle[2] = startX + fWidth;
		mCoordsMiddle[3] = startY - fHeight /2;
		
		
		//Primary lines
		int numberOfPrimaryPointsX = (iDivisionsX +5)/5;
		int numberOfPrimaryPointsY = (iDivisionsY +5)/5+1;
		mCoordsPrimary = new float[(numberOfPrimaryPointsX+numberOfPrimaryPointsY)*4];
				
		for(int i=0; i<numberOfPrimaryPointsX;i++)
		{
			mCoordsPrimary[i*4] = startX+(graphWidth*i*5)/ iDivisionsX;
			mCoordsPrimary[i*4+1] = startY;			
			mCoordsPrimary[i*4+2] = startX+(graphWidth*i*5)/ iDivisionsX;
			mCoordsPrimary[i*4+3] = startY-graphHeight;
		}

		
				
		float[] frameCoords =  new float[10];
		
		frameCoords[0]=startX;
		frameCoords[1]=startY;
		frameCoords[2]=startX+graphWidth;
		frameCoords[3]=startY;
		frameCoords[4]=startX+graphWidth;;
		frameCoords[5]=startY-graphHeight;
		frameCoords[6]=startX;
		frameCoords[7]=startY-graphHeight;
		frameCoords[8]=startX;
		frameCoords[9]=startY;
				
		for(int i=0; i<(iDivisionsY /2+1);i++)
		{
			mCoords[iDivisionsX *4+i*4] = startX;
			mCoords[iDivisionsX *4+i*4+1] = startY-graphHeight/2+(graphHeight*i)/(iDivisionsY);
			mCoords[iDivisionsX *4+i*4+2] = startX+graphWidth;
			mCoords[iDivisionsX *4+i*4+3] = startY-graphHeight/2+(graphHeight*i)/(iDivisionsY);;
		}
		
		int secondaryOffset = (iDivisionsY /2+1)*4;
		
		for(int i=0; i<(iDivisionsY /2+1);i++)
		{
			mCoords[secondaryOffset + iDivisionsX *4+i*4] = startX;
			mCoords[secondaryOffset + iDivisionsX *4+i*4+1] = startY-graphHeight/2-(graphHeight*(i+1))/ iDivisionsY;
			mCoords[secondaryOffset + iDivisionsX *4+i*4+2] = startX+graphWidth;
			mCoords[secondaryOffset + iDivisionsX *4+i*4+3] = startY-graphHeight/2-(graphHeight*(i+1))/ iDivisionsY;;
		}
		
		int primaryArrayOffset= numberOfPrimaryPointsX*4;
		
		for(int i=0; i<(numberOfPrimaryPointsY/2+1);i++)
		{
			mCoordsPrimary[primaryArrayOffset+i*4] = startX;
			mCoordsPrimary[primaryArrayOffset+i*4+1] = startY-graphHeight/2+(graphHeight*i*5)/ iDivisionsY;
			mCoordsPrimary[primaryArrayOffset+i*4+2] = startX+graphWidth;
			mCoordsPrimary[primaryArrayOffset+i*4+3] = startY-graphHeight/2+(graphHeight*i*5)/ iDivisionsY;
		}
		
		primaryArrayOffset=(numberOfPrimaryPointsX+numberOfPrimaryPointsY/2)*4;
		
		for(int i=0; i<(numberOfPrimaryPointsY/2);i++)
		{
			mCoordsPrimary[primaryArrayOffset+i*4] = startX;
			mCoordsPrimary[primaryArrayOffset+i*4+1] = startY-graphHeight/2-(graphHeight*i*5)/ iDivisionsY;
			mCoordsPrimary[primaryArrayOffset+i*4+2] = startX+graphWidth;
			mCoordsPrimary[primaryArrayOffset+i*4+3] = startY-graphHeight/2-(graphHeight*i*5)/ iDivisionsY;;
		}
		
		
		ByteBuffer vbb = ByteBuffer.allocateDirect((iDivisionsX + iDivisionsY +2)*2 * 2 * 4);
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
		
				
		for (int i = 0; i < (iDivisionsX + iDivisionsY)*2; i++) {
			for(int j = 0; j < 2; j++) {
				mFVertexBuffer.put(mCoords[i*2+j]);
			}
		}
		
		int loopValue = (numberOfPrimaryPointsX+numberOfPrimaryPointsY)*2;
		
		for (int i = 0; i < loopValue; i++) {
			mFVertexBufferPrimary.put(mCoordsPrimary[i*2]);
			mFVertexBufferPrimary.put(mCoordsPrimary[i*2+1]);
			}
	
		
		for(int i=0;i<10;i++)
		{
			mFrameVertexBuffer.put(frameCoords[i]);
		}
		
		for(int i=0;i<4;i++)
		{
			mMiddleVertexBuffer.put(mCoordsMiddle[i]);
		}
		
		mFVertexBuffer.position(0);
		mFrameVertexBuffer.position(0);
		mFVertexBufferPrimary.position(0);
		mMiddleVertexBuffer.position(0);
	}

}

