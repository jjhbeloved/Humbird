package org.humbird.soa.portal.listener;

import org.humbird.soa.core.util.CacheUtil;
import org.humbird.soa.core.util.HumbirdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

/**
 * Created by david on 15/3/19.
 */
public class HumbirdListener extends ContextLoaderListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(HumbirdListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.debug("============================== Initial Humbird Project ==============================");
        CacheUtil.newInstance(WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.getServletContext()));
        try {
            HumbirdUtil.init();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug("******* Loading Cache Error *******");
            HumbirdUtil.destory();
            LOGGER.debug("============================== Destory Humbird Project ==============================");
            System.exit(0);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        HumbirdUtil.destory();
        LOGGER.debug("============================== Destory Humbird Project ==============================");
    }
}
