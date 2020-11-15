package TeachersCode;

import java.io.*;
import javax.sound.sampled.*;

public class AudioPlayer2 {
	public static void main(String[] args) throws Exception {
//		if (args.length != 1) {
//			System.err.println("usage: java AudioPlayer2 file");
//			return;
//		}

		File file = new File("src\\AudioFiles\\a.wav");
		AudioInputStream ain = AudioSystem.getAudioInputStream(file);

		AudioFormat format = ain.getFormat();
		System.err.println(format);

		long l = ain.getFrameLength();
		float r = format.getFrameRate();
		System.err.println(l + " frames in " + l / r + " seconds");

		SourceDataLine line = AudioSystem.getSourceDataLine(format);
		line.open(format);
		line.start();

		byte[] buf = new byte[1024];
		int n;
		while ((n = ain.read(buf)) != -1)
			line.write(buf, 0, n);
		line.drain();

		line.stop();
		line.close();
	}
}
