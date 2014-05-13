package cs377w.audiosource;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.TextView;

public class Send extends Activity {
	private TextView tv;
	private GestureDetector mGestureDetector;
	private Recorder recorder;
	private Thread recorderThread;
    public RecognitionResult recognitionResult = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.content);
		mGestureDetector = createGestureDetector(this);
	}
	
    @SuppressLint("HandlerLeak") public Handler recognitionResultHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		if (recognitionResult == null) return;
    		tv.setText(recognitionResult.toString());
    		recorder = null;
    		recorderThread = null;
    	}
    };


	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					if (recorder == null) {
						recorder = new Recorder();
						// TODO: think of a more elegant way to pass these along
						recognitionResult = new RecognitionResult();
						recorder.recognitionResult = recognitionResult;
						recorder.handler = recognitionResultHandler;
						recorderThread = new Thread(recorder);
					}
					
					if(!recorder.getIsRunning()) {
						tv.setText("Streaming...");
						recorderThread.start();
					} else {
						recorder.cancel();
						recorder = null;
						recorderThread = null;
						tv.setText("Stopped Streaming");
					}
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					return true;
				}
				return false;
			}
		});
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				// do something on scrolling
				return true;
			}
		});
		return gestureDetector;
	}

	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}
	
	@Override
	public void onDestroy() { 
	    super.onDestroy();
	    if (recorder != null) {
	    	recorder.cancel();
	    	recorder = null;
	    }
	}
}
