package TeachersCode;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class AudioServer implements Runnable {

	Socket so;
	AudioServer(Socket so) {
		this.so = so;
	}

	@Override
	public void run() {
		try {
			OutputStream out = so.getOutputStream();
			out.write(header);
			out.flush();
			out.write(buf);
			out.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
		try {
			so.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	static int getEncoding(AudioFormat format) {
		AudioFormat.Encoding e = format.getEncoding();
		int i = 0;
		if (e == AudioFormat.Encoding.ALAW)
			i = 1;
		else if (e == AudioFormat.Encoding.PCM_FLOAT)
			i = 2;
		else if (e == AudioFormat.Encoding.PCM_SIGNED)
			i = 3;
		else if (e == AudioFormat.Encoding.PCM_UNSIGNED)
			i = 4;
		else if (e == AudioFormat.Encoding.ULAW)
			i = 5;
		return i;
	}

	static byte[] pack(AudioFormat format) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(getEncoding(format));
		dos.writeFloat(format.getSampleRate());
		dos.writeInt(format.getSampleSizeInBits());
		dos.writeInt(format.getChannels());
		dos.writeInt(format.getFrameSize());
		dos.writeFloat(format.getFrameRate());
		dos.writeBoolean(format.isBigEndian());
		dos.close();
		byte[] aa = bos.toByteArray();
		bos.close();
		return aa;
	}

	static byte[] wrap(byte[] aa) {
		long n = aa.length;
		byte[] bb = new byte[(int) n + 4];
		for (int i = 0; i < 4; i++)
			bb[i] = (byte) (n >> (3 - i) * 8 & 255);
		for (int i = 0; i < n; i++)
			bb[4 + i] = aa[i];
		return bb;
	}

	static byte[] header, buf;

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: java AudioServer file port");
			return;
		}

		File file = new File(args[0]);
		AudioInputStream ain = AudioSystem.getAudioInputStream(file);

		AudioFormat format = ain.getFormat();
		System.err.println(format);

		long l = ain.getFrameLength();
		float r = format.getFrameRate();
		System.err.println(l + " frames in " + l / r + " seconds");

		header = wrap(pack(format));
		buf = ain.readAllBytes();

		String host = InetAddress.getLocalHost().toString();
		int port = Integer.parseInt(args[1]);
		System.err.println(host + ":" + port);

		ServerSocket sso = new ServerSocket(port);

		while (true) {
			Socket so = sso.accept();
			System.err.println("connection from " + so.getRemoteSocketAddress()
					+ " to " + so.getLocalSocketAddress());

			new Thread(new AudioServer(so)).start();
		}
	}
}
