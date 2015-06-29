package org.humbird.soa.core.util;

import org.apache.commons.io.IOUtils;
import org.humbird.soa.common.model.common.PropsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.exceptions.ServiceError;
import org.humbird.soa.core.exceptions.ServiceException;

import java.io.*;
import java.util.*;

/**
 * Created by david on 15/3/19.
 */
public class CustomUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(CustomUtil.class);

    private static String resolveFolder(String key)
    {
        String keyProperty = HumbirdUtil.getProperty(key);
        if (keyProperty == null) {
            throw new ServiceException(ServiceError.APPLICATION_ERROR, key);
        }
        return HumbirdUtil.getProperty("org.humbird.soa.custom.folder") + "/" + keyProperty + "/";
    }

    public static String loadCustomFile(String filename, String key) throws ServiceException {
        return loadFile(filename, resolveFolder(key), "UTF-8");
    }

    public static String loadCustomContent(String filename, String key) throws ServiceException {
        return loadFile(filename, HumbirdUtil.getProperty("org.humbird.soa.custom.folder") + "/" + key + "/", "UTF-8");
    }

    public static Properties loadCustomPropertis(String filename, String key) throws ServiceException {
        return loadProperties(filename, HumbirdUtil.getProperty("org.humbird.soa.custom.folder") + "/" + key + "/");
    }

    public static PropsModel loadCustomPropsModel(String filename, String key) throws ServiceException {
        return loadPropsModel(filename, key, -1);
    }

    public static PropsModel loadCustomPropsModel(String filename, String key, int encrpyt) throws ServiceException {
        return loadPropsModel(filename, HumbirdUtil.getProperty("org.humbird.soa.custom.folder") + "/" + key + "/", encrpyt);
    }

    public static String loadFile(String filename, String folder, String encoding) throws ServiceException {
        String resourcePath = folder + filename;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*** loading file from " + resourcePath);
        }
        ClassLoader classLoader = CustomUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        try {
            return IOUtils.toString(inputStream, encoding);
        } catch (Exception e) {
            LOGGER.error("Could not load file:" + resourcePath);
            throw new ServiceException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ...
            }
        }
    }

    public static Properties loadProperties(String filename, String folder) throws ServiceException {
        String resourcePath = folder + filename;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*** loading properties from " + resourcePath);
        }
        ClassLoader classLoader = CustomUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Could not load properties:" + resourcePath);
            throw new ServiceException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ...
            }
        }
        return properties;
    }

    public static PropsModel loadPropsModel(String filename, String folder) throws ServiceException {
        return loadPropsModel(folder, filename, -1);
    }

    public static PropsModel loadPropsModel(String filename, String folder, int encrpyt) throws ServiceException {
        return loadPropsModelAssembly(folder + filename, encrpyt);
    }

    public static PropsModel loadPropsModelAssembly(String resourcePath, int encrpyt) throws ServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*** loading properties from " + resourcePath);
        }
        ClassLoader classLoader = CustomUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String lines = null;
        PropsModel propsModel = new PropsModel();
        try {
            if(encrpyt == -1) {
                while ((lines = reader.readLine()) != null) {
                    propsModel.add(lines);
                }
            } else {
                while ((lines = reader.readLine()) != null) {
                    propsModel.add(lines, encrpyt);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not load properties:" + resourcePath);
            throw new ServiceException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ...
            }
        }
        return propsModel;
    }

    public static Map<String, List<String>> showFolder() throws Exception {
        Map<String, List<String>> shows = new HashMap<String, List<String>>();
        List<HumbirdSession> humbirdSessionList = HumbirdUtil.getSessionList();
        for(HumbirdSession session : humbirdSessionList) {
            List<String> propNames = new ArrayList<String>();
            String folder = (String) session.getKey();
            Map<String, Properties> propertiesMap = (Map<String, Properties>) session.getValue();
            Iterator<Map.Entry<String, Properties>> iterator = propertiesMap.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, Properties> entry = iterator.next();
                propNames.add(entry.getKey());
            }
            shows.put(folder, propNames);
        }
        return shows;
    }
}
