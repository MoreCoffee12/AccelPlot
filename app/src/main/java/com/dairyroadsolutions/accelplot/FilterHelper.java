package com.dairyroadsolutions.accelplot;

import android.util.Log;
import java.lang.Math;

/**
 * Created by Brian on 9/6/2015.
 * This class is a Java re-cast of the BASIC code found in chapter 33 of
 * 'The Scientist's and Engineer's Guide to Digital Signal Processors'
 * handbook, Figures 33-7 and Figures 33-8.
 */
public class FilterHelper {

    /**
     * Object variables
     */
    public static final int MAXKERNELSIZE = 32;
    private double dA[] = new double[MAXKERNELSIZE];
    private double dB[] = new double[MAXKERNELSIZE];

    private double dHPCorner;
    private double dLPCorner;
    private double dSamplingFrequency;
    private long lNP;
    private double dPR;

    private double dPi;
    private boolean bHighPass;
    private boolean bLowPass;
    private boolean bIsDirty;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName() +":FilterHelper";


    /**
     * Update the HP corner frequency
     * @param dHPCornerNew  New frequency, hertz
     * @return              True if the update succeeded
     */
    public boolean bSetHPCorner(double dHPCornerNew){
        if( dHPCornerNew <= 0){
            return false;
        }
        if( dHPCornerNew > dSamplingFrequency/2.0 ){
            return false;
        }
        dHPCorner = dHPCornerNew;
        bIsDirty = true;


        return true;
    }

    /**
     * Update the LP corner frequency
     * @param dLPCornerNew  New frequency, hertz
     * @return              True if update succeeded
     */
    public boolean bSetLPCorner(double dLPCornerNew){
        if( dLPCornerNew <= 0){
            return false;
        }
        if( dLPCornerNew > dSamplingFrequency/2.0 ){
            return false;
        }
        dLPCorner = dLPCornerNew;
        bIsDirty = true;


        return true;
    }

    /**
     * Update the sampling frequency
     * @param dSamplingFrequencyNew     New sampling frequency in hertz
     * @return                          True if the updated succeeded
     */
    public boolean bSetSamplingFrequency(double dSamplingFrequencyNew){
        if(dSamplingFrequencyNew <= 0 ){
            return false;
        }
        dSamplingFrequency = dSamplingFrequencyNew;
        bIsDirty = true;


        return true;
    }

    /**
     * Update the number of poles on the filter
     * @param lNPNew        Number of poles for the filter
     * @return              True if the update succeeded
     */
    public boolean bSetNumberPoles(long lNPNew){
        if( lNPNew <= 0 ){
            return false;
        }

        lNP = lNPNew;
        bIsDirty = true;

        return true;

    }

    /**
     * Update the percent ripple
     * @param dPRNew    New percent ripple
     */
    public void setPercentRipple(double dPRNew){
        dPR = dPRNew;
        bIsDirty = true;
    }

    /**
     * Update the boolean for a highpass filter
     * @param bHighPassNew  True if this is a highpass filter
     */
    public void setHighPass( boolean bHighPassNew ){
        bHighPass = bHighPassNew;
        bIsDirty = true;
    }

    /**
     * Update the boolean for a lowpass filter
     * @param bLowPassNew  True if this is a lowpass filter
     */
    public void setLowPass( boolean bLowPassNew ){
        bLowPass = bLowPassNew;
        bIsDirty = true;
    }

    /**
     * Return the numerator filter coefficient at index iAindex
     * @param iAIndex       Index of the filter coefficient
     * @return              Value of the filter coefficient
     */
    public double dGet_A(int iAIndex)
    {
        if( bIsDirty){
            bCalcCoefficients();
        }
        return dA[iAIndex];
    }

    /**
     * Return the denominator filter coefficient at index iBIndex
     * @param iBIndex       Index of the filter coefficient
     * @return              Value of the filter coefficient
     */
    public double dGet_B(int iBIndex)
    {
        if( bIsDirty){
            bCalcCoefficients();
        }
        return dB[iBIndex];
    }

    /**
     * Return the HP corner frequency
     * @return      Highpass corner frequency
     */
    public double getHPCorner(){
        return dHPCorner;
    }

    /**
     * Return the LP corner frequency
     * @return      Lowpass corner frequency
     */
    public double getLPCorner(){
        return dLPCorner;
    }

    /**
     * Return the sampling frequency
     * @return      Sampling frequency in hertz
     */
    public double getSamplingFrequency(){
        return dSamplingFrequency;
    }

    /**
     * Return the number of filter poles
     * @return      Number of filter poles
     */
    public long getNumberPoles(){
        return lNP;
    }

