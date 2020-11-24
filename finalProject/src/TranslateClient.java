import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class TranslateClient {

    public static class Receiver extends Thread{
        Socket socket;
        InputStream inputStream;

        public Receiver(Socket so) throws IOException {
            socket = so;
            inputStream = so.getInputStream();
        }

        @Override
        public void run(){
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            while(true){
                try {
                    while (dataInputStream.available() == 0){
                        Thread.sleep(10);
                    }
                    int usernameLength = dataInputStream.readInt();
                    String username = new String(dataInputStream.readNBytes(usernameLength));
                    Date date = new Date(dataInputStream.readLong());
                    String language = intToLanguage(dataInputStream.readInt());
                    int messageLength = dataInputStream.readInt();
                    String message = new String(dataInputStream.readNBytes(messageLength));

                    System.out.println("[" + date.toString() + "] From: " + username + "[" + language + "] -- " + message);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        String host;

        if(args.length != 0){
            host = args[0];
        }
        else
            host = "127.0.0.1";

        int port = 1024;

        int chosenLanguage = choseLanguage();
        String username = choseUsername();
        Socket socket = new Socket(host, port);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] usernameBytes = username.getBytes();

        dataOutputStream.writeInt(chosenLanguage);
        dataOutputStream.writeInt(usernameBytes.length);
        dataOutputStream.write(username.getBytes());
        dataOutputStream.flush();
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);


        outputStream.write(byteArrayOutputStream.toByteArray());
        new Receiver(socket).start();

        System.out.println("You are connected! Type a message to begin chatting.");

        String fromConsole = "";
        while((fromConsole = bufferedReader.readLine()) != null){
            printWriter.println(fromConsole);
        }
    }

    public static String choseUsername(){
        System.out.print("Choose a username: ");
        Scanner scanner = new Scanner(System.in);
        String user = scanner.nextLine();
        return user;
    }

    public static void printLanguage(){
        System.out.println("Languages:");
        System.out.println("1. English");
        System.out.println("2. Spanish");
        System.out.println("3. French");
        System.out.println("4. German");
        System.out.println("5. Chinese");
        System.out.println("6. Greek");
    }

    public static int choseLanguage(){
        printLanguage();
        System.out.print("Choose your language: ");
        Scanner scanner = new Scanner(System.in);
        int selected = scanner.nextInt();
        if(selected <= 6){
            return selected - 1;
        }
        return 0;
    }

    public static String intToLanguage(int i){
        switch (i){
            case 0: return "EN";
            case 1: return "ES";
            case 2: return "FR";
            case 3: return "DE";
            case 4: return "ZH";
            case 5: return "EL";
            default: return "EN";
        }
    }
}
