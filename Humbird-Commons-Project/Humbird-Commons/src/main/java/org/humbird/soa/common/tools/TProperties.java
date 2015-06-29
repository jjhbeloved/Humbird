package org.humbird.soa.common.tools;

import org.apache.commons.lang.StringUtils;
import org.humbird.soa.common.codec.Codec;
import org.humbird.soa.common.codec.CodecCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by david on 15/3/9.
 */
public class TProperties {

    private final static Logger LOGGER = LoggerFactory.getLogger(TProperties.class);

    public final static char symbol = '#';

    /**
     * @param name
     * @return
     */
    public static Properties get(String name) throws IOException {
        InputStream inputStream = TProperties.class.getClassLoader().getResourceAsStream(name);
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    /**
     * @param name
     * @return
     */
    public static URL getResource(String name) {
        return TProperties.class.getClassLoader().getResource(name);
    }

    /**
     * @param classLoader
     * @param name
     * @return
     */
    public static Properties get(ClassLoader classLoader, String name) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(name);
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    /**
     * @param name          配置文件路径
     * @param codecCallback 回调函数
     * @throws java.io.IOException
     */
    public static void enc(String name, CodecCallback codecCallback) throws IOException {
        Properties properties = TProperties.get(name);
        enc(properties, codecCallback);
    }

    /**
     * 要想会写到对应的文件, 需要在配置文件加入 path 这个参数
     *
     * @param properties    配置文件
     * @param codecCallback 回调函数
     * @throws java.io.IOException
     */
    public static void enc(Properties properties, CodecCallback codecCallback) throws IOException {

        boolean flag = false;
        String path = null;

        Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            List vals = null;
            if ((vals = codecCallback.encrypt(key, value)) != null) {
                if ((Boolean) vals.get(0) == true) {
                    flag = true;
                    properties.setProperty(key, (String) vals.get(1));
                } else {
                    value = (String) vals.get(1);
                }
            }
            try {
                if ("path".equalsIgnoreCase(key)) {
                    path = Codec.decode(properties.getProperty(key));
                }
            } catch (Exception e) {
                continue;
            } finally {
                LOGGER.trace(key + " ::: " + value);
            }
        }
        if (flag && !StringUtils.isEmpty(path)) {
            LOGGER.debug("encrypt properties");
            URL url = TProperties.getResource(path);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(url.getFile());
                properties.store(fos, "encrypt properties");
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    /**
     * 要想会写到对应的文件, 需要在配置文件加入 path 这个参数
     *
     * @param properties    配置文件
     * @param codecCallback 回调函数
     * @throws java.io.IOException
     */
    public static void encAll(Properties properties, CodecCallback codecCallback) throws IOException {

    }

    public static void saveProperties() {

    }

}