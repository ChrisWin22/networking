import java.io.IOException;
import java.net.*;

public class Curl {

    public static void main(String[] args) throws IOException {
        String testString = "https://www.google.com";
        new URL(testString).openStream().transferTo(System.out);
    }
}
