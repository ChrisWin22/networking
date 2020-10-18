import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class WebServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Starting Server");
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.println("Server Started");

        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Connection to " + socket.getInetAddress() + ":" + socket.getPort());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            byte[] requestBytes = inputStream.readNBytes(inputStream.available());

            printWriter.println("HTTP/1.1 403 FORBIDDEN");
            printWriter.println("Date: " + new Date());
            printWriter.println("Content-type: text/html");
            printWriter.println("Content-length: " + requestBytes.length);
            printWriter.println();	// IMPORTANT: DON'T OMIT THIS EMPTY LINE!!!
            printWriter.flush();

            outputStream.write(requestBytes);
            outputStream.flush();


        }

    }



}
