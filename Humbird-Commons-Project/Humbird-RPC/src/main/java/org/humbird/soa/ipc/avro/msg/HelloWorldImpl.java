package org.humbird.soa.ipc.avro.msg;

import org.apache.avro.AvroRemoteException;
import org.humbird.soa.ipc.avro.vo.Curse;
import org.humbird.soa.ipc.avro.vo.Greeting;
import org.humbird.soa.ipc.avro.vo.HelloWorld;

/**
 * Created by david on 15/6/6.
 */
public class HelloWorldImpl implements HelloWorld {

    @Override
    public Greeting hello(Greeting greeting) throws AvroRemoteException, Curse {
        int i = 999;
        System.out.println(greeting);
        if(greeting.getMessage().toString().equalsIgnoreCase("how are you")){
            greeting.setMessage("not too bad");
            return greeting;
        }
        return null;
    }
}
