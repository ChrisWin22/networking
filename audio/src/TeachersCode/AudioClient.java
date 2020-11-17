package TeachersCode;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class AudioClient {

	static AudioFormat.Encoding getEncoding(int i) {
		AudioFormat.Encoding e = null;
		if (i == 1)
			e = AudioFormat.Encoding.ALAW;
		else if (i == 2)
			e = AudioFormat.Encoding.PCM_FLOAT;
		else if (i == 3)
			e = AudioFormat.Encoding.PCM_SIGNED;
		else if (i == 4)
			e = AudioFormat.Encoding.PCM_UNSIGNED;
		else if (i == 5)
			e = AudioFormat.Encoding.ULAW;
		return e;
	}

	static AudioFormat unpack(byte[] aa) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(aa);
		DataInputStream dis = new DataInputStream(bis);
		AudioFormat.Encoding encoding = getEncoding(dis.readInt());
		float sampleRate = dis.readFloat();
		int sampleSizeInBits = dis.readInt();
		int channels = dis.readInt();
		int frameSize = dis.readInt();
		float frameRate = dis.readFloat();
		boolean bigEndian = dis.readBoolean();
		dis.close();
		bis.close();
		return new AudioFormat(encoding, sampleRate, sampleSizeInBits,
				channels, frameSize, frameRate, bigEndian);
	}

	static AudioFormat getFormat(InputStream in) throws IOException {
		int n = 0;
		for (int i = 0; i < 4; i++)
			n = n << 8 | in.read();
		byte[] aa = in.readNBytes(n);
		return unpack(aa);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: java AudioClient host port");
			return;
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		Socket so = new Socket(host, port);
		System.err.println("connection from " + so.getLocalSocketAddress()
				+ " to " + so.getRemoteSocketAddress());

		InputStream in = so.getInputStream();

		AudioFormat format = getFormat(in);
		System.err.println(format);

		SourceDataLine line = AudioSystem.getSourceDataLine(format);
		line.open(format);
		line.start();

		byte[] buf = new byte[1024];
		int n;
		while ((n = in.read(buf)) != -1)
			line.write(buf, 0, n);
		line.drain();

		line.stop();
		line.close();
	}
}
