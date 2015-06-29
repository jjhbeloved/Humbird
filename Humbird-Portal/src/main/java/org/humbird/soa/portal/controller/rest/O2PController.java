package org.humbird.soa.portal.controller.rest;

import org.humbird.soa.core.invoke.AsynDone;
import org.humbird.soa.core.invoke.Done;
import org.humbird.soa.core.model.ProtocolModel;
import org.humbird.soa.portal.view.InvokeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by david on 15/3/22.
 */
@Controller
@RequestMapping("/o2p")
public class O2PController {

    private final static Logger LOGGER = LoggerFactory.getLogger(O2PController.class);

    @Autowired
    @Qualifier("invokeModel")
    private InvokeModel invokeModel;

    public void setInvokeModel(InvokeModel invokeModel) {
        this.invokeModel = invokeModel;
    }

    /**
     * just invoke the data. not result view.
     *
     * @param folder
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{folder}")
    public ModelAndView get(@PathVariable String folder, HttpServletRequest request, HttpServletResponse response) throws IOException {

        ProtocolModel protocolModel = new ProtocolModel("o2p", folder, request, response);

        Done done = invokeModel.getSources().get(folder);
        try {
            if (done instanceof AsynDone) {
                new Thread(new AsynInvoke(done, protocolModel)).start();
                response.setStatus(200);
            } else {
                done.invoke(protocolModel);
            }
        }   catch (Exception e) {

        }
        return null;
    }

    class AsynInvoke implements Runnable {

        private Done asynDone;
        private ProtocolModel protocolModel;

        public AsynInvoke(Done asynDone, ProtocolModel protocolModel) {
            this.asynDone = asynDone;
            this.protocolModel = protocolModel;
        }

        @Override
        public void run() {
            try {
                asynDone.invoke(protocolModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}