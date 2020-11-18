import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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

    public static class Sender extends Thread{
        static AffineTransformOp affineTransformOp;

        public Sender(){
            affineTransformOp = new AffineTransformOp(AffineTransform.getScaleInstance(.3,.3), AffineTransformOp.TYPE_BICUBIC);
        }

        public static BufferedImage scale(BufferedImage image){
            int newWidth = (int) (image.getWidth() * .3);
            int newHeight = (int) (image.getHeight() * .3);
            BufferedImage newImage = new BufferedImage(newWidth, newHeight, image.getType());
            newImage = affineTransformOp.filter(image, newImage);
            return newImage;
        }

        public static byte[] getBytesToSend() throws IOException {
            BufferedImage image = robot.createScreenCapture(screenSize);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedImage scaledImage = scale(image);
            ImageIO.write(scaledImage, "gif", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        @Override
        public void run(){
            while(true) {
                byte[] bytesToSend = new byte[0];
                try {
                    bytesToSend = getBytesToSend();
                    for (Iterator<Connection> it = activeConnections.iterator(); it.hasNext();) {
                        Connection c = it.next();
                        if (c.nextTimeout < System.currentTimeMillis()) {
                            it.remove();
                            System.out.println("Removed " + c.address + ":" + c.port + " from active connections (timeout)");
                            continue;
                        }
                        DatagramPacket response = new DatagramPacket(bytesToSend, bytesToSend.length, c.address, c.port);
                        socket.send(response);
                        System.out.println("Sent Message");
                    }
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) throws IOException, AWTException {

        System.out.println("Starting Server...");
        socket = new DatagramSocket(1024);
        socket.setSendBufferSize(1024);
        robot = new Robot();
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize = new Rectangle(0,0, screenDimension.width, screenDimension.height);
        activeConnections = new ArrayList<>();
        new Sender().start();
        System.out.println("Started on port 1024");

        while(true){
            DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
            socket.receive(request);
            processHeartbeat(request.getAddress().getHostAddress(), request.getPort());
            System.out.println("Received heartbeat from " + request.getAddress().getHostAddress() + ":" + request.getPort());
        }
    }

    public static void processHeartbeat(String ip, int port) throws UnknownHostException {
        boolean found = false;
        for (Connection c : activeConnections) {
            if (c.address.getHostAddress().compareTo(ip) == 0 && c.port == port) {
                c.receivedHeartbeat();
                found = true;
                break;
            }
        }
        if (!found) {
            activeConnections.add(new Connection(ip, port));
            System.out.println("Added " + ip + ":" + port + " to active connections");
        }
    }
}
