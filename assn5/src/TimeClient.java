import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

public class TimeClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 37);
        socket.setSoTimeout(10000);

        InputStream in = socket.getInputStream();
        byte[] bytes = in.readAllBytes();
        System.out.println("NumBytes: " + bytes.length);

        long serverTimeInMilli = 0;
        for (int i = 0; i < bytes.length; i++) {
            serverTimeInMilli <<= 8;
            serverTimeInMilli |= (bytes[i] & 0x00FF);
        }
        serverTimeInMilli *= 1000;

        Date date = new Date(serverTimeInMilli);

        System.out.println(date);
    }
}
