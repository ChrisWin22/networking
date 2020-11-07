import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ChatServer{
    static ArrayList<MessageSender> threads;

    static class MessageReceiver extends Thread {

        Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();

                while (true) {
                    while (inputStream.available() == 0) {
                        Thread.sleep(10);
                    }
                        //Read bytes from client
                    byte[] bytesFromClient = inputStream.readNBytes(inputStream.available());
                    System.out.println("Received new message from: " + this.socket.getInetAddress() + ":" + this.socket.getPort());

                    //add bytes to message queues
                    for(MessageSender thread : threads) {
                        if(this.socket != thread.socket) {
                            thread.messageQueue.add(bytesFromClient);
                            System.out.println("Added message to " + thread.socket.getInetAddress() + ":" + thread.socket.getPort() + " queue");
                        }
                    }
                }
            }
            catch (InterruptedException | IOException interruptedException) {
                interruptedException.printStackTrace();
            }


        }
    }


    static class MessageSender extends Thread {
        public Queue<byte[]> messageQueue;
        Socket socket;

        public MessageSender(Socket socket) {
            this.socket = socket;
            messageQueue = new LinkedList<>();
        }

        @Override
        public void run() {
            try {
                OutputStream outputStream = socket.getOutputStream();
                while (true) {
                        while (messageQueue.isEmpty()) {
                            Thread.sleep(10);
                        }
                        System.out.println("Sent message to " + this.socket.getInetAddress() + ":" + this.socket.getPort());
                        outputStream.write(messageQueue.remove());
                        outputStream.flush();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


//    static class QueueHandler implements Runnable{
//
//        @Override
//        public void run() {
//            while(true) {
//                while (!messageQueue.isEmpty()) {
//                    byte[] message = messageQueue.remove();
//
//                }
//            }
//        }
//    }

    public static void main(String[] args) throws IOException {
//        int port = Integer.parseInt(args[0]);

        int port = 7;

        System.out.println("Starting chat server on port " + port + "...");
        ServerSocket serverSocket = new ServerSocket(port);
        threads = new ArrayList<>();
//        messageQueue = new SynchronousQueue<>();
        System.out.println("Server started");

        while (true){
            Socket newClientConnection = serverSocket.accept();
            System.out.println("Incoming connection...");
//            Thread receiver = new Thread(new messageReciever(newClientConnection));
//            Thread sender = new Thread(new messageSender(newClientConnection));
            MessageSender sender = new MessageSender(newClientConnection);
            MessageReceiver receiver = new MessageReceiver(newClientConnection);

            receiver.start();
            sender.start();
            threads.add(sender);
            System.out.println("Connected to " + newClientConnection.getInetAddress() + ":" + newClientConnection.getPort());
        }
    }
}

