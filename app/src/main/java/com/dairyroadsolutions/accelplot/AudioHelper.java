package com.dairyroadsolutions.accelplot;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.app.Activity;
import android.util.Log;


/**
 * Created by Brian on 8/5/2016.
 */
public class AudioHelper extends Activity {

    private int targetSamples = 1024;
    private int sample[] = new int[targetSamples];
    private byte generatedSnd[] = new byte[targetSamples];
    private int sampleRate = 44100;
    private int numCycles;
    private int numSamples;

    private float freqOfTone = 440;

    private boolean bAudioOut = false;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();


    private void vUpdateAudio(){

        UpdateFreq();
        if( bAudioOut == true){
            playSound();
        }

    }

    /**
     * toggle the audio out state
     */
    public void vToggleRunning(){
        bAudioOut = !bAudioOut;
        vUpdateAudio();
    }

    /**
     * Set the audio out to true to started audio out
     * @param bAudioOutNew  True to turn on audio out
     */
    public void vSetAudioOut(boolean bAudioOutNew){

        bAudioOut = bAudioOutNew;
        vUpdateAudio();
    }

    // From http://blog.workingsi.com/2012/03/android-tone-generator-app.html

    // Based on but modified and improved from
    // http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
    // functions for tone generation
    void genTone(double freqOfTone) {

        //clean out the arrays
        for (int i = 0; i < targetSamples * 2; ++i) {
            sample[i] = 0;
        }
        for (int i = 0; i < targetSamples * 2 * 2; ++i) {
            generatedSnd[i] = (byte) 0x0000;
        }

    }

    /**
     * Adjustments to make the start and stop evenly
     */
    void UpdateFreq() {

        // calculate adjustments to make the sample start and stop evenly
        numCycles = (int)(0.5f + freqOfTone * (float)targetSamples/(float)sampleRate);
        Log.d(strTag, ":HM:                            numCycles: " + numCycles);
        numSamples = (int)(0.5f + numCycles * (float)sampleRate/(float)freqOfTone);
        Log.d(strTag, ":HM:                           numSamples: " + numSamples);

        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = (int)Math.sin(2 * Math.PI * (double)i / ((double)sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        int idx = 0;
        for (double dVal : sample) {
//            // scale loudness by frequency
//            double amplitude = (double) (32767 * 5/(Math.log(freqOfTone)));
//            if (amplitude > 32767) amplitude = 32767;
//            // scale signal to amplitude
//            short val = (short) (dVal * amplitude);
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }


    void playSound(){
//        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, numSamples*2,
//                AudioTrack.MODE_STREAM);
//        audioTrack.write(generatedSnd, 0, numSamples*2);
//        audioTrack.play();
//        while (bAudioOut ==true){
//            audioTrack.write(generatedSnd, 0, numSamples*2);
//        }
//        audioTrack.stop();
//        bAudioOut = false;

    }

}
