import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.*;

public class Proxy1 implements Runnable{

    Socket socket;
    String site;
    public Proxy1(Socket socket, String site){
        this.socket = socket;
        this.site = site;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringTokenizer header = new StringTokenizer(bufferedReader.readLine());

            String requestType = header.nextToken();
            if(requestType.compareTo("GET") == 0) {
                String fullURL = site + header.nextToken();
                byte[] fromURL = new URL(fullURL).openStream().readAllBytes();

                printWriter.println("HTTP/1.1 200 OK");
                printWriter.println("Server: Proxy1");
                printWriter.println("Date: " + new Date());
                printWriter.println("Content-type: text/html");
                printWriter.println("Content-length: " + fromURL.length);
                printWriter.println();    // IMPORTANT: DON'T OMIT THIS EMPTY LINE!!!
                printWriter.flush();

                outputStream.write(fromURL);
                outputStream.flush();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String site = args[1];

//        int port = 8080;
//        String site = "http://bing.com";

        System.out.println("Starting proxy server for " + site + "...");
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket.getInetAddress() + ":" + socket.getPort());

            System.out.println("Creating new thread...");
            new Thread(new Proxy1(socket, site)).start();

        }
    }
}
