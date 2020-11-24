import com.sun.java.accessibility.util.Translator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class TranslateServer {

    static ArrayList<Sender> connections;
    static Queue<Message> messages;
    static int usernameValue;

    public static class Message{
        public String username;
        public Date date;
        public Language ogLanguage;
        public byte[] message;

        public Message(String u, Language lan, byte[] m){
            username = u;
            message = m;
            ogLanguage = lan;
            date = new Date();
        }
    }

    public enum Language {
        ENGLISH,
        SPANISH,
        FRENCH,
        GERMAN,
        CHINESE,
        GREEK,
    }

    public static int langToInt(Language lang){
        switch (lang){
            case ENGLISH: return 0;
            case SPANISH: return 1;
            case FRENCH: return 2;
            case GERMAN: return 3;
            case CHINESE: return 4;
            case GREEK: return 5;
            default: return 0;
        }
    }

    static class Sender extends Thread{
        Queue<byte[]> toSend;
        Socket socket;
        Receiver receiver;

        public Sender(Socket so, Receiver r){
            socket = so;
            receiver = r;
            toSend = new LinkedList<>();
        }

        @Override
        public void run(){
            while(true) {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    while (toSend.isEmpty()) {
                        Thread.sleep(10);
                    }
                    byte[] sendingMessage = toSend.remove();
                    System.out.println("sent to " + this.receiver.username + ", contains: " + sendingMessage.length);
                    outputStream.write(sendingMessage);
                    outputStream.flush();
                }
                catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    static class Receiver extends Thread{
        InputStream inputStream;
        Socket socket;
        Language lang;
        String username;

        public Receiver(Socket so){
            socket = so;
            try {
                inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                int lan = dataInputStream.readInt();
                lang = Language.values()[lan];
                int usernameLength = dataInputStream.readInt();
                username = new String(dataInputStream.readNBytes(usernameLength));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            try {
                while (true) {
                    while (inputStream.available() == 0) {
                        Thread.sleep(10);
                    }
                    byte[] data = inputStream.readNBytes(inputStream.available());
                    System.out.println("Received message");
                    messages.add(new Message(username, lang, data));
                }
            }
            catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static class queueAndTranslate extends Thread{
        @Override
        public void run(){
            while(true){
                try {
                    while (messages.isEmpty()) {
                        Thread.sleep(10);
                    }
                    Message message = messages.remove();

                    for(Sender c : connections){
                        if(c.receiver.username.compareTo(message.username) != 0){
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                            byte[] usernameBytes = message.username.getBytes();
                            dataOutputStream.writeInt(usernameBytes.length);
                            dataOutputStream.write(usernameBytes);

                            dataOutputStream.writeLong(message.date.getTime());

                            dataOutputStream.writeInt(langToInt(message.ogLanguage));

                            byte[] newMessage = translateText(new String(message.message), message.ogLanguage, c.receiver.lang).getBytes();
                            dataOutputStream.writeInt(newMessage.length);
                            dataOutputStream.write(newMessage);
                            dataOutputStream.flush();
                            byte[] sendingMessage = byteArrayOutputStream.toByteArray();

                            c.toSend.add(sendingMessage);
                            System.out.println("Added to " + c.receiver.username);
                        }
                    }
                }
                catch (InterruptedException | IOException e){

                }
            }
        }
    }

    public static String translateText(String text, Language og, Language to) throws IOException, InterruptedException {
        String beginText = "translation";
        String endText = "pronunciation";
        String newText = text.replace(" ", "%20").replace("\n", "");
        String baseUrl = "https://google-translate20.p.rapidapi.com/translate?text=";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + newText + "&sl=" + LanguageToString(og) + "&tl=" + LanguageToString(to)))
                .header("x-rapidapi-key", "4e957fe9afmsh33d9d283f921b3ep177683jsn96a1ecdd8e52")
                .header("x-rapidapi-host", "google-translate20.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        int beginIndex = body.indexOf(beginText) + 3 + beginText.length();
        int endIndex = body.indexOf(endText) - 3;
        String toReturn = body.substring(beginIndex, endIndex);
        return toReturn;
//        return text.replace("\n", "");
    }

    public static void main(String[] args) throws IOException {

        int port = 1024;

        System.out.println("Starting server...");

        ServerSocket serverSocket = new ServerSocket(port);
        messages = new LinkedList<>();
        connections = new ArrayList<>();
        usernameValue = 1;
        new queueAndTranslate().start();

        System.out.println("Server started");


        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Incoming connection...");

            Receiver receiver = new Receiver(socket);
            Sender sender = new Sender(socket, receiver);

            sender.start();
            receiver.start();

            connections.add(sender);

            System.out.println("Connected");
        }
    }

    public static String LanguageToString(Language i){
        switch (i){
            case ENGLISH: return "en";
            case SPANISH: return "es";
            case FRENCH: return "fr";
            case GERMAN: return "de";
            case CHINESE: return "zh-TW";
            case GREEK: return "el";
            default: return "en";
        }
    }
}
