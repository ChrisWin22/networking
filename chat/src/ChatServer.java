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

                    //add bytes to message queues
                    for(MessageSender thread : threads) {
                        if(this.socket != thread.socket)
                            thread.messageQueue.add(bytesFromClient);
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
                    while (messageQueue.peek() != null) {
                        outputStream.write(messageQueue.remove());
                        outputStream.flush();
                    }
                }
            } catch (IOException e) {
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
//            Thread receiver = new Thread(new messageReciever(newClientConnection));
//            Thread sender = new Thread(new messageSender(newClientConnection));
            MessageSender sender = new MessageSender(newClientConnection);
            MessageReceiver receiver = new MessageReceiver(newClientConnection);

            receiver.start();
            sender.start();
            threads.add(sender);
        }
    }
}