    /**
     * Return the percent ripple in the filter
     * @return      Percent ripple
     */
    public double getPercentRipple(){
        return dPR;
    }

    /**
     * Return the boolean indicating this is a highpass filter
     * @return      True for a highpass filter
     */
    public boolean getHighPass(){
        return bHighPass;
    }

    /**
     * Return the boolean indicating this is a lowpass filter
     * @return      True for a lowpass filter
     */
    public boolean getLowPass(){
        return bLowPass;
    }

    /**
     * Default constructor
     */
    FilterHelper(){

        dPi = 3.1415926535897932384626433832795;

        // Need to set the values for initial configuration
        bSetHPCorner(150.0);
        bSetLPCorner(150.0);
        bSetSamplingFrequency(3600.0);
        setHighPass(false);
        setLowPass(true);

        // Set the percent ripple (0 to 29)
        setPercentRipple(0.5);

        // Set the number of poles (2,4,...20)
        bSetNumberPoles(4);

    }

    /**
     * Initialize the kernel arrays to zero.
     *
     * @return  True if the calculation succeeded.
     */
    private boolean bResetFilterKernel() {

        int i;

        for (i = 0; i < MAXKERNELSIZE; i++)
        {
            dA[i] = 0;
            dB[i] = 0;
        }

        return true;
    }

    /**
     * This method selects the appropriate method for
     * calculating filter coefficients
     * @return      True for success
     */
    public boolean bCalcCoefficients() {

        boolean bTemp = false;

        // Nothing special if this is simply high-pass or
        // low-pass filtered
        if (bHighPass) {
            bTemp =  bCalcHPLPCoefficients(true);
            if( bTemp ){
                bIsDirty = false;
            }
        }
        if (bLowPass) {
            bTemp =  bCalcHPLPCoefficients(false);
            if( bTemp ){
                bIsDirty = false;
            }
        }

        // Success?
        return bTemp;
    }


