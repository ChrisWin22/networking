package translate;

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
    }

    public static int langToInt(Language lang){
        switch (lang){
            case ENGLISH: return 0;
            case SPANISH: return 1;
            case FRENCH: return 2;
            default: return 0;
        }
    }

//    public class MyCustomConnection{
//        Socket socket;
//        Language lang;
//        String username;
//        Sender sender;
//        Receiver receiver;
//        InputStream inputStream;
//        OutputStream outputStream;
//
//        public MyCustomConnection(Socket so) throws IOException {
//            socket = so;
//            sender = new Sender();
//            receiver = new Receiver();
//            inputStream = socket.getInputStream();
//            outputStream = socket.getOutputStream();
//            int lan = inputStream.read();
//            lang = Language.values()[lan];
//            username = "Testing" + usernameValue;
//            usernameValue++;
//
//
//            sender.start();
//            receiver.start();
//
//        }
//
//        public class Sender extends Thread{
//            Queue<byte[]> toSend;
//
//            public Sender(){
//                toSend = new LinkedList<>();
//            }
//
//            @Override
//            public void run(){
//                while(true) {
//                    try {
//                        while (toSend.isEmpty()) {
//                            Thread.sleep(10);
//                        }
//                        outputStream.write(toSend.remove());
//                        outputStream.flush();
//                    }
//                    catch (InterruptedException | IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }
//
//        public class Receiver extends Thread{
//            public Receiver(){}
//
//            @Override
//            public void run(){
//                while(true){
//                    try {
//                        while(inputStream.available() == 0){
//                            Thread.sleep(10);
//                        }
//                        byte[] data = inputStream.readNBytes(inputStream.available());
//                        System.out.println("Received message");
//                        messages.add(new Message(username, lang, data));
//                    }
//                    catch (InterruptedException | IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }
//
//        public String getUsername(){
//            return username;
//        }
//
//    }

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
                    outputStream.write(toSend.remove());
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
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                    Message message = messages.remove();


                    for(Sender c : connections){
                        if(c.receiver.username.compareTo(message.username) != 0){
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
    }

    public static void main(String[] args) throws IOException {
        int port = 1024;

        ServerSocket serverSocket = new ServerSocket(port);
        messages = new LinkedList<>();
        connections = new ArrayList<>();
        usernameValue = 1;
        new queueAndTranslate().start();


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
            default: return "en";
        }
    }
}
