package org.humbird.soa.component.esign.done;

import org.humbird.soa.component.esign.invoke.EsignInvoke;
import org.humbird.soa.component.esign.invoke.impl.ReceiveBillZipInvoke;
import org.humbird.soa.core.invoke.AsynDone;
import org.humbird.soa.core.model.ProtocolModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/4/7.
 */
public class ESignDone implements AsynDone
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ESignDone.class);
    private static final String METHOD_NAME = "method_name";
    private static final String RECEIVE = "4001";
    private static final String SEND = "4002";
    private static final EsignInvoke invoke = new ReceiveBillZipInvoke();

    public void initial()
            throws Exception
    {
        LOGGER.info("================= Initialize {} =================", ESignDone.class);
        invoke.init();
    }

    public void invoke(ProtocolModel protocolModel) throws Exception
    {
        String name = protocolModel.getRequest().getParameter("method_name");
        if (name == null) {
            String err = "The method parameter must be not null, now exsit.";
            LOGGER.error(err);
            throw new Exception(err);
        }
        if ("4001".equals(name))
            invoke.invoke();
    }

    public void refresh()
            throws Exception
    {
        invoke.init();
    }

    public void destory() throws Exception
    {
        invoke.destroy();
        LOGGER.info("================= Destory {} =================", ESignDone.class);
    }
}
