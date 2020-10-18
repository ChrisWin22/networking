import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeServer {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Time Server on Port 37");

        long timeFrom1900To1970 = 2208988800L;

        ServerSocket serverSocket = new ServerSocket(37);
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar c = Calendar.getInstance(tz);
        c.set(1900, 0, 1, 0, 0, 0);
        long time_ = c.getTimeInMillis();
        System.out.println("Started");

        while(true){

            Socket socket = serverSocket.accept();
            socket.setSoTimeout(5000);
            System.out.println("Connect to " + socket.getInetAddress());

            long time = System.currentTimeMillis();

            Date date = new Date(time);
            System.out.println("Current Time is: " + date);

            OutputStream outputStream = socket.getOutputStream();

            byte[] buff = new byte[6];
            long s = (time - time_ + 500) / 1000;
            for (int i = 0; i < 4; i++)
                buff[i] = (byte) (s >> (3 - i) * 8 & 255);
            buff[4] = '\r';
            buff[5] = '\n';

            outputStream.write(buff);
            outputStream.flush();

            System.out.println("Reply Sent");
            System.out.println();
        }
    }
}
