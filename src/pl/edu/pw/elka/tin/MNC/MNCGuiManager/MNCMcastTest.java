package pl.edu.pw.elka.tin.MNC.MNCGuiManager;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.TreeSet;

/**
 * Tester przesyłania datagramów multicast.
 * @author Maciek
 */
public class MNCMcastTest implements Runnable{
    private DatagramSocket udpClient;
    private String name;
    private boolean running = true;

    public MNCMcastTest(String name){
        this.name = name;
        new Thread(new McastReceiver()).start();
        try {
            udpClient = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            NetworkInterface netint = NetworkInterface.getByName(MNCConsts.DEFAULT_INTERFACE_NAME);
            Enumeration addresses = netint.getInetAddresses();
            InetAddress inetAddress = null;
            while (addresses.hasMoreElements()) {
                InetAddress addr = (InetAddress)addresses.nextElement();
                if(addr instanceof Inet6Address)
                    if(!addr.isLinkLocalAddress()){
                        inetAddress = addr;
                        break;
                    }
            }
            if(inetAddress == null){
                inetAddress = netint.getInterfaceAddresses().get(0).getAddress();
            }
            MNCMcastTest test = new MNCMcastTest(inetAddress.getHostAddress());
            new Thread(test).start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(running) {
            byte data[] = name.getBytes();
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, MNCConsts.MULTICAST_ADDR.getJavaAddress(), MNCConsts.MCAST_PORT);
                udpClient.send(packet);
                Thread.sleep(500);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class McastReceiver implements Runnable{
        private MulticastSocket udpListener;
        private TreeSet<String> received;

        public McastReceiver() {
            received = new TreeSet<String>();
            try {
                udpListener = new MulticastSocket(MNCConsts.MCAST_PORT);
                InetAddress group = MNCConsts.MULTICAST_ADDR.getJavaAddress();
                udpListener.joinGroup(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (running) {
                byte[] buf = new byte[MNCConsts.MAX_UDP_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    udpListener.receive(packet);
                    received.add(new String(packet.getData()));
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    int i = 1;
                    for(String address: received){
                        System.out.println(i+".: "+address);
                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}