    /**
     * This method calculates the recursion coefficients for a
     * Chebychev Type I filter. The filter coefficients are stored in
     * the arrays A and B where A is the numerator (feedforward
     * coefficients) and B is the denominator (feedback coefficients)
     * @param bIsHighPass   Is the filter a highpass or lowpass?
     * @return              True if the calculation succeeded
     */
    public boolean bCalcHPLPCoefficients( boolean bIsHighPass ){

        // Local variables
        double dTA[] = new double[MAXKERNELSIZE];
        double dTB[] = new double[MAXKERNELSIZE];
        int i;
        int p;
        double dFC;
        double dRP;
        double dIP;
        double dES;
        double dVX;
        double dKX;
        double dt;
        double dW;
        double dM;
        double dD;
        double dX0;
        double dX1;
        double dX2;
        double dY1;
        double dY2;
        double dk;
        double dA0;
        double dA1;
        double dA2;
        double dB1;
        double dB2;
        double dSA;
        double dSB;
        double dGAIN;
        long lLH;

        // Reset the filter kernel
        bResetFilterKernel();

        // Initialize arrayd for the recursive calculation
        dA[2] = 1;
        dB[2] = 1;

        //Enter 0 for LP, 1 for HP filter
        if( bIsHighPass )
        {
            dFC = ( dHPCorner / dSamplingFrequency );
            lLH = 1;
        }
        else
        {
            dFC = ( dLPCorner / dSamplingFrequency );
            lLH = 0;
        }

        // Loop for each pole-zero pair
        for( p=1; p<=( lNP / 2 ); p++)
        {
            //Calculate the pole location on the unit circle
            dRP = -Math.cos(dPi / ((double) lNP * 2) + (p - 1) * dPi / (double) lNP);
            dIP = Math.sin(dPi / ( (double)lNP * 2) + (p - 1) * dPi / (double)lNP);

            //Warp from a circle to an ellipse
            if (dPR != 0 )
            {
                dES = Math.sqrt( Math.pow((100 / (100 - dPR) ),2) - 1);
                dVX = (1 / (double)lNP) * Math.log((1 / dES) + Math.sqrt((1.0 / (dES * dES)) + 1.0));
                dKX = (1 / (double)lNP) * Math.log((1 / dES) + Math.sqrt((1.0 / (dES * dES)) - 1.0));
                dKX = (Math.exp(dKX) + Math.exp(-dKX)) / 2.0;
                dRP = dRP * ((Math.exp(dVX) - Math.exp(-dVX)) / 2.0) / dKX;
                dIP = dIP * ((Math.exp(dVX) + Math.exp(-dVX)) / 2.0) / dKX;
            }

            //s-domain to z-domain conversion
            dt = ( 2.0 * Math.tan(1.0 / 2.0) );
            dW = ( 2.0 * dPi * dFC );
            dM = ( ( dRP * dRP ) + (dIP * dIP ) );
            dD = 4.0 - 4.0 * dRP * dt + dM * dt * dt;
            dX0 = ( ( dt * dt) / dD );
            dX1 = ( ( 2.0 * dt *dt ) / dD );
            dX2 = ( ( dt * dt ) / dD );
            dY1 = ( ( 8.0 - 2.0 * dM * dt * dt) / dD );
            dY2 = ( (-4.0 - 4.0 * dRP * dt - dM * dt * dt) / dD );

            // LP TO LP, or LP TO HP transform
            dk = 0;
            if ( lLH == 1 )
                dk = -Math.cos(dW / 2.0 + 1.0 / 2.0) / Math.cos(dW / 2.0 - 1.0 / 2.0);
            if ( lLH == 0 )
                dk = Math.sin(1.0 / 2.0 - dW / 2.0) / Math.sin(1.0 / 2.0 + dW / 2.0);
            dD = 1 + dY1 * dk - dY2 * dk * dk;

            dA0 = ( ( dX0 - dX1 * dk + dX2 * dk * dk) / dD );
            dA1 = ( (-2 * dX0 * dk + dX1 + dX1 * dk * dk - 2 * dX2 * dk) / dD );
            dA2 = ( ( dX0 * dk * dk - dX1 * dk + dX2) / dD );
            dB1 = ( ( 2 * dk + dY1 + dY1 * dk * dk - 2 * dY2 * dk) / dD );
            dB2 = ( ( -(dk * dk) - dY1 * dk + dY2) / dD );

            if ( lLH == 1 )
                dA1 = -dA1;
            if ( lLH == 1 )
                dB1 = -dB1;

            // Add coefficients to the cascade
            for (i=0; i<MAXKERNELSIZE; i++)
            {
                dTA[i] = dA[i];
                dTB[i] = dB[i];
            }

            for (i=2; i<MAXKERNELSIZE; i++)
            {
                dA[i] = (dA0 * dTA[i]) + (dA1 * dTA[i - 1]) + (dA2 * dTA[i - 2]);
                dB[i] = dTB[i] - (dB1 * dTB[i - 1]) - dB2 * (dTB[i - 2]);
            }

        }

        // Finish combining coefficients
        dB[2] = 0;

        for ( i = 0; i<(MAXKERNELSIZE-2); i++)
        {
            dA[i] = dA[i + 2];
            dB[i] = -(dB[i + 2]);
        }

        // Normalize the gain
        dSA = 0;
        dSB = 0;

        for (i=0; i<(MAXKERNELSIZE-2); i++)
        {
            if ( lLH == 0 )
                dSA = dSA + dA[i];
            if ( lLH == 0 )
                dSB = dSB + dB[i];
            if ( lLH == 1 )
                dSA = dSA + dA[i] * Math.pow((double)-1, i);
            if ( lLH == 1 )
                dSB = dSB + dB[i] * Math.pow((double)-1, i);
        }

        dGAIN = dSA / (1 - dSB);

        for ( i=0; i<(MAXKERNELSIZE-2); i++)
        {
            dA[i] = dA[i] / dGAIN;
        }

        // Success
        return true;

    }


    /**
     * This function logs the filter to the debug file
     */
    public void LogFilter(){

        for( int idx=0; idx<MAXKERNELSIZE; ++idx){

            Log.i(strTag, String.format("A[%02d]: %1.5E    B[%02d]: %1.5E\n", idx, dGet_A(idx), idx, dGet_B(idx)));

        }
        Log.i(strTag, "Highpass Corner (Hz)   : " + getHPCorner());
        Log.i(strTag, "Lowpass Corner (Hz)    : " + getLPCorner());
        Log.i(strTag, "Sampling frequency (Hz): " + getSamplingFrequency());
        Log.i(strTag, "Number of poles        : " + getNumberPoles());
        Log.i(strTag, "Percent ripple         : " + getPercentRipple());
        Log.i(strTag, "Highpass boolean       : " + getHighPass());
        Log.i(strTag, "Lowpass boolean        : " + getLowPass());

    }

