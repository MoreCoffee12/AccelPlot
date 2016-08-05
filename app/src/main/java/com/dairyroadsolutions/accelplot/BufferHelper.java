package com.dairyroadsolutions.accelplot;

/**
 * Created by Brian on 9/20/2015.
 */
public class BufferHelper {

    /**
     * This little bit of code is used for modulus of negative values
     * @param i             Arbitrary index
     * @param iBufferSize   Size of the buffer
     * @return              Index modulo the buffer size
     */
    static public int wrap(int i, int iBufferSize) {

        int result = i % iBufferSize;
        return result < 0 ? result+iBufferSize : result;

    }



}
