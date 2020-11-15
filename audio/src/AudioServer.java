import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioServer {
    static byte[] formatHeader;
    static byte[] songBytes;


    public static class Connection extends Thread {
        Socket socket;
        public Connection(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(formatHeader);
                outputStream.flush();
                outputStream.write(songBytes);
                outputStream.flush();
                System.out.println("Sent bytes to: " + socket.getInetAddress() + ":" + socket.getPort());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static int getEncoding(AudioFormat.Encoding encoding){
        if (AudioFormat.Encoding.ALAW.equals(encoding)) {
            return 1;
        } else if (AudioFormat.Encoding.PCM_FLOAT.equals(encoding)) {
            return 2;
        } else if (AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) {
            return 3;
        } else if (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) {
            return 4;
        } else if (AudioFormat.Encoding.ULAW.equals(encoding)) {
            return 5;
        }
        return -1;
    }

    public static byte[] makeHeader(AudioFormat format) throws IOException {
        System.out.println("Compiling header...");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(getEncoding(format.getEncoding()));
        dataOutputStream.writeFloat(format.getSampleRate());
        dataOutputStream.writeInt(format.getSampleSizeInBits());
        dataOutputStream.writeInt(format.getChannels());
        dataOutputStream.writeInt(format.getFrameSize());
        dataOutputStream.writeFloat(format.getFrameRate());
        dataOutputStream.writeBoolean(format.isBigEndian());
        dataOutputStream.close();
        System.out.println("Header Compiled");
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
//        String filePath = args[1];
//        int port = Integer.parseInt(args[0]);
        System.out.println("Starting server...");

        String filePath = "src\\AudioFiles\\a.wav";
        int port = 27;

        File file = new File(filePath);

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        formatHeader = makeHeader(format);
        songBytes = audioInputStream.readAllBytes();
        System.out.println("Header bytes: " + formatHeader.length);
        System.out.println("Song bytes: " + songBytes.length);

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Connection received from: " + socket.getInetAddress() + ":" + socket.getPort());

            new Thread(new Connection(socket)).start();
        }
    }
}
