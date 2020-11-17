import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ScreenServer {
    static ArrayList<Connection> activeConnections;
    static DatagramSocket socket;
    static Robot robot;
    static Rectangle screenSize;

    public static class Connection{
        InetAddress address;
        int port;
        long nextTimeout;

        public Connection(String a, int p) throws UnknownHostException {
            address = InetAddress.getByName(a);
            port = p;
            nextTimeout = System.currentTimeMillis() + 10000;
        }

        public void receivedHeartbeat(){
            nextTimeout = System.currentTimeMillis() + 10000;
        }
    }

    public static byte[] getBytesToSend(){
        //TODO
        BufferedImage image = robot.createScreenCapture(screenSize);
        return new byte[1024];
    }

    public static class Sender extends Thread{
        @Override
        public void run(){
            byte[] bytesToSend = getBytesToSend();
           synchronized (activeConnections){
               for(Connection c : activeConnections){
                   if(c.nextTimeout < System.currentTimeMillis()){
                       activeConnections.remove(c);
                       continue;
                   }
                   DatagramPacket response = new DatagramPacket(bytesToSend, bytesToSend.length, c.address, c.port);
                   try {
                       socket.send(response);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
        }
    }


    public static void main(String[] args) throws IOException, AWTException {

        System.out.println("Starting Server...");
        socket = new DatagramSocket(1024);
        robot = new Robot();
        DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize = new Rectangle(0,0, screenDimension.width, screenDimension.height);
        activeConnections = new ArrayList<>();
        new Sender().start();
        System.out.println("Started on port 1024");

        while(true){
            socket.receive(request);
            processHeartbeat(request.getAddress().getHostAddress(), request.getPort());
            System.out.println("Received heartbeat from " + request.getAddress().getHostAddress() + ":" + request.getPort());
        }
    }

    public static void processHeartbeat(String ip, int port) throws UnknownHostException {
        boolean found = false;
        synchronized (activeConnections) {
            for (Connection c : activeConnections) {
                if (c.address.getHostAddress().compareTo(ip) == 0 && c.port == port) {
                    c.receivedHeartbeat();
                    found = true;
                    break;
                }
            }
            if (!found) {
                activeConnections.add(new Connection(ip, port));
            }
        }
    }
}
