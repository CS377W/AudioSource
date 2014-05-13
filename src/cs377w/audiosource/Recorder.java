package cs377w.audiosource;

/*
 * Reference: http://stackoverflow.com/questions/5139739/android-audiorecord-wont-initialize-2nd-time
 *
 *
 * == TODO ==
 * There are probably race conditions here...
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;

public class Recorder implements Runnable {
    private volatile boolean cancelled = false;
    private volatile boolean isRunning = false;
    private DatagramSocket socket = null;
    private InetAddress destination = null;
    
    public Handler handler;
    public RecognitionResult recognitionResult;
	
    private static final int port = 50005;

    @Override
    public void run() {
    	isRunning = true;
    	
    	try {
			destination = InetAddress.getByName("192.241.193.147");
			Log.d("CS377W", "Address retrieved");
			socket = new DatagramSocket();
			Log.d("CS377W", "Socket Created");
			Thread senderThread = new Thread(new Sender());
			Thread receiverThread = new Thread(new Receiver());
			senderThread.start();
			receiverThread.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
    }

    public void cancel() {
    	isRunning = false;
        cancelled = true;
    }
    
    public boolean getIsRunning() {
    	return isRunning;
    }
    
    private class Sender implements Runnable {
		@Override
		public void run() {
	        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	        
			int bufferRead = 0;
			int bufferSize = RecorderSingleton.instance.getBufferSize();
			byte[] tempBuffer = new byte[bufferSize];
			Log.d("CS377W", "Buffer created of size " + bufferSize);
			
			if (!RecorderSingleton.instance.start()) {
				Log.d("CS377W", "Recorder singleton not started.");
				isRunning = false;
				socket.close();
				return;
			}
			
			try {
				Log.d("CS377W", "Recorder Started");
				while (!cancelled) {
					bufferRead = RecorderSingleton.instance.read(tempBuffer);
					if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
						throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
					} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
						throw new IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
					}

					socket.send(new DatagramPacket(tempBuffer, tempBuffer.length, destination, port));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Close resources...
				RecorderSingleton.instance.stop();
				socket.close();
			}
			
			isRunning = false;
		}
    }
    
    private class Receiver implements Runnable {
		@Override
		public void run() {
			while (!cancelled) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[256];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					while (!baos.toString().contains("}") && !baos.toString().startsWith("null")) {
						socket.receive(packet);
						baos.write(packet.getData());
					}
					// Log.d("CS377W", baos.toString());
					
					String received = baos.toString();
					if (!received.startsWith("null")) {
						JSONObject obj = (JSONObject)JSONValue.parse(received.substring(0, received.indexOf("}")+1));
						
						recognitionResult.songName = (String)obj.get("song_name");
						recognitionResult.seconds = ((Number)obj.get("seconds")).doubleValue();
						System.out.println(recognitionResult);
						handler.obtainMessage().sendToTarget();
						
						cancel();
					} else {
						recognitionResult.songName = null;
						handler.obtainMessage().sendToTarget();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}
