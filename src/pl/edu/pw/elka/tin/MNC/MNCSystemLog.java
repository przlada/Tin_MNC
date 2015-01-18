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
import java.util.HashSet;
import java.util.Set;

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
    private MNCAddress.TYPE deviceType = MNCAddress.TYPE.CONTROLLER;
    private String[] initialGroups = null;

    public MNCSystemLog(Langs l){
        setLang(l);
        guiManager = new MNCGuiMenagerCommunication();
    }

    public void initialGroups(String[] groups){
        initialGroups = groups;
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
                guiManager.sendToManager(new MNCControlEvent(TYPE.ControllerStarted, getLangText(lang,"ControllerStarted")));
                for(String group: initialGroups) {
                    System.out.println("dodano "+group);
                    device.addGroup(group);
                }
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
            guiManager.sendToManager(new MNCControlEvent(TYPE.ControllerStoped, getLangText(lang,"ControllerStoped")));
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

    //Akcje
    public void actionNewTokenOwner(String group){
        print(getLangText(lang,"HaveNewTokenOwner")+group+" "+device.getTokensOwners().get(group));
        guiManager.sendToManager(new MNCControlEvent(TYPE.HaveNewTokenOwner, getLangText(lang,"HaveNewTokenOwner")+group+" "+device.getTokensOwners().get(group)));
    }
    public void actionReceiveUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromUnicast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.ReceiveFromUnicast, getLangText(lang,"ReceiveFromUnicast")+datagram));
    }

    public void actionSendUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByUnicast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.SendByUnicast, getLangText(lang,"SendByUnicast")+datagram));
    }

    public void actionReceiveDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromMulticast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.ReceiveFromMulticast, getLangText(lang,"ReceiveFromMulticast")+datagram));
        //guiManager.sendToManager(getLangText(lang,"ReceiveFromMulticast")+datagram);
    }

    public void actionAddedNewDevice(String group, MNCAddress address){
        print(getLangText(lang,"AddedNewDeviceToGroup")+group+" "+address);
        guiManager.sendToManager(new MNCControlEvent(TYPE.AddedNewDeviceToGroup, getLangText(lang,"AddedNewDeviceToGroup")+group+" "+address));
    }

    public void actionDataAlreadyConsumed(MNCDatagram data){
        print(getLangText(lang,"DataAlreadyConsumed")+data);
        guiManager.sendToManager(new MNCControlEvent(TYPE.DataAlreadyConsumed, getLangText(lang,"DataAlreadyConsumed")+data));
    }

    public void actionDataReBroadcast(MNCDatagram data){
        print(getLangText(lang,"DataReBroadcast")+data);
        guiManager.sendToManager(new MNCControlEvent(TYPE.DataReBroadcast, getLangText(lang,"DataReBroadcast")+data));
    }

    public void actionReceivedToken(MNCToken token){
        print(getLangText(lang,"ReceivedToken")+token);
        guiManager.sendToManager(new MNCControlEvent(TYPE.ReceivedToken, getLangText(lang,"ReceivedToken")+token));
    }

    public void actionSendDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByMulticast")+datagram);
        guiManager.sendToManager(new MNCControlEvent(TYPE.SendByMulticast, getLangText(lang,"SendByMulticast")+datagram));
    }

    public void dataConsumption(MNCDeviceParameterSet set){
        print(getLangText(lang,"DataConsumption")+set.getGroup()+" "+set.getParameterSetID());
        guiManager.sendToManager(new MNCControlEvent(TYPE.DataConsumption, getLangText(lang,"DataConsumption")+set.getGroup()+" "+set.getParameterSetID()));
    }

    public void actionSentDataBroadcastConfirm(){
        print(getLangText(lang,"SentDataBroadcastConfirm"));
        guiManager.sendToManager(new MNCControlEvent(TYPE.SentDataBroadcastConfirm, getLangText(lang,"SentDataBroadcastConfirm")));
    }

    public void actionTokenOutOfReach(MNCAddress address){
        print(getLangText(lang,"TokenOutOfReach")+address);
        guiManager.sendToManager(new MNCControlEvent(TYPE.TokenOutOfReach, getLangText(lang,"TokenOutOfReach")+address));
    }

    public void actionTokenOwnerAssignment(){
        print(getLangText(lang,"TokenOwnerAssignment"));
        guiManager.sendToManager(new MNCControlEvent(TYPE.TokenOwnerAssignment, getLangText(lang,"TokenOwnerAssignment")));
    }

    public void actionTokenTransfered(MNCAddress address){
        print(getLangText(lang,"TokenTransfered")+address);
        guiManager.sendToManager(new MNCControlEvent(TYPE.TokenTransfered, getLangText(lang,"TokenTransfered")+address));
    }

    public void actionTokenTransferError(MNCAddress address){
        print(getLangText(lang,"TokenTransferError")+address);
        guiManager.sendToManager(new MNCControlEvent(TYPE.TokenTransferError, getLangText(lang,"TokenTransferError")+address));
    }

    public void actionRemoveDeviceFromTokenList(MNCAddress address){
        print(getLangText(lang,"RemoveDeviceFromTokenList")+address);
        guiManager.sendToManager(new MNCControlEvent(TYPE.RemoveDeviceFromTokenList, getLangText(lang,"RemoveDeviceFromTokenList")+address));
    }

    //Koniec Akcji


    public void informGuiManagerTokensChange(){
        if(device instanceof MNCController) {
            Set<String> tokenGroups = new HashSet<String>();
            for(String group : device.getGroups()){
                if(((MNCController)device).getToken(group) != null)
                    tokenGroups.add(((MNCController)device).getToken(group).getGroup());
            }
            guiManager.sendToManager(new MNCControlEvent(TYPE.MyTokens, null, tokenGroups.toArray(new String[tokenGroups.size()])));
        }
    }

    public void informGuiManagerGroupsChange(){
        guiManager.sendToManager(new MNCControlEvent(TYPE.MyGroups, null, device.getGroups().toArray(new String[device.getGroups().size()])));
    }

    public void stopWorking(){
        guiManager.setRunning(false);
    }

    public synchronized void receiveCommandFromManager(MNCControlEvent command){
        print(command.toString());
        String cmd = (String)command.getData();
        if(cmd.equals("shutdown/power_on")) {
            if (device == null)
                startNewDevice(deviceType);
            else
                stopDevice();
        }
        else if(cmd.equals("add group")){
            device.addGroup(command.getGroup()[0]);
        }
        else if(cmd.equals("send data")){
            if(device instanceof MNCController){
                MNCDeviceParameterSet paramSet = new MNCDeviceParameterSet(command.getGroup()[0]);
                paramSet.populateSet();
                ((MNCController) device).sendParameterSet(paramSet);
            }
        }
        else if(cmd.equals("show_token")){
            if(device instanceof MNCController) {
                MNCToken token = ((MNCController) device).getToken(command.getGroup()[0]);
                if(token != null){
                    guiManager.sendToManager(new MNCControlEvent(TYPE.TokenInfo, token.toString(), command.getGroup()));
                }
            }
        }

    }

    public synchronized void startGuiManager(){
        Boolean isMonitor = (deviceType == MNCAddress.TYPE.MONITOR);
        MNCControlEvent data = new MNCControlEvent(TYPE.Start, isMonitor, device.getGroups().toArray(new String[device.getGroups().size()]));
        data.setName(device.getMyAddress().toString());
        guiManager.sendToManager(data);
    }

    private class MNCGuiMenagerCommunication{
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected = false;
        private boolean running = true;

        public MNCGuiMenagerCommunication(){
            try {
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
                        receiveCommandFromManager(mncControlEvent);
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
