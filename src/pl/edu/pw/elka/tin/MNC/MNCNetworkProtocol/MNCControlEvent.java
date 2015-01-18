package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import java.io.Serializable;

/**
 * Created by przemek on 11.01.15.
 */
public class MNCControlEvent implements Serializable{
    public static enum TYPE{
        //Logi
        HaveNewTokenOwner,
        SendByMulticast,
        ReceiveFromMulticast,
        ReceiveFromUnicast,
        DataConsumption,
        SendByUnicast,
        AddedNewDeviceToGroup,
        DataAlreadyConsumed,
        DataReBroadcast,
        ReceivedToken,
        SentDataBroadcastConfirm,
        TokenOutOfReach,
        TokenOwnerAssignment,
        TokenTransfered,
        TokenTransferError,
        RemoveDeviceFromTokenList,
        //Komendy od gui managera
        Command,
        //Dane specjalne dla GuiManagera
        Start,
        MyGroups,
        MyTokens,
        TokenInfo
    }

    public final TYPE type;
    public final Object data;
    public String[] group = null;
    public String name = null;

    public MNCControlEvent(TYPE t, Object d){
        type = t;
        data = d;
    }

    public MNCControlEvent(TYPE t, Object d, String[] group){
        type = t;
        data = d;
        this.group = group;
    }

    public void setName(String n){
        name = n;
    }

    public String getName(){
        return name;
    }

    public TYPE getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String[] getGroup() {
        return group;
    }

    public String toString(){
        String text = "";
        if(type == TYPE.Command){
            text = "Command from Gui "+(String)data+" to group:";
            if(group != null)
                for(String g : group)
                    text+=g+" ";
        }
        return text;
    }
}
