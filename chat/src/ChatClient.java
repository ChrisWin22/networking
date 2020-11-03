import java.io.*;
import java.net.Socket;

public class ChatClient implements Runnable{

    InputStream inputStream;

    public ChatClient(InputStream in){
        inputStream = in;
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1",7);
        System.out.println("Connected to: " + socket.getInetAddress());

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        PrintWriter printWriter = new PrintWriter(outputStream, true);

        Thread thread = new Thread(new ChatClient(inputStream));
        thread.start();

        String line = "";
        while((line = bufferedReader.readLine()) != null){
            printWriter.println(line);
        }
    }


    @Override
    public void run() {
        while(true) {
            try {
                int received = inputStream.read();
                if(received == -1) {
                    System.out.println("End of Input");
                    throw new IOException();
                }
                if (32 <= received && received <= 126 || received == 10 || received == 27) {
                    System.out.print((char) received);
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
