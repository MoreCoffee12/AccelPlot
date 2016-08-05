package com.dairyroadsolutions.accelplot;

/**
 * Created by Brian on 6/26/2015.
 *
 * This class provides helper calculations for the trace layout on the screen
 */
public class TraceHelper{

    private int localTraceCount = 3;

    public TraceHelper( int TraceCount){
        localTraceCount = TraceCount;
    }

    /**
     * Get the location of the first trace
     * @return  This is the start of the trace pattern on the screen
     */
    public float getTraceStart(){
        return (localTraceCount-1.0f)/(float)localTraceCount;
    }

    /**
     * Get the spacing between traces
     * @return  This is the separation between traces
     */
    public float getTraceIncrement(){

        return (-2.0f/((float)localTraceCount));
    }


}
