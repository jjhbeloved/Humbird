package org.humbird.soa.ipc.avro.msg;

import org.apache.avro.AvroRemoteException;
import org.humbird.soa.ipc.avro.vo.Message;
import org.humbird.soa.ipc.avro.vo.MessageProtocol;

/**
 * Created by david on 15/6/6.
 */
public class MessageProtocolImpl implements MessageProtocol {

    @Override
    public Message sendMessage(Message message) throws AvroRemoteException {
        System.out.println("----- " + message);
        System.out.println("Got a new message:");
        System.out.println("\tname: " + message.getName());
        System.out.println("\ttype: " + message.getType());
        System.out.println("\tprice: " + message.getPrice());
        System.out.println("\tvalid: " + message.getValid());
        System.out.println("\tcontent: " + new String(message.getContent().array()));
        System.out.print("\ttags:");
        for (CharSequence tag: message.getTags()) {
            System.out.print(" " + tag);
        }
        System.out.printf("");
        return message;
    }
}
