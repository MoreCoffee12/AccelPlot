package com.dairyroadsolutions.accelplot;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.os.SystemClock;
import android.util.Log;


/**
 * Class to provide audio output functions
 * Brian Howard
 * 8 August 2016
 */
public class AudioHelper {

    private final static int SAMPLE_RATE = 44100;

    private float freqOfTone = 2000f;

    private short[] buffer = null;

    boolean bAudioOut = false;
    boolean bRunLoop = true;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();

    // thread object
    private  Thread t;

    public float getFreqOfTone() {
        return freqOfTone;
    }

    public void setFreqOfTone(float freqOfTone) {
        this.freqOfTone = freqOfTone;
    }


    /**
     * Constructor, here we get the thread started
     */
    public AudioHelper(){


        t = new Thread() {
            public void run() {

                // set the buffer size
                int iBuffSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                iBuffSize = iBuffSize+1024;


                // create an audiotrack object
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, iBuffSize,
                        AudioTrack.MODE_STREAM);

                short samples[] = new short[iBuffSize];
                int amp = 20000;
                double twopi = 8.*Math.atan(1.);
                double ph = 0.0;

                // start audio
                audioTrack.play();

                // synthesis loop
                while(bRunLoop){
                    if( bAudioOut){

                        for(int i=0; i < iBuffSize; i++){
                            samples[i] = (short) (amp*Math.sin(ph));
                            ph += twopi*freqOfTone/SAMPLE_RATE;
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
