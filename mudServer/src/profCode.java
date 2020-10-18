import java.io.*;import java.net.*;

public class profCode implements Runnable{
    InputStream in;
    profCode(InputStream in) {
        this.in = in;
    }
    static final int LF   = 10;   // \n line feed (newline)
    static final int CR   = 13;   // \r carriage return
    static final int ESC  = 27;   // en.wikipedia.org/wiki/ANSI_escape_codestatic
    final int SE   = 240;
    static final int GA   = 249;  // go ahead
    static final int SB   = 250;
    static final int WILL = 251;
    static final int WONT = 252;
    static final int DO   = 253;
    static final int DONT = 254;
    static final int IAC  = 255;

    byte[] buf = new byte[4096];
    int i_, numberOfReturnedBytes;
    int getc() throws IOException {
        if (i_ == numberOfReturnedBytes) {
            if ((numberOfReturnedBytes = in.read(buf)) == -1)     // end of input
                throw new IOException();
            i_ = 0;
        }
        byte a = buf[i_++];
        return a < 0 ? a + 256 : a;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int a = getc();
                if (32 <= a && a <= 126 || a == LF || a == ESC) {
                    System.out.print((char) a);
                    continue;
                }
                if (a == WILL || a == WONT || a == DO || a == DONT || a == SB) {
                    int b = getc();
                    if (a == WILL)
                        System.err.println("WILL " + b);
                    else if (a == WONT)
                        System.err.println("WONT " + b);
                    else if (a == DO)
                        System.err.println("DO " + b);
                    else if (a == DONT)
                        System.err.println("DONT " + b);
                    else {System.err.print("SB ");
                    while (b != SE) {
                        System.err.print(b + " ");
                        b = getc();}System.err.println("SE");
                    }
                }
                else if (a == IAC || a == GA || a == CR) {}  // ignore
                else      // unknown
                    System.err.print("\n\n====== " + a + " ======\n\n");
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("  usage: java MudClient host port");
            System.err.println("example: java MudClient achaea.com 23");
            System.err.println("example: java MudClient aardmud.org 4000");
            System.err.println("example: java MudClient carrionfields.net 4449");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        Socket so = new Socket(host, port);
        System.err.println("connection from " + so.getLocalSocketAddress()+ " to " + so.getRemoteSocketAddress());
        InputStream in = so.getInputStream();
        OutputStream out = so.getOutputStream();
        Thread t = new Thread(new profCode(in));
        t.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(out, true);
        String s;
        while ((s = br.readLine()) != null)
            pw.println(s);
    }
}
