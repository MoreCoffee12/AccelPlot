package com.dairyroadsolutions.accelplot;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


/**
 * Class to provide audio output functions
 * Brian Howard
 * 8 August 2016
 */
public class AudioHelper {

    private final static int SAMPLE_RATE = 44100;

    private float freqOfTone1 = 1000f;
    private float freqOfTone2 = 1000f;

    private short[] buffer = null;

    boolean bAudioOut = false;
    boolean bRunLoop = true;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();

    // thread object
    private  Thread t;

    public float getFreqOfTone1() {
        return freqOfTone1;
    }

    public void setFreqOfTone(float freqOfTone) {

        this.freqOfTone1 = freqOfTone;
        this.freqOfTone2 = freqOfTone;
    }

    public void setFreqOfTone(float freqOfTone1, float freqOfTone2) {

        this.freqOfTone1 = freqOfTone1;
        this.freqOfTone2 = freqOfTone2;
    }

    /**
     * Constructor, here we get the thread started
     */
    public AudioHelper(){


        t = new Thread() {
            public void run() {

                // set the buffer size
                int iBuffSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                iBuffSize = iBuffSize+1024;


                // create an audiotrack object
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, iBuffSize,
                        AudioTrack.MODE_STREAM);

                short samples[] = new short[iBuffSize];
                int amp = 20000;
                double twopi = 8.*Math.atan(1.);
                double ph1 = 0.0;
                double ph2 = 0.0;

                // start audio
                audioTrack.play();

                // synthesis loop
                while(bRunLoop){
                    if( bAudioOut){

                        for(int i=0; i < iBuffSize; i +=2){
                            samples[i] = (short) (amp*Math.sin(ph1));
                            ph1 += twopi*freqOfTone1 /SAMPLE_RATE;

                            samples[i+1] = (short) (amp*Math.sin(ph2));
                            ph2 += twopi*freqOfTone2/SAMPLE_RATE;
                        }

                        audioTrack.write(samples, 0, iBuffSize);
                    }

                }
                audioTrack.stop();
                audioTrack.release();
            }
        };
        t.start();
    }

    /**
     * External call to change state of audio out
     * @param bAudioOutNew      Set to true enable audio out
     */
    public void vSetAudioOut(boolean bAudioOutNew){

        bAudioOut = bAudioOutNew;

    }


}
