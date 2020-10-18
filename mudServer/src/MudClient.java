import java.io.*;
import java.net.Socket;

public class MudClient implements Runnable{
    InputStream inputStream;
//    byte[] buf = new byte[4096];
//    int numberOfBytesRead;
//    int spotInBufArray;
    MudClient(InputStream in){
        inputStream = in;
    }

    public int getCharacterNoBuffer() throws IOException{
        int received = inputStream.read();
        if(received == -1) {
            System.out.println("End of Input");
            throw new IOException();
        }
        return received;
    }
//
//    public int getCharacter() throws IOException{
//        if(numberOfBytesRead == spotInBufArray) {
//            if ((numberOfBytesRead = inputStream.read(buf)) == -1) {
//                System.out.println("End of Input");
//                throw new IOException();
//            }
//            spotInBufArray = 0;
//        }
//        int toReturnInt = buf[spotInBufArray];
//        spotInBufArray++;
//        if(toReturnInt < 0)
//            toReturnInt += 256;
//        return toReturnInt;
//    }

    @Override
    public void run() {
        try {
            while (true) {
                int returnedChar = getCharacterNoBuffer();
                if (32 <= returnedChar && returnedChar <= 126 || returnedChar == 10 || returnedChar == 27) {
                    System.out.print((char) returnedChar);
                    continue;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter printWriter = new PrintWriter(out, true);

        Thread thread = new Thread(new MudClient(in));
        thread.start();

        String line = "";
        while((line = reader.readLine()) != null){
            printWriter.println(line);
        }

    }
}
