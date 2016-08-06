package com.dairyroadsolutions.accelplot;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

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

    // debug
    private static final String strTag = MainActivity.class.getSimpleName();


    private void vUpdateAudio(){
        if( bAudioOut == true ){ AudioOut(); }
    }

    public AudioHelper(){
                
    }

    /**
     * External call to change state of audio out
     * @param bAudioOutNew      Set to true enable audio out
     */
    public void vSetAudioOut(boolean bAudioOutNew){

        bAudioOut = bAudioOutNew;
        vUpdateAudio();

    }

    public void AudioOut() {
        AudioTrack at;
        int buffsize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        buffer = new short[buffsize];
        fillbuf(buffsize);

        at = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize<<1,
                AudioTrack.MODE_STATIC);
        at.write(buffer, 0, buffsize);



        at.play();

    }

    void fillbuf(int bufsizsamps) {
        double omega, t;
        double dt = 1.0 / SAMPLE_RATE;
        t = 0.0;
        omega = (float) (2.0 * Math.PI * freqOfTone);
        for (int i = 0; i < bufsizsamps; i++) {
            buffer[i] = (short) (32000.0 * Math.sin(omega * t));
            t += dt;
        }
    }

}
