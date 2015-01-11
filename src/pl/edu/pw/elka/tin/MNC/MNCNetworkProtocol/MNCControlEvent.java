package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import java.io.Serializable;

/**
 * Created by przemek on 11.01.15.
 */
public class MNCControlEvent implements Serializable {
    public static enum TYPE{
        //FORM MNC
        ReceiveFromMulticast
    }

    public final TYPE type;
    public final Object data;

    public MNCControlEvent(TYPE t, Object d){
        type = t;
        data = d;
    }

    public TYPE getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
