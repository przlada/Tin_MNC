package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Wątek odpowiedzialny za odbieranie połączeń TCP
 * @author Karol
 */
public class MNCUnicastReceiver implements Runnable{
    private ServerSocket server;
    private MNCDevice myDevice;
    private boolean running = true;
    private Thread myThread = null;


    public MNCUnicastReceiver(MNCDevice device){
        myDevice = device;
        try{
            server = new ServerSocket(MNCConsts.UCAST_PORT);
            System.out.println("serwer nasłuchuje");
        } catch (IOException e) {
            System.out.println("Could not listen on port");
        }
    }

    public synchronized boolean isRunning(){
        return running;
    }

    public synchronized void stopRunning(){
        running = false;
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getThread(){
        return myThread;
    }

    @Override
    public void run() {
        myThread = Thread.currentThread();
        try {
            while (isRunning()) {
                ClientWorker w;
                    w = new ClientWorker(server.accept());
                    Thread t = new Thread(w);
                    t.start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 4444");
        }
    }

    private class ClientWorker implements Runnable {
        private Socket client;

        public ClientWorker(Socket client) {
            this.client = client;
        }

        public void run(){
            try{
                ObjectInputStream in  = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                MNCDatagram sendDatagram = (MNCDatagram) in.readObject();
                int id = myDevice.receiveUnicastData(sendDatagram);
                out.writeObject(id);
                client.close();
            } catch (IOException e) {
                System.out.println("in or out failed");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
