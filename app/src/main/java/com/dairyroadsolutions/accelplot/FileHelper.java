package com.dairyroadsolutions.accelplot;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Locale;

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
     * @return              true if successful. false for the unexpected
     */
    public boolean bFileToSD(String strDir, String strFileName, float[] data){

        // TODO: Exception handling in the case no SD card exist.

        // Local variable that stores path to SD card
        File sdCard;
        boolean bSuccess = true;
        int iBytes;

        // Pull the directory. I chose to put this in the directory for user-created documents.
        sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Assembly the absolute path to the directory
        File dir = new File (sdCard.getAbsolutePath() + strDir);

        // Does the directory exist?
        if ( !dir.isDirectory()) {

            // Attempt to make the directory
            try {
                Files.createDirectory(dir.toPath());
            } catch (java.io.IOException e) {
                Log.d(_strTag, ":HM:              Directory creation failed: ");
                e.printStackTrace();
                bSuccess = false;
            }
        }
        // Create the file
        File file = new File(dir, strFileName);

        try{

            // Open a channel to begin writing
            FileChannel filechannel = null;
            try{
                filechannel = new FileOutputStream(file).getChannel();
            }catch (NullPointerException e) {
                e.printStackTrace();
                Log.i(_strTag, ":HM:                      getChannel failed: ");
            }

            // Clear and reset the buffer
            bb.clear();

            // Assemble data for the file write
            for(int idx = 0; idx<data.length; ++idx) bb.putFloat(data[idx]);

            Log.i(_strTag, ":HM:                              Directory: " + dir.getPath());
            Log.i(_strTag, ":HM:                              File Name: " + strFileName);
            Log.i(_strTag, ":HM:                                data[0]: " + data[0]);


            // The position value is at the end of the buffer, so we need this command to move the
            // position marker to zero and reset the mark.
            bb.flip();

            // Write the buffer to the file
            assert filechannel != null;
            iBytes = filechannel.write(bb);
            Log.d(_strTag, ":HM:                          Bytes written: " + iBytes);
            if (iBytes<1){
                bSuccess = false;
            }

            // Close the file
            filechannel.close();

            //Log.d(_strTag, ":HM:                    File write complete: ");

        } catch (Exception e) {
            Log.d(_strTag, ":HM:                   FileHelper Exception: " + e.getMessage());
        }

        // Increment the file index
        ++lFileIdx;

        // Everything must have gone ok
        return !bSuccess;

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

        // Local variables
        String strFileName;
        String strDir;
        boolean bSuccess = true;

        // TODO: bFileToSD has proof-of-concept code. It needs to be refactored so that code is
        //  not copy-pasted everywhere.

        // The filename and directory strings for the first trace
        strFileName = "Trace01_" + String.format(Locale.US, "%07d", lFileIdx) + ".dat";
        Log.d(_strTag, ":HM:                 Write Trace01 Filename: " + strFileName);
        strDir = "/AccelPlot";
        Log.d(_strTag, ":HM:                Write Trace01 Directory: " + strDir);
        if (bFileToSD(strDir, strFileName, data01)){
            Log.d(_strTag, ":HM:                        Failed to write: " + strFileName);
            bSuccess = false;
        }

        // The filename and directory strings for the second trace
        strFileName = "Trace02_" + String.format(Locale.US,"%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        if (bFileToSD(strDir, strFileName, data02)){
            Log.d(_strTag, ":HM:                        Failed to write: " + strFileName);
            bSuccess = false;
        }

        // The filename and directory strings for the third trace
        strFileName = "Trace03_" + String.format(Locale.US,"%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        if (bFileToSD(strDir, strFileName, data03)){
            Log.d(_strTag, ":HM:                        Failed to write: " + strFileName);
            bSuccess = false;
        }

        // The filename and directory strings for the fourth trace
        strFileName = "Trace04_" + String.format(Locale.US,"%07d", lFileIdx) + ".dat";
        strDir = "/AccelPlot";
        if (bFileToSD(strDir, strFileName, data04)){
            Log.d(_strTag, ":HM:                        Failed to write: " + strFileName);
            bSuccess = false;
        }

        // Everything must have gone ok
        return bSuccess;

    }


}
