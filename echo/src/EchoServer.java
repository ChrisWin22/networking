import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class EchoServer implements Runnable{

    Socket socket;

    public EchoServer(Socket so){
        socket = so;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Server");
        ServerSocket serverSocket = new ServerSocket(7);
        System.out.println("Server Started");

        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Connected to: " + socket.getInetAddress());

            Thread thread = new Thread(new EchoServer(socket));
            thread.start();
        }

    }

    @Override
    public void run() {
        try {
            System.out.println("Starting new thread");
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            while(true) {
                while(inputStream.available() == 0){
                    Thread.sleep(10);
                }

                byte[] bytes = inputStream.readNBytes(inputStream.available());
                outputStream.write(bytes);
                outputStream.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }
}
