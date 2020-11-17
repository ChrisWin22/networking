package TeachersCode;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;

public class UDPServer {

	static final byte DA = (byte) 0xDA;

	static DatagramSocket so;

	static class C {
		String s; InetAddress host; int port; long t;
		C(String s, InetAddress host, int port, long t) {
			this.s = s; this.host = host; this.port = port; this.t = t;
		}
	}
	static ArrayList<C> list = new ArrayList<>();

	static class Sender extends Thread {
		Robot robot;
		Rectangle rectangle;
		AffineTransformOp atop;

		static final double SC = 0.4;

		Sender(Robot robot) {
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

		void send() throws IOException {
			ArrayList<C> l = new ArrayList<>();
			synchronized(list) {
				long t = Calendar.getInstance().getTimeInMillis();
				for (C c : list)
					if (c.t > t)
						l.add(c);
			}
			if (l.isEmpty())
				return;

			BufferedImage img = robot.createScreenCapture(rectangle);
			byte[] buf = pack(scale(img));
			for (C c : l) {
				DatagramPacket p = new DatagramPacket(buf, buf.length, c.host, c.port);
				so.send(p);
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					send();
					Thread.sleep(100);
				}
			} catch (IOException | InterruptedException e) {
				System.err.println(e);
				// reduce SC if "java.io.IOException: Message too long"
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("usage: java UDPServer port");
			return;
		}

		String host = InetAddress.getLocalHost().toString();
		int port = Integer.parseInt(args[0]);
		System.err.println(host + ":" + port);

		new Sender(new Robot()).start();

		so = new DatagramSocket(port);
		so.setSendBufferSize(1 << 20);

		byte[] buf = new byte[2];
		while (true) {
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			so.receive(p);

			if (p.getOffset() != 0 || p.getLength() != 2)
				continue;
			byte[] aa = p.getData();
			if (aa[0] != DA || aa[1] != DA)
				continue;

			InetAddress host_ = p.getAddress();
			int port_ = p.getPort();
			String s = host_ + ":" + port_;
			System.err.println("heartbeat from " + s);

			synchronized(list) {
				long t = Calendar.getInstance().getTimeInMillis()
					+ 10000; // 10 more seconds to live
				boolean found = false;
				for (C c : list)
					if (c.s.equals(s)) {
						found = true;
						c.t = t;
						break;
					}
				if (!found)
					list.add(new C(s, host_, port_, t));
			}
		}
	}
}
