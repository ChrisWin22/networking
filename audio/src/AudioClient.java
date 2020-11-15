import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class AudioClient {

    public static AudioFormat.Encoding getEncoding(int e){
        return switch (e) {
            case 1 -> AudioFormat.Encoding.ALAW;
            case 2 -> AudioFormat.Encoding.PCM_FLOAT;
            case 3 -> AudioFormat.Encoding.PCM_SIGNED;
            case 4 -> AudioFormat.Encoding.PCM_UNSIGNED;
            case 5 -> AudioFormat.Encoding.ULAW;
            default -> null;
        };
    }

    public static AudioFormat getFormat(InputStream inputStream) throws IOException {
        System.out.println("Decoding header...");
        System.out.println(inputStream.available());
        byte[] header = inputStream.readNBytes(25);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(header);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        AudioFormat.Encoding encoding = getEncoding(dataInputStream.readInt());
        float sampleRate = dataInputStream.readFloat();
        int sampleSizeInBits = dataInputStream.readInt();
        int channels = dataInputStream.readInt();
        int frameSize = dataInputStream.readInt();
        float frameRate = dataInputStream.readFloat();
        boolean isBigEndian = dataInputStream.readBoolean();
        dataInputStream.close();
        System.out.println("Header decoded");
        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, isBigEndian);
    }

    public static void main(String[] args) throws IOException, LineUnavailableException, InterruptedException {
//        String host = args[0];
//        int port = Integer.parseInt(args[1]);

        String host = "127.0.0.1";
        int port = 27;

        Socket socket = new Socket(host, port);
        System.out.println("Connected to host");

        InputStream inputStream = socket.getInputStream();

        AudioFormat format = getFormat(inputStream);

        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format);
        sourceDataLine.open(format);
        sourceDataLine.start();

        byte[] buffer = new byte[1024];
        int numBytesRead;
        while((numBytesRead = inputStream.read(buffer)) != -1){
            sourceDataLine.write(buffer, 0, numBytesRead);
        }
    }
}

