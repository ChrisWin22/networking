package TeachersCode;

import java.io.*;
import javax.sound.sampled.*;

public class AudioPlayer1 {
	public static void main(String[] args) throws Exception {
//		if (args.length != 1) {
//			System.err.println("usage: java AudioPlayer1 file");
//			return;
//		}

		File file = new File("src\\AudioFiles\\a.wav");
		AudioInputStream ain = AudioSystem.getAudioInputStream(file);

		AudioFormat format = ain.getFormat();
		System.err.println(format);

		Clip clip = AudioSystem.getClip();
		clip.open(ain);

		int l = clip.getFrameLength();
		long m = clip.getMicrosecondLength();
		System.err.println(l + " frames in " + m / 1000000.0 + " seconds");

		clip.start();
		do
			Thread.sleep(1000);
		while (clip.isRunning());
	}
}
