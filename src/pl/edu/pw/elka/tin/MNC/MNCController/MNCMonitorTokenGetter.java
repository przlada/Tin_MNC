package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;

import java.io.IOException;

/**
 * Wątek odpowiedzialny za ustalenie adresu sterownika z tokenem.
 * @author Maciek
 */
public class MNCMonitorTokenGetter implements Runnable {
    private MNCMonitor parentMonitor;
    private String group;
    private boolean running = true;
    private Thread myThread = null;

    public synchronized boolean isRunning(){
        return running;
    }

    public synchronized void setRunning(boolean r){
        running = r;
    }

    public MNCMonitorTokenGetter(MNCMonitor monitor, String group){
        parentMonitor = monitor;
        this.group = group;
    }

    public Thread getMyThread(){
        return myThread;
    }

    @Override
    public void run() {
        myThread = Thread.currentThread();
        while(!parentMonitor.tokensOwners.containsKey(group) && isRunning()){
            MNCDatagram isThereToken = new MNCDatagram(parentMonitor.getMyAddress(), MNCConsts.MULTICAST_ADDR,group, MNCDatagram.TYPE.IS_THERE_TOKEN, null);
            try {
                parentMonitor.sendDatagram(isThereToken);
                Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        parentMonitor.tokenOwnerGetters.remove(group);
    }
}
