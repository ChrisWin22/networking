import java.io.*;
import java.net.*;
import java.util.*;

public class Proxy1_TeachersCode implements Runnable {
	Socket so;
	Proxy1_TeachersCode(Socket so) {
		this.so = so;
	}

	@Override
	public void run() {
		try {
			InputStream in = so.getInputStream();
			OutputStream out = so.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			PrintWriter pw = new PrintWriter(out);
			StringTokenizer st = new StringTokenizer(br.readLine());
			String method = st.nextToken().toUpperCase();
			if (method.equals("GET")) {
				String s = site + st.nextToken();
				System.err.println("GET " + s);
				byte[] content = new URL(s).openStream().readAllBytes();
				pw.println("HTTP/1.1 200 OK");
				pw.println("Server: Proxy1");
				pw.println("Date: " + new Date());
				pw.println("Content-type: text/html");
				pw.println("Content-length: " + content.length);
				pw.println();
				pw.flush();
				out.write(content);
				out.flush();
			}
		} catch (IOException e) {
			System.err.println(so + " " + e);
		}
		try {
			so.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	static String site;

	public static void main(String[] args) throws IOException {
//		if (args.length != 2) {
//			System.err.println("  usage: java Proxy1 port site");
//			System.err.println("example: java Proxy1 8080 http://bing.com");
//			return;
//		}

		int port = 8080;
		site = "http://bing.com";
		System.err.println(port + " " + site);

		ServerSocket sso = new ServerSocket(port);
		while (true) {
			Socket so = sso.accept();
			System.err.println("connection from " + so.getRemoteSocketAddress()
					+ " to " + so.getLocalSocketAddress());

			new Thread(new Proxy1_TeachersCode(so)).start();
		}
	}
}
