package org.humbird.soa.common.jms;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

public class JMSFactoryTest {

    private static final String USER = "weblogic"; //Set the "User"

    private static final String PASSWORD = "12345678"; //Set the "Password"

    private static final String SAP_NAMING_PROVIDER_URL = "t3s://192.168.1.51:17002"; // Set the <server_host> and the <p4_port>
//      private static final String SAP_NAMING_PROVIDER_URL = "t3s://10.1.249.84:8012"; // Set the <server_host> and the <p4_port>

    private static final String SAP_INITIAL_CONTEXT_FACTORY_IMPL = "weblogic.jndi.WLInitialContextFactory";

    {
        System.setProperty("javax.net.ssl.trustStore", "/install_apps/iso/store/telenor_identify.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "odcSOAdk");
    }

    public void newProducerTest() throws NamingException, JMSException {

//        System.setProperty("weblogic.security.SSL.ignoreHostnameVerification","true");
//        System.setProperty("java.protocol.handler.pkgs", "weblogic.net");
//        System.setProperty("weblogic.security.TrustKeyStore","CustomTrust");
//        System.setProperty("weblogic.security.CustomTrustKeyStoreFileName", "/install_apps/iso/store/telenor_trust2.jks");
//        System.setProperty("weblogic.security.CustomTrustKeyStorePassPhrase","odcSOAdk");
//        System.setProperty("weblogic.security.CustomTrustKeyStoreType","jks");


        /* JNDI service naming environment properties initialization */

        Properties ctxProp = new Properties();
        ctxProp.put(Context.INITIAL_CONTEXT_FACTORY,
                SAP_INITIAL_CONTEXT_FACTORY_IMPL);
        ctxProp.put(Context.PROVIDER_URL, SAP_NAMING_PROVIDER_URL);
        ctxProp.put(Context.SECURITY_PRINCIPAL, USER);
        ctxProp.put(Context.SECURITY_CREDENTIALS, PASSWORD);

        InitialContext initialContext;
        QueueConnectionFactory qconFactory;
        QueueConnection qcon;
        QueueSession qsession;
        QueueSender qsender;
        Queue queue;
        TextMessage msg;

        initialContext = JMSFactory.init(ctxProp);
//        /**测试JNDI 是否联连成功 是否可以把属性挂在JNDI 树上 开始*/
//        initialContext.bind("name","zs");
//        Object ojbObject = initialContext.lookup("name");
//        System.out.println(ojbObject.toString());
//        /****/
        Hashtable hashtable = initialContext.getEnvironment();
        System.out.println(hashtable.size());

        Iterator iterator = hashtable.entrySet().iterator();
        while(iterator.hasNext()) {
            Object o =  iterator.next();
            System.out.println(o);
        }
        qconFactory = (QueueConnectionFactory) JMSFactory.newFactory(initialContext, "oebsFactory");
        qcon = (QueueConnection) JMSFactory.newConnect(qconFactory);
        qsession = (QueueSession) JMSFactory.newSession(qcon, false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) JMSFactory.newDestination(initialContext, "oebsQueue");
        qsender = (QueueSender) JMSFactory.newProducer(qsession, queue);
        msg = qsession.createTextMessage();
        msg.setText("hello mifans");

        JMSFactory.start(qcon);
        JMSFactory.send(qsender, msg);

        JMSFactory.close(qcon, qsession, null, qsender);
    }

    public void newConsumerTest() throws NamingException, JMSException {

        /* JNDI service naming environment properties initialization */

        Properties ctxProp = new Properties();
        ctxProp.put(Context.INITIAL_CONTEXT_FACTORY,
                SAP_INITIAL_CONTEXT_FACTORY_IMPL);
        ctxProp.put(Context.PROVIDER_URL, SAP_NAMING_PROVIDER_URL);
        ctxProp.put(Context.SECURITY_PRINCIPAL, USER);
        ctxProp.put(Context.SECURITY_CREDENTIALS, PASSWORD);

        InitialContext initialContext;
        QueueConnectionFactory qconFactory;
        QueueConnection qcon;
        QueueSession qsession;
        QueueReceiver queueReceiver;
        Queue queue;

        initialContext = JMSFactory.init(ctxProp);
        qconFactory = (QueueConnectionFactory) JMSFactory.newFactory(initialContext, "oebsFactory");
        qcon = (QueueConnection) JMSFactory.newConnect(qconFactory);
        qsession = (QueueSession) JMSFactory.newSession(qcon, false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) JMSFactory.newDestination(initialContext, "oebsQueue");
        queueReceiver = (QueueReceiver) JMSFactory.newConsumer(qsession, queue);

        JMSFactory.start(qcon);
        TextMessage message = (TextMessage) JMSFactory.receive(queueReceiver);
        System.out.println(message.getText());

        JMSFactory.close(qcon, qsession, queueReceiver, null);
    }

    public static void main(String []argv) throws JMSException, NamingException {
//        JMSFactoryTest jmsFactoryTest = new JMSFactoryTest();
//        jmsFactoryTest.newProducerTest();
//        jmsFactoryTest.newConsumerTest();
    }
}