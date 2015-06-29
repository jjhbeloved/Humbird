package org.humbird.soa.common.jms;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by david on 15/5/22.
 */
public class JMSFactory {

    public static InitialContext init() throws NamingException {
        return new InitialContext();
    }

    public static InitialContext init(Properties properties) throws NamingException {
        return new InitialContext(properties);
    }

    public static ConnectionFactory newFactory(InitialContext initialContext, String jndiName) throws NamingException {
        return (ConnectionFactory) initialContext.lookup(jndiName);
    }

    public static Connection newConnect(ConnectionFactory connectionFactory) throws JMSException {
        return connectionFactory.createConnection();
    }

    public static Session newSession(Connection connection, boolean flag, int param) throws JMSException {
        return connection.createSession(flag, param);
    }

    public static Destination newDestination(InitialContext initialContext, String name) throws NamingException {
        return (Destination) initialContext.lookup(name);
    }

    public static MessageProducer newProducer(Session session, Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    public static MessageConsumer newConsumer(Session session, Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }

    public static void start(Connection connection) throws JMSException {
        connection.start();
    }

    public static void send(MessageProducer messageProducer, Message message) throws JMSException {
        messageProducer.send(message);
    }

    public static Message receive(MessageConsumer messageConsumer) throws JMSException {
        return messageConsumer.receive();
    }

    public static void close(Connection connection, Session session, MessageConsumer messageConsumer, MessageProducer messageProducer) {
        if(connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                // ignore
            }
        }
        if(session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                // ignore
            }
        }
        if(messageConsumer != null) {
            try {
                messageConsumer.close();
            } catch (JMSException e) {
                // ignore
            }
        }
        if(messageProducer != null) {
            try {
                messageProducer.close();
            } catch (JMSException e) {
                // ignore
            }
        }
    }
}
