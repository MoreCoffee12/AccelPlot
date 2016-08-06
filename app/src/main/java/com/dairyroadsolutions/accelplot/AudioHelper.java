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

    private final static float freqOfTone = 440.0f;

    private short[] buffer = null;

    boolean bAudioOut = false;
    boolean bRunLoop = true;

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();


    public  Thread t;


    /**
     * Constructor, here we get the thread started
     */
    public AudioHelper(){


        t = new Thread() {
            public void run() {
                // set process priority
//                setPriority(Thread.MAX_PRIORITY);
                // set the buffer size
                int buffsize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);


                // create an audiotrack object
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, buffsize,
                        AudioTrack.MODE_STREAM);

                short samples[] = new short[buffsize];
                int amp = 10000;
                double twopi = 8.*Math.atan(1.);
                double fr = 440.f;
                double ph = 0.0;

                // start audio
                audioTrack.play();
                Log.i(strTag, ":HM:                          Playing audioTrack: ");

                // synthesis loop
                while(bRunLoop){
                    fr =  440;
                    for(int i=0; i < buffsize; i++){
                        samples[i] = (short) (amp*Math.sin(ph));
                        ph += twopi*fr/SAMPLE_RATE;
                    }

                    if( bAudioOut){
                        audioTrack.write(samples, 0, buffsize);
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
