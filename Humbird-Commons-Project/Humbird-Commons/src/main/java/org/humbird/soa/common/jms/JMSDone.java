package org.humbird.soa.common.jms;

/**
 * Created by david on 15/5/22.
 */
public interface JMSDone {

    void send(Object obj);

    Object receive();
}
