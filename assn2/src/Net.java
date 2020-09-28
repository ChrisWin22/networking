import java.net.*;
import java.util.*;

public class Net {

    public static void main(String[] args) {

        switch (args[0]){
            case "ifconfig": Ifconfig(); break;
            case "ping": Ping(args[1]); break;
            case "nslookup": Nslookup(args[1]); break;
            default: defaultInput();
        }

    }

    public static void defaultInput(){
        System.out.println("Enter one of the following:");
        System.out.println("1 - ifconfig");
        System.out.println("2 - ping <ip address/website name>");
        System.out.println("3 - nslookup <ip address/website name>");
    }

    public static void Ping(String site){
        try {
            double startTime = System.currentTimeMillis();
            InetAddress siteIP = InetAddress.getByName(site);
            String hostName = siteIP.getHostName();
            double totalTime = System.currentTimeMillis() - startTime;
            System.out.println("Ping " + hostName + "/" + siteIP.getHostAddress() + " took " + totalTime + "ms");
        }catch(UnknownHostException e){
            System.out.println("Couldn't find host " + site);
        }

    }

    public static void Ifconfig(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            ArrayList<NetworkInterface> interfacesList = Collections.list(interfaces);

            for(NetworkInterface itfc : interfacesList){
                System.out.println(itfc.getDisplayName());

                for(InetAddress address : Collections.list(itfc.getInetAddresses())){
                    System.out.println(address.getHostAddress());
                }
                System.out.println();
            }

        }catch(SocketException e){

        }
    }

    public static void Nslookup(String site){
        try{
            InetAddress[] ips = InetAddress.getAllByName(site);
            for(InetAddress ip : ips){
                System.out.println("Hostname: " + ip.getCanonicalHostName());
                System.out.println("Address: " + ip.getHostAddress());
            }
        }catch (UnknownHostException e){
            System.out.println("Could not find host: " + site);
        }
    }

}
