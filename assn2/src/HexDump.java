import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HexDump {


    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            printHelp();
        }
        switch (args[0]){
            case "-h": printHelp(); break;
            case "-C":
            case "--canonical": canonical(args[1]); break;
            default: canonical(args[0]); break;
        }
    }


    public static void printHelp(){
        System.out.println("Expected format: hexdump <options> <file>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("-h: Opens this menu");
        System.out.println("--canonical, -C: Canonical hex+AXCII display");
        System.out.println();
    }

    public static void canonical(String file) throws IOException {
        byte[] readInBytes = new byte[16];
        FileInputStream fileInputStream = new FileInputStream(new File(file));
        int currentLine = 0;
        while(fileInputStream.read(readInBytes, 0, 16) != -1){
            printToScreen(readInBytes, 16, currentLine);
            currentLine += 16;
            readInBytes = new byte[16];
        }

    }

    public static void printToScreen(byte[] bytes, int bytesPerLine, int currentLine){
        StringBuilder lineIndex = new StringBuilder(Integer.toHexString(currentLine) + "  ");
        while(lineIndex.length() < 10){
            lineIndex.insert(0, "0");
        }
        System.out.print(lineIndex);

        int spotInLine = 0;
        for(; spotInLine < bytesPerLine/2; spotInLine++){
            String inHex = Integer.toHexString(bytes[spotInLine]);
            if(inHex.length() < 2){
                inHex = "0" + inHex;
            }
            System.out.print(inHex + " ");
        }
        System.out.print(" ");
        for(; spotInLine < bytesPerLine; spotInLine++){
            String inHex = Integer.toHexString(bytes[spotInLine]);
            if(inHex.length() < 2){
                inHex = "0" + inHex;
            }
            System.out.print(inHex + " ");
        }
        System.out.println(" |" + new String(bytes) + "|");
    }
}


