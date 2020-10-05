import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;


public class DictClient {


    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("dict.org", 2628);
        socket.setSoTimeout(10000);

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        reader.readLine();

        for(int i = 0; i < args.length; i++){
            writer.write("DEFINE ! " + args[i] + "\r\n");
            writer.flush();

            String line = reader.readLine();
            if(!line.startsWith("552")){
                while((line = reader.readLine()) != null){
                    if(line.startsWith("250")){
                        break;
                    }
                    System.out.println(line);
                }
            }
            else{
                System.out.println("Couldn't find the word \"" + args[i] + "\"");
            }
            System.out.println();
            System.out.println();
        }
        writer.write("quit\r\n");
        writer.flush();



    }

}
