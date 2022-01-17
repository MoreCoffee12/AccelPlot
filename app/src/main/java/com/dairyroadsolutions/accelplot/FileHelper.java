package com.dairyroadsolutions.accelplot;

/**
 * Created by Brian on 1/24/2016.
 *
 * This helper provides methods to write data to the sd card on the device.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

class FileHelper {

    private ByteBuffer bb;

    FileHelper(int iFloatBuffLength){
        vSetSamples( iFloatBuffLength );
    }

    // debug
    private static final String _strTag = MainActivity.class.getSimpleName();

    // File index
    private long lFileIdx = 0;

    public void vSetSamples( int iFloatBuffLength ){
        bb = ByteBuffer.allocate(iFloatBuffLength*4);
    }



    /**
     * This function writes a single array to the external storage directory using default
     * directory and file name values;
     * @param strDir        String with directory name. Must include leading "/"
     * @param strFileName   String with the filename
     * @param data          float array with the data
     * @return              true if successful
     */
    public boolean bFileToSD(String strDir, String strFileName, float[] data){

        File sdCard;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }else{
            sdCard = Environment.getExternalStorageDirectory();
        }
        File dir = new File (sdCard.getAbsolutePath() + strDir);

        // Attempt to make the directory
        try{

            // mkdir returns 0 if the creation succeeded, otherwise is -1
            if(dir.mkdirs()) {
                Log.d(_strTag, ":HM:             Directory creation success: ");
            }else{
                Log.d(_strTag, ":HM:              Directory creation failed: ");
            };

        }catch (Exception e){
            Log.d(_strTag, ":HM:      *Error* Directory creation failed: ");
            e.printStackTrace();
        }
        File file = new File(dir, strFileName);

        try{
            FileChannel fchannel = new FileOutputStream(file).getChannel();

            bb.clear();

            for( int idx=0; idx<data.length; ++idx){
                bb.putFloat(data[idx]);
            }

            Log.i(_strTag, ":HM:                              Directory: " + dir.getPath());
            Log.i(_strTag, ":HM:                              File Name: " + strFileName);
            Log.i(_strTag, ":HM:                                data[0]: " + data[0]);


            // The position value is at the end of the buffer, so we need this command to move the
            // position marker to zero and reset the mark.
            bb.flip();

            // Write the buffer to the file
            fchannel.write(bb);

            // Close the file
            fchannel.close();

            //Log.d(_strTag, ":HM:                    File write complete: ");

        } catch (Exception e) {
            Log.d(_strTag, ":HM:                   FileHelper Exception: " + e.getMessage());
        }

        // Increment the file index
        ++lFileIdx;

        // Everything must have gone ok
        return true;

    }

    /**
     * This function writes four arrays to the external storage directory using default
     * directory and file name values;
     * @param data01    float array with the data
     * @param data02    float array with the data
     * @param data03    float array with the data
     * @param data04    float array with the data
     * @return          true if successful
     */
    boolean bFileToSD(float[] data01, float[] data02, float[] data03, float[] data04){

        String strFileName;
        String strDir;

        // The filename and directory strings
        strFileName = "Trace01_" + String.format("%07d", lFileIdx) + ".dat";
        Log.d(_strTag, ":HM:                 Write Trace01 Filename: " + strFileName);
        strDir = "/AccelPlot";
        Log.d(_strTag, ":HM:                Write Trace01 Directory: " + strDir);
        bFileToSD(strDir, strFileName, data01);

        // The filename and directory strings
        strFileName = "Trace02_" + String.format("%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        bFileToSD(strDir, strFileName, data02);

        // The filename and directory strings
        strFileName = "Trace03_" + String.format("%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        bFileToSD(strDir, strFileName, data03);

        // The filename and directory strings
        strFileName = "Trace04_" + String.format("%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        bFileToSD(strDir, strFileName, data04);

        // Everything must have gone ok
        return true;

    }


}
