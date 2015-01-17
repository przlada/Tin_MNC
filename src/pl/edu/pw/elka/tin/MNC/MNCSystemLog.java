package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCControlEvent;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;

import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.Langs;
import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.getLangText;
import static pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCControlEvent.TYPE;

/**
 * Klasa odpowiedzialna za odbieranie i wyświetlanie wszelkich zdarzeń.
 * @author Paweł
 */
public class MNCSystemLog {
    private Langs lang;
    private String controllerName = null;
    private MNCDevice device;
    private MNCGuiMenagerCommunication guiManager;
    private MNCAddress.TYPE deviceType;

    public MNCSystemLog(Langs l){
        setLang(l);
        guiManager = new MNCGuiMenagerCommunication();
    }

    public static String getLocalAddress(){
        NetworkInterface netint = null;
        try {
            netint = NetworkInterface.getByName(MNCConsts.DEFAULT_INTERFACE_NAME);
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
            return inetAddress.getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void startNewDevice(MNCAddress.TYPE type){
        if(device == null){
            MNCAddress deviceAddress = new MNCAddress(getLocalAddress(), deviceType);
            controllerName = deviceAddress.toString();
            deviceType = type;
            try {
                device = new MNCController(deviceType.toString(), deviceAddress, this);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopDevice(){
        if(device != null){
            device.closeDevice();
            device = null;
        }
    }

    public MNCDevice getDevice(){
        return device;
    }

    public synchronized void setLang(Langs l){
        lang = l;
    }

    private void print(String text){
        Date date= new Date();
        //System.out.println(new Timestamp(date.getTime()) + " " + controllerName+ " " + text);
        System.out.println(controllerName+ " " + text);
    }

    public void acction(String type){
        print(type);
    }

    public void actionNewTokenOwner(String group){
        print(getLangText(lang,"HaveNewTokenOwner")+group+" "+device.getTokensOwners().get(group));
    }
    public void actionReceiveUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromUnicast")+datagram);
    }

    public void actionSendUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByUnicast")+datagram);
    }

    public void actionReceiveDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromMulticast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.ReceiveFromMulticast, getLangText(lang,"ReceiveFromMulticast")+datagram));
        //guiManager.sendToManager(getLangText(lang,"ReceiveFromMulticast")+datagram);
    }

    public void actionAddedNewDevice(String group, MNCAddress address){
        print(getLangText(lang,"AddedNewDeviceToGroup")+group+" "+address);
    }

    public void actionDataAlreadyConsumed(MNCDatagram data){
        print(getLangText(lang,"DataAlreadyConsumed")+data);
    }

    public void actionDataReBroadcast(MNCDatagram data){
        print(getLangText(lang,"DataReBroadcast")+data);
    }

    public void actionReceivedToken(MNCToken token){
        print(getLangText(lang,"ReceivedToken")+token);
    }

    public void actionSendDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByMulticast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.ReceiveFromMulticast, getLangText(lang,"SendByMulticast")+datagram));
    }

    public void dataConsumption(MNCDeviceParameterSet set){
        print(getLangText(lang,"DataConsumption")+set.getGroup()+" "+set.getParameterSetID());
    }

    public void actionSentDataBroadcastConfirm(){
        print(getLangText(lang,"SentDataBroadcastConfirm"));
    }

    public void actionTokenOutOfReach(MNCAddress address){
        print(getLangText(lang,"TokenOutOfReach")+address);
    }

    public void actionTokenOwnerAssignment(){
        print(getLangText(lang,"TokenOwnerAssignment"));
    }

    public void actionTokenTransfered(MNCAddress address){
        print(getLangText(lang,"TokenTransfered")+address);
    }

    public void actionTokenTransferError(MNCAddress address){
        print(getLangText(lang,"TokenTransferError")+address);
    }

    public void actionRemoveDeviceFromTokenList(MNCAddress address){
        print(getLangText(lang,"RemoveDeviceFromTokenList")+address);
    }

    public void stopWorking(){
        guiManager.setRunning(false);
    }

    private class MNCGuiMenagerCommunication{
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected = false;
        private boolean running = true;

        public MNCGuiMenagerCommunication(){
            try {
                //socket = new Socket(MNCConsts.GUI_MANAGER_HOST, MNCConsts.GUI_MANAGER_PORT);
                socket = new Socket();
                socket.connect(new InetSocketAddress(MNCConsts.GUI_MANAGER_HOST, MNCConsts.GUI_MANAGER_PORT), 1000);
                out = new ObjectOutputStream(socket.getOutputStream());
                new Thread(new ReceiveFromManager()).start();
                connected = true;
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Nie można nawiażać połączenia z managerem");
            }
        }
        public synchronized void sendToManager(MNCControlEvent data){
            if(!connected) return;
            try {
                out.writeObject(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized boolean isRunning(){
            return running;
        }

        public synchronized void setRunning(boolean r){
            running = r;
        }

        private class ReceiveFromManager implements Runnable{

            @Override
            public void run() {
                try {
                    in = new ObjectInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    connected = false;
                }
                while(isRunning()){
                    try {
                        MNCControlEvent mncControlEvent = (MNCControlEvent) in.readObject();
                        System.out.println((String)mncControlEvent.data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
