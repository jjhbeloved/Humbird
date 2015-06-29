package org.humbird.soa.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by david on 15/6/5.
 */
public class SpringUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringUtils.class);

    private static boolean flag = true;

    private static ApplicationContext context;

    public static boolean init(String []files) {
        if(flag) {
            try {
                context = new ClassPathXmlApplicationContext(files);
            } catch (BeansException e) {
                LOGGER.error("initial spring configure failed. reason is {}", e.getMessage());
                System.exit(0);
            }
            flag = false;
        }
        return !flag;
    }

    /**
     * if not init, return null.
     * @return
     */
    public static ApplicationContext get() {
        if(flag) {
            return null;
        } else {
            return context;
        }
    }

    public static void clear() {
        context = null;
        flag = true;
    }
}
