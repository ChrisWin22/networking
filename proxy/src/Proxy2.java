import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.*;
import java.io.*;

public class Proxy2 implements Runnable{

    String site;
    Socket socket;
    public Proxy2(Socket socket, String site){
        this.socket = socket;
        this.site = site;
    }

    @Override
    public void run() {
        try {
            System.out.println("Starting connection to " + socket.getInetAddress() + ":" + socket.getPort());
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

        int MAXNUMTHREADS = 5;
        int port = Integer.parseInt(args[0]);
        String site = args[1];

//        int port = 8080;
//        String site = "http://bing.com";

        System.out.println("Starting proxy server for " + site + "...");
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started");

        ExecutorService pool = Executors.newFixedThreadPool(MAXNUMTHREADS);

        while(true){
            try {
                Socket socket = serverSocket.accept();
                pool.execute(new Proxy2(socket, site));
            }
            catch(Exception e){
                System.out.println("shutting down pool...");
                pool.shutdown();
            }
        }

    }
}