    // Added this short function to compare two doubles to
    // allow for some small round-off error.  The technique
    // of comparing relative absolute values is discussed in
    // an article at:
    // http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
    // See that link for more details.
    boolean bCompareDoubles( double dA, double dB )
    {
        double dMaxRelDiff = 0.00001;

        // Calculate the difference.
        double dDiff = Math.abs(dA - dB);
        dA = Math.abs(dA);
        dB = Math.abs(dB);

        // Find the largest
        double dLargest = (dB > dA) ? dB : dA;

        // How's this look?
        if (dDiff <= dLargest * dMaxRelDiff)
            return true;

        // Didn't look so good.
        return false;
    }


    /**
     * This code checks all of the methods in the class and verifies basic functions
     * This needs to be integrated into the Android test suite
     */
    public void TestHarness() {

        // Locals
        final int ARRAYSIZE = 32768;
        double dData[] = new double[ARRAYSIZE];
        double dTestSampleFreq;
        double dTemp;

        // Show the values on instantiation
        Log.i(strTag, "Verifying values at instantiation.");
        LogFilter();

        // Construct the test waveform.
        for (int i = 0; i < ARRAYSIZE; i++) {
            dData[i] = Math.sin(40.0 * i * dPi / (double) ARRAYSIZE);
            dData[i] = dData[i] + Math.sin(400.0 * i * dPi / (double) ARRAYSIZE);
        }

        // Check code reponse when the corner values or sampling
        // frequency equal zero.
        Log.i(strTag, "Verifying code response when corner values equal zero.");
        if (bSetSamplingFrequency(0.0)) {
            Log.e( strTag, "The filter object failed to trap a zero value sampling frequency." );
        }
        if (bSetHPCorner(0.0)){
            Log.e( strTag, "The filter object failed to trap a zero value highpass corner." );
        }
        if (bSetLPCorner(0.0)){
            Log.e( strTag, "The filter object failed to trap a zero value lowpass corner." );
        }

        // Check the recursion coefficient values.
        // Configure the filter for lowpass with an fc of
        // 0.01, where fc equals the corner frequency
        // divided by the sampling frequency.
        Log.i(strTag, "Verifying recursion values when fc is 0.01.");
        setLowPass(true);
        dTestSampleFreq = ((double) ARRAYSIZE * 2.8);
        if( !bSetSamplingFrequency(dTestSampleFreq)){
            Log.e( strTag, "bSetSamplingFrequency failed for a valid sampling frequency." );
        }
        if( !bSetLPCorner( dTestSampleFreq * 0.01 ) ) {
            Log.e( strTag, "bSetLPCorner failed for a valid setting." );
        }
        dTemp = 4.149425e-07;
        if( !bCompareDoubles( dTemp, dGet_A(0) ) )        {
            Log.e(strTag, "The recursion coefficients did not match for 0.01 corner frequency.");
            Log.i(strTag, String.format("Correct A[0]: %1.10f | Calculated A[0]: %1.10f", dTemp, dGet_A(0)));
        }
        dTemp = -5.688233;
        if( !bCompareDoubles( dTemp, dGet_B(2) ) ){
            Log.e( strTag, "The recursion coefficients did not match for 0.01 corner frequency." );
            Log.i(strTag, String.format("Correct B[2]: %1.10f | Calculated B[2]: %1.10f", dTemp, dGet_B(2)));
        }

        // Allow 4-pole high pass coefficients to be
        // calculated and compared to those presented in Table 20-2
        // Modify the filter object characteristics for a 4-pole filter
        Log.i(strTag, "Verifying lowpass recursion values when fc is 0.20.");
        setLowPass(true);
        if (!bSetNumberPoles(4)) {
            Log.e(strTag, "bSetNumberPoles failed.");
        }
        // Set the corner frequency ratio at 0.20 = 18350 Hz
        if (!bSetLPCorner(dTestSampleFreq * 0.20)){
            Log.e(strTag, "bSetLP Corner failed.");
        }
        dTemp = 3.22455E-02;
        if( !bCompareDoubles( dTemp, dGet_A(0) ) )        {
            Log.e(strTag, "The recursion coefficients did not match for 0.20 corner frequency.");
            Log.i(strTag, String.format("Correct A[0]: %1.10f | Calculated A[0]: %1.10f", dTemp, dGet_A(0)));
        }
        dTemp =  -1.20388E+00;
        if( !bCompareDoubles(dTemp, dGet_B(2)) ){
            Log.e( strTag, "The recursion coefficients did not match for 0.20 corner frequency." );
            Log.i(strTag, String.format("Correct B[2]: %1.10f | Calculated B[2]: %1.10f", dTemp, dGet_B(2)));
        }


        // Document results
        LogFilter();




        // Tests completed successfully
        Log.i(strTag, "All tests complete.");

    }

}
