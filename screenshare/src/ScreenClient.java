import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ScreenClient {
    static DatagramSocket socket;
    static Frame frame;
    static Canvas canvas;

    public static class keepAlive extends Thread{
        DatagramPacket heartBeat;

        public keepAlive(){
            byte[] words = "alive".getBytes();
            heartBeat = new DatagramPacket(words, words.length);
        }

        @Override
        public void run(){
            try {
                while(true) {
                    socket.send(heartBeat);
                    System.out.println("Sent heartbeat");
                    Thread.sleep(2000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static BufferedImage getImage(byte[] bytes, int offset, int length) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes, offset, length);
        BufferedImage image = ImageIO.read(byteArrayInputStream);
        byteArrayInputStream.close();
        return image;
    }

//    public static BufferedImage scale(BufferedImage image){
//        int canvasWidth = canvas.getWidth();
//        int canvasHeight = canvas.getHeight();
//
//        int imageWidth = image.getWidth();
//        int imageHeight = image.getHeight();
//
//
//    }


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Client...");
//        InetAddress host = InetAddress.getByName(args[0);
//        int port = Integer.parseInt(args[1]);

        InetAddress host = InetAddress.getByName("127.0.0.1");
        int port = 1024;
        socket = new DatagramSocket();
//        socket.setSoTimeout(10000);
        socket.connect(host, port);
        new keepAlive().start();
        System.out.println("Client connected to: " + host.getHostAddress() + ":" + port);

        frame = new Frame("Screenshare");
        canvas = new Canvas();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        canvas.setSize(dimension.width/2, dimension.height/2);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);


        byte[] buf = new byte[1 << 16];
        while (true){
            DatagramPacket response = new DatagramPacket(buf, buf.length);
            socket.receive(response);
            BufferedImage receivedImage = getImage(response.getData(), response.getOffset(), response.getLength());
//            BufferedImage scaledImage = scale(receivedImage);
            canvas.paint(receivedImage.createGraphics());
//            canvas.update(receivedImage.createGraphics());
            Thread.sleep(90);
        }

    }
}
