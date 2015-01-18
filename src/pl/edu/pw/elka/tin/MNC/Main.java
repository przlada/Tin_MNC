package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;

import java.util.Arrays;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        if(args.length >= 2){
            String command;
            Scanner in = new Scanner(System.in);
            MNCSystemLog log = new MNCSystemLog(MNCDict.Langs.PL);
            MNCAddress.TYPE deviceType;
            if(args[0].equals("C"))
                deviceType = MNCAddress.TYPE.CONTROLLER;
            else
                deviceType = MNCAddress.TYPE.MONITOR;
            log.initialGroups(Arrays.copyOfRange(args, 1, args.length));
            log.startNewDevice(deviceType);
            log.startGuiManager();

            while(true){
                command = in.nextLine();
                if(command.equals("token")){
                    command = in.nextLine();
                    System.out.println(((MNCController) log.getDevice()).getToken(command));
                }
                else if(command.equals("tcp")){
                    command = in.nextLine();
                    MNCDeviceParameterSet paramSet = new MNCDeviceParameterSet(command);
                    paramSet.populateSet();
                    ((MNCController) log.getDevice()).sendParameterSet(paramSet);
                }
                else if(command.equals("transfer")){
                    command = in.nextLine();
                    ((MNCController) log.getDevice()).transferToken(command);
                }
                else if(command.equals("quit") || command.equals("q")){
                    log.stopDevice();
                    break;
                }
                else if(command.equals("stop")){
                    log.stopDevice();
                }
                else if(command.equals("start")){
                    log.startNewDevice(deviceType);
                    for (int i = 1; i < args.length; i++)
                        log.getDevice().addGroup(args[i]);
                }
            }
        }
    }
}
