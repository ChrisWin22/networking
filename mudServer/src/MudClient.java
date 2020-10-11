import java.io.*;
import java.net.Socket;
import java.util.logging.SocketHandler;

public class MudClient implements Runnable{
    InputStream inputStream;
    MudClient(InputStream in){
        inputStream = in;
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("achaea.com", 23);

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        Thread thread = new Thread(new MudClient(in));
        thread.start();

        String line = "";
        while((line = reader.readLine()) != null){
            System.out.println(line);
        }

    }
}
