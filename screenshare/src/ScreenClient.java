import java.io.IOException;
import java.net.*;

public class ScreenClient {


    public static void main(String[] args) throws IOException {
        System.out.println("Starting Client...");
//        InetAddress host = InetAddress.getByName(args[0);
//        int port = Integer.parseInt(args[1]);

        InetAddress host = InetAddress.getByName("127.0.0.1");
        int port = 27;
        DatagramSocket socket = new DatagramSocket();
//        socket.setSoTimeout(10000);
        socket.connect(host, port);
        DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
        System.out.println("Client connected to: " + host.getHostAddress() + ":" + port);


        while (true){
            socket.receive(response);
            System.out.println(new String(response.getData()));
        }

    }
}
