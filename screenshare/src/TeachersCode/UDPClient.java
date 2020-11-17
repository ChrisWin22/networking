package TeachersCode;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

public class UDPClient implements Runnable {

	static final byte DA = (byte) 0xDA;

	static DatagramSocket so;
	static InetAddress host;
	static int port;

	@Override
	public void run() { // heartBeat
		try {
			byte[] buf = new byte[2];
			buf[0] = buf[1] = DA;
			while (true) {
				DatagramPacket p = new DatagramPacket(buf, 2, host, port);
				so.send(p);
				Thread.sleep(2000);
			}
		} catch (IOException | InterruptedException e) {
			System.err.println(e);
		}
	}

	static class Canvas_ extends Canvas {
		volatile BufferedImage image;

		@Override
		public void paint(Graphics g) {
			BufferedImage img = image;
			if (img == null)
				return;

			int w = getWidth(), h = getHeight();
			int w_ = img.getWidth(), h_ = img.getHeight();
			if (w >= w_ * h / h_)
				g.drawImage(img, 0, 0, w_ * h / h_, h, 0, 0, w_, h_, this);
			else
				g.drawImage(img, 0, 0, w, h_ * w / w_, 0, 0, w_, h_, this);
		}

		@Override
		public void update(Graphics g) {
			paint(g);
		}
	}

	static BufferedImage readImage(byte[] aa, int o, int n) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(aa, o, n);
		BufferedImage img = ImageIO.read(bis);
		bis.close();
		return img;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: java UDPClient host port");
			return;
		}

		host = InetAddress.getByName(args[0]);
		port = Integer.parseInt(args[1]);

		so = new DatagramSocket();
		so.connect(host, port);
		System.err.println("connection from "
				+ InetAddress.getLocalHost().getHostAddress()
				+ ":" + so.getLocalPort());

		so.setReceiveBufferSize(1 << 20); // 1MB
		System.err.println("receive buffer size " + so.getReceiveBufferSize());

		Thread heartBeat = new Thread(new UDPClient());
		heartBeat.start();

		Frame frame = new Frame(host.getHostAddress() + ":" + port);
		Canvas_ canvas = new Canvas_();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		double s = 0.5;
		int w = (int) (d.width * s), h = (int) (d.height * s);
		canvas.setSize(w, h);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		byte[] buf = new byte[1 << 16]; // maximum UDP packet size
		while (true) {
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			so.receive(p);
			canvas.image = readImage(p.getData(), p.getOffset(), p.getLength());
			canvas.repaint();
			Thread.sleep(90);
		}
	}
}
