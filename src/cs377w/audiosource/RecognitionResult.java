package cs377w.audiosource;

public class RecognitionResult {
	public String songName;
	public double seconds;
	
	public RecognitionResult() {
		songName = null;
		seconds = -1;
	}
	
	public String toString() {
		return "song:" + songName + " seconds:" + seconds;
	}
}
