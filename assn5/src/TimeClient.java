import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("time.nist.gov", 37);
        socket.setSoTimeout(10000);

        InputStream in = socket.getInputStream();
        byte[] bytes = in.readAllBytes();

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
