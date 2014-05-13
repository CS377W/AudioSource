package cs377w.audiosource;

/*
 * Reference: http://stackoverflow.com/questions/5139739/android-audiorecord-wont-initialize-2nd-time
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder; 

public class RecorderSingleton {

    private static final int FREQUENCY = 44100;

    public static RecorderSingleton instance = new RecorderSingleton();
    private AudioRecord recordInstance = null;
    private int bufferSize;

    private RecorderSingleton() {
        bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    public boolean init() {
        recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        return recordInstance.getState() == AudioRecord.STATE_INITIALIZED;
    }

    public int getBufferSize() {
        return bufferSize;
    }
    public boolean start() {
        if (recordInstance != null && recordInstance.getState() != AudioRecord.STATE_UNINITIALIZED) {
            if (recordInstance.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                recordInstance.stop();
            }
            recordInstance.release();
        }
        if (!init()) {
            return false;
        }
        recordInstance.startRecording();
        return true;
    }

    public int read(byte[] tempBuffer) {
        if (recordInstance == null) {
            return AudioRecord.ERROR_INVALID_OPERATION;
        }
        int ret = recordInstance.read(tempBuffer, 0, bufferSize);
        return ret;
    }

    public void stop() {
        if (recordInstance == null) {
            return;
        }
        recordInstance.stop();
        recordInstance.release();
    }
}
