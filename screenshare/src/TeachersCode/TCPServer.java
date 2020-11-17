package TeachersCode;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

public class TCPServer {

	static byte[] buffer = null;
	static Object lock = new Object();

	static class Producer extends Thread {
		Robot robot;
		Rectangle rectangle;
		AffineTransformOp atop;

		static final double SC = 0.4;

		Producer(Robot robot) {
			this.robot = robot;
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			rectangle = new Rectangle(0, 0, d.width, d.height);
			atop = new AffineTransformOp(
					AffineTransform.getScaleInstance(SC, SC),
					AffineTransformOp.TYPE_BICUBIC);
		}

		BufferedImage scale(BufferedImage img) {
			int w = (int) (img.getWidth() * SC);
			int h = (int) (img.getHeight() * SC);
			BufferedImage img_ = new BufferedImage(w, h, img.getType());
			img_ = atop.filter(img, img_);
			return img_;
		}

		byte[] pack(BufferedImage img) throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(img, "gif", bos);
			byte[] aa = bos.toByteArray();
			bos.close();
			return aa;
		}

		byte[] wrap(byte[] aa) {
			long n = aa.length;
			byte[] bb = new byte[(int) n + 4];
			for (int i = 0; i < 4; i++)
				bb[i] = (byte) (n >> (3 - i) * 8 & 255);
			for (int i = 0; i < n; i++)
				bb[4 + i] = aa[i];
			return bb;
		}

		@Override
		public void run() {
			try {
				while (true) {
					BufferedImage img = robot.createScreenCapture(rectangle);
					byte[] buf = wrap(pack(scale(img)));
					synchronized(lock) {
						buffer = buf;
						lock.notifyAll();
					}
					Thread.sleep(100);
				}
			} catch (IOException | InterruptedException e) {
				System.err.println(e);
			}
		}
	}

	static class Consumer extends Thread {
		Socket so;
		Consumer(Socket so) {
			this.so = so;
		}

		@Override
		public void run() {
			try {
				OutputStream out = so.getOutputStream();
				byte[] buf = null;
				while (true) {
					synchronized(lock) {
						while (buf == buffer)
							lock.wait();
						buf = buffer;
					}
					out.write(buf);
					out.flush();
				}
			} catch (IOException | InterruptedException e) {
				System.err.println(e);
			}
			try {
				so.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("usage: java TCPServer port");
			return;
		}

		String host = InetAddress.getLocalHost().toString();
		int port = Integer.parseInt(args[0]);
		System.err.println(host + ":" + port);

		new Producer(new Robot()).start();

		ServerSocket sso = new ServerSocket(port);

		while (true) {
			Socket so = sso.accept();
			System.err.println("connection from " + so.getRemoteSocketAddress()
					+ " to " + so.getLocalSocketAddress());

			new Consumer(so).start();
		}
	}
}
