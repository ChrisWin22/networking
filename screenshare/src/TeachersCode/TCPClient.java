package TeachersCode;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

public class TCPClient {

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

	static BufferedImage readImage(InputStream in) throws IOException {
		int n = 0;
		for (int i = 0; i < 4; i++)
			n = n << 8 | in.read();
		byte[] aa = in.readNBytes(n);
		ByteArrayInputStream bis = new ByteArrayInputStream(aa, 0, n);
		BufferedImage img = ImageIO.read(bis);
		bis.close();
		return img;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: java TCPClient host port");
			return;
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);

		SocketAddress sa = new InetSocketAddress(host, port);
		Socket so = new Socket();
		so.setReceiveBufferSize(1 << 20); // 1MB
		so.connect(sa);
		System.err.println("connection from " + so.getLocalSocketAddress()
				+ " to " + so.getRemoteSocketAddress());
		System.err.println("receive buffer size " + so.getReceiveBufferSize());

		InputStream in = so.getInputStream();

		Frame frame = new Frame(host + ":" + port);
		Canvas_ canvas = new Canvas_();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		double s = 0.5;
		int w = (int) (d.width * s), h = (int) (d.height * s);
		canvas.setSize(w, h);
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		while (true) {
			canvas.image = readImage(in);
			canvas.repaint();
			Thread.sleep(90);
		}
	}
}
