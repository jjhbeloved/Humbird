package org.humbird.soa.core.util;

import net.sf.ehcache.CacheManager;
import org.humbird.soa.common.model.common.PropsModel;
import org.humbird.soa.common.tools.TIO;
import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.cache.*;
import org.humbird.soa.core.code.Callback;
import org.humbird.soa.core.exceptions.ServiceException;
import org.humbird.soa.core.model.IndexModel;
import org.humbird.soa.core.model.PropertyModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by david on 15/3/19.
 */
public class HumbirdUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(HumbirdUtil.class);

    private static Properties DEFAULT_PROPERTIES = null;

    private static Properties MAPPING_PROPERTIES = null;

    private static final String CACHE_PATH = "/cache";

    private static final String HUMBIRD_CACHE = "humbird";

    private static Cache<String, HumbirdSession> PERSISTANCE_CACHE = null;

    private static Cache METADATA_CACHE = null;

    private static Cache SESSION_CACHE = null;

    private static final Locale LOCALE_DK = new Locale("da", "DK");

    private static SecureRandom secureRandom = null;

    private static boolean flag = true;

    // just only initial one
    public static void init() throws Exception {
        if (flag) {
            if (DEFAULT_PROPERTIES == null || MAPPING_PROPERTIES == null) {
                initDefaultProperties();
            }

            if (PERSISTANCE_CACHE == null) {
                initLocalPersistCache();
            }

            initFirstCache();

            flag = false;
        }
    }

    public static void destory() {
        removeAllSessions();

        if (DEFAULT_PROPERTIES == null) {
            DEFAULT_PROPERTIES.clear();
        }

        if (MAPPING_PROPERTIES == null) {
            MAPPING_PROPERTIES.clear();
        }
    }

    private static void initLocalPersistCache() {
        //  default cache is JVM
        String localCacheName = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_LOCAL_CACHE, KeyUtil.LOCAL_CACHE_EHCACHE);
        if (KeyUtil.LOCAL_CACHE_EHCACHE.equalsIgnoreCase(localCacheName)) {

            URL cacheConfigURL = HumbirdUtil.class.getResource(
                    CACHE_PATH + DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_LOCAL_CACHE_PATH, KeyUtil.DEFAULT_EHCACHE_PATH));
            PERSISTANCE_CACHE = new EHCacheWrapper(HUMBIRD_CACHE, CacheManager.create(cacheConfigURL));

        } else if (KeyUtil.LOCAL_CACHE_JVM.equalsIgnoreCase(localCacheName)) {
            String cacheConfigURL = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_LOCAL_CACHE_PATH, KeyUtil.DEFAULT_JVM_PATH);
            PERSISTANCE_CACHE = new JAVACacheWrapper(HUMBIRD_CACHE, CACHE_PATH + cacheConfigURL);

        } else {
            // warning ERROR
        }
    }

    public static void initLocalSessionCache(String name, String localCacheName, Object cacheManager) {
        if (KeyUtil.LOCAL_CACHE_EHCACHE.equalsIgnoreCase(localCacheName)) {
            SESSION_CACHE = new EHCacheWrapper(name, (CacheManager) cacheManager);
        } else if (KeyUtil.LOCAL_CACHE_JVM.equalsIgnoreCase(localCacheName)) {
            SESSION_CACHE = new JAVACacheWrapper(name, (String) cacheManager);
        } else {
            // warning ERROR
        }
    }

    private static void initDefaultProperties() {
        try {
            DEFAULT_PROPERTIES = new Properties();
            MAPPING_PROPERTIES = new Properties();
            DEFAULT_PROPERTIES.load(HumbirdUtil.class.getResourceAsStream("/default.properties"));
            MAPPING_PROPERTIES.load(HumbirdUtil.class.getResourceAsStream("/mapping.properties"));
        } catch (IOException e) {
            LOGGER.error("Could not load  default default.properties from classpath, ", e);
        }

    }

    public static void initLocalTestProperties() {
        Class clazz = HumbirdUtil.class;
        try {
            DEFAULT_PROPERTIES = new Properties();
            MAPPING_PROPERTIES = new Properties();
            DEFAULT_PROPERTIES.load(HumbirdUtil.class.getResourceAsStream("/unittest-default.properties"));
            MAPPING_PROPERTIES.load(HumbirdUtil.class.getResourceAsStream("/mapping.properties"));
        } catch (IOException e) {
            LOGGER.error("Could not load local-default.properties from classpath, ", e);
        }

        String customFolder = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_FOLDER, "custom") + "/";
        String absTestClasspath = getAbsoluteClasspath(clazz);
        DEFAULT_PROPERTIES.put(KeyUtil.DEFAULT_FOLDER_PATH, absTestClasspath + customFolder);
    }

    private static void initFirstCache() throws Exception {
        String metaDataName = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_METADATA_CACHE, KeyUtil.METADATA_CACHE_MEMCACHED);
        // master or slave
        if (KeyUtil.MASTER.equalsIgnoreCase(DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_CLUSTER))) {
            // add local prop file, then insert into redis ? memcached ? or others cache system
            if (KeyUtil.METADATA_CACHE_MEMCACHED.equalsIgnoreCase(metaDataName)) {
                String cacheConfigURL = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_METADATA_CACHE_PATH, KeyUtil.DEFAULT_MEMCACHED_PATH);
                HumbirdSession indexSession = initIndex();
                HumbirdSession customSession = initCustom();
                int expiration = Integer.parseInt(DEFAULT_PROPERTIES.getProperty(KeyUtil.METADATA_EXPIRATION, "0"));

                try {
                    METADATA_CACHE = new MEMCacheWrapper<String, CacheContext<String>>(CACHE_PATH + cacheConfigURL);
                    CacheContext cacheContext = new CacheContext(expiration);
                    cacheContext.getMap().put(KeyUtil.CUSTOM_KEY, customSession);
                    cacheContext.getMap().put(KeyUtil.INDEX_KEY, indexSession);
                    METADATA_CACHE.putOnlyOne(HUMBIRD_CACHE, cacheContext);
                } catch (Exception e) {
                    throw new Exception("Loading metadata cache failed, " + e.getMessage());
                }
                try {
                    setSession(KeyUtil.CUSTOM_KEY, customSession);
                    setSession(KeyUtil.INDEX_KEY, indexSession);
                } catch (Exception e) {
                    //
                }
            } else if (KeyUtil.METADATA_CACHE_REDIS.equalsIgnoreCase(metaDataName)) {

            } else {
                // warning ERROR
            }
        } else {
            int count = 0;
            boolean flag = false;
            // load local prop file, then insert into redis ? memcached ? or others cache system
            if (KeyUtil.METADATA_CACHE_MEMCACHED.equalsIgnoreCase(metaDataName)) {
                String cacheConfigURL = DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_METADATA_CACHE_PATH, KeyUtil.DEFAULT_MEMCACHED_PATH);
                CacheContext cacheContext = null;

                while (true) {
                    try {
                        METADATA_CACHE = new MEMCacheWrapper<String, CacheContext<String>>(CACHE_PATH + cacheConfigURL);
                        cacheContext = (CacheContext) getMetaDataSession(HUMBIRD_CACHE);
                    } catch (Exception e) {
                        count++;
                        for (int i = 0; i < 5; i++) {
                            try {
                                System.out.print(".. ");
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                // ignore
                            }
                        }
                        System.out.println();
                        if (count == 5) {    // waiting master 25s, if not, use local properties files
                            throw new Exception("Loading metadata cache failed, " + e.getMessage());
                        }
                        continue;
                    }
                    if (cacheContext != null) {
                        break;
                    }
                }

                Map<String, HumbirdSession> map = cacheContext.getMap();
                HumbirdSession customSession = map.get(KeyUtil.CUSTOM_KEY);
                HumbirdSession indexSession = initIndex();
                try {
                    setSession(KeyUtil.CUSTOM_KEY, customSession);
                    setSession(KeyUtil.INDEX_KEY, indexSession);
                } catch (Exception e) {
                    //
                }
            } else if (KeyUtil.METADATA_CACHE_REDIS.equalsIgnoreCase(metaDataName)) {

            } else {
                // warning ERROR
            }
        }
    }

    private static HumbirdSession initCustom() {

        final Map<String, Map<String, PropertyModel>> properties = new HashMap();

        final int encrpyt = Integer.parseInt(DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_ENCRYPT, KeyUtil.ENCRPYT));

        mapProperties(MAPPING_PROPERTIES, new Callback() {
            @Override
            public Object any(Object key, Object val) {

                String k = (String) key;
                String value = (String) val;
                String[] values = value.split("\\#");
                if (values.length > 1) {
                    String folder = values[0];
                    Map<String, PropertyModel> propertyModels = new HashMap();
                    for (int i = 1, size = values.length; i < size; i++) {
                        String file = values[i];
                        PropsModel propsModel = CustomUtil.loadCustomPropsModel(values[i], folder, encrpyt);
                        PropertyModel propertyModel = new PropertyModel(folder, file, k, propsModel, encrpyt);
                        String folder_file = folder + "/" + file;
                        propertyModels.put(folder_file, propertyModel);
                        //  if file be encrpyt
                        if(propsModel.isFlag()) {
                            URL url = HumbirdUtil.class.getResource("/" + DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_FOLDER, "custom") + "/" + folder_file);
                            BufferedWriter bufferedWriter = null;
                            try {
                                bufferedWriter = TIO.create(new File(url.getFile()), false);
                                writeProperties(propsModel, bufferedWriter);
                            } catch (IOException e) {
                                e.printStackTrace();
                                // ignore
                            } finally {
                                TIO.close(bufferedWriter);
                            }
                        }
                    }
                    properties.put(folder, propertyModels);
                }
                return null;
            }
        });
        return new HumbirdSession(DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_VERSION), properties);
    }

    private static HumbirdSession initIndex() {
        Map vals = new HashMap();
        Iterator<Map.Entry<Object, Object>> iterator = DEFAULT_PROPERTIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            vals.put(key, value);
        }
        IndexModel indexModel = new IndexModel(null, DEFAULT_PROPERTIES, vals);

        return new HumbirdSession(DEFAULT_PROPERTIES.getProperty(KeyUtil.DEFAULT_VERSION), indexModel);
    }

    public static String getAbsoluteClasspath(Class clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public static String getProperty(String key) {
        return System.getProperty(key, DEFAULT_PROPERTIES.getProperty(key, null));
    }

    public static String getMappingProperty(String key) {
        return System.getProperty(key, MAPPING_PROPERTIES.getProperty(key, null));
    }

    public static Integer getPropertyAsInteger(String key) {
        String property = getProperty(key);
        return property != null ? Integer.valueOf(property) : null;
    }

    public static HumbirdSession createSessionObject(String key, String value)
            throws ServiceException {
        return new HumbirdSession(key, value);
    }

    public static synchronized void setSession(String transactionId, HumbirdSession session) throws Exception {
        PERSISTANCE_CACHE.put(transactionId, session);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("*** Set persistance session with data: " + session.toString());
    }

    public static synchronized void setSessionCache(String transactionId, Object object) throws Exception {
        SESSION_CACHE.put(transactionId, object);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("*** Set variable session with data: " + object.toString());
    }

    public static synchronized void setMetaDataSessionCache(String transactionId, Object object) throws Exception {
        METADATA_CACHE.put(transactionId, object);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("*** Set metadata session with data: " + object.toString());
    }

    public static synchronized HumbirdSession getSession(String transactionId) throws Exception {
        return PERSISTANCE_CACHE.get(transactionId);
    }

    public static synchronized Object getCacheSession(String transactionId) throws Exception {
        return SESSION_CACHE.get(transactionId);
    }

    public static synchronized Object getMetaDataSession(String transactionId) throws Exception {
        return METADATA_CACHE.get(transactionId);
    }

    public static synchronized List<HumbirdSession> getSessionList() throws Exception {
        List keys = PERSISTANCE_CACHE.getKeys();
        List result = new ArrayList();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            result.add(PERSISTANCE_CACHE.get(key));
        }
        return result;
    }

    public static synchronized List getCacheSessionList() throws Exception {
        List keys = SESSION_CACHE.getKeys();
        List result = new ArrayList();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            result.add(SESSION_CACHE.get(key));
        }
        return result;
    }

    public static synchronized List getMetaDataSessionList() throws Exception {
        List keys = METADATA_CACHE.getKeys();
        List result = new ArrayList();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            result.add(METADATA_CACHE.get(key));
        }
        return result;
    }

    public static synchronized void removeAllSessions() {
        if(PERSISTANCE_CACHE != null) {
            PERSISTANCE_CACHE.clearCache();
        }
        if(SESSION_CACHE != null) {
            SESSION_CACHE.clearCache();
        }
        if(METADATA_CACHE != null) {
            METADATA_CACHE.clearCache();
        }
    }

    public static synchronized void removeAllCacheSessions() {
        SESSION_CACHE.removeAll();
    }

    public static synchronized void traceSessionCache() throws Exception {
        Iterator i$ = PERSISTANCE_CACHE.getKeys().iterator();
        while (i$.hasNext()) {
            Object element = i$.next();
            HumbirdSession session = PERSISTANCE_CACHE.get((String) element);
            LOGGER.trace("key: " + element + " session: " + session);
        }
    }

    public static synchronized void traceCacheSessionCache() throws Exception {
        Iterator i$;
        if (LOGGER.isTraceEnabled())
            for (i$ = SESSION_CACHE.getKeys().iterator(); i$.hasNext(); ) {
                Object element = i$.next();
                Object session = SESSION_CACHE.get(element);
                LOGGER.trace("key: " + element + " session: " + session);
            }
    }

    public static synchronized void traceMetaDataCacheSessionCache() throws Exception {
        Iterator i$ = METADATA_CACHE.getKeys().iterator();
        while (i$.hasNext()) {
            Object element = i$.next();
            Object session = METADATA_CACHE.get(element);
            LOGGER.trace("key: " + element + " session: " + session);
        }
    }


    public static synchronized void removeSession(String transactionId) throws Exception {
        PERSISTANCE_CACHE.remove(transactionId);
        LOGGER.debug("*** Removed session with transactionId: " + transactionId);
    }

    public static synchronized void removeCacheSession(String transactionId) throws Exception {
        SESSION_CACHE.remove(transactionId);
        LOGGER.debug("*** Removed session with transactionId: " + transactionId);
    }

    public static synchronized void removeMetaDataCacheSession(String transactionId) throws Exception {
        METADATA_CACHE.remove(transactionId);
        LOGGER.debug("*** Removed session with transactionId: " + transactionId);
    }

    public static synchronized void addCache(String name, Object param) throws Exception {
        PERSISTANCE_CACHE.addCache(name, param);
    }

    public static synchronized void addCacheSession(String name, Object param) throws Exception {
        SESSION_CACHE.addCache(name, param);
    }

    public static synchronized void switchCache(String name) {
        PERSISTANCE_CACHE.switchCache(name);
    }

    public static synchronized void switchCacheSession(String name) {
        SESSION_CACHE.switchCache(name);
    }

    public static synchronized void delCache(String name) throws Exception {
        if (!HUMBIRD_CACHE.equalsIgnoreCase(name)) {
            PERSISTANCE_CACHE.delCache(name);
        }
    }

    public static synchronized void delCacheSession(String name) throws Exception {
        SESSION_CACHE.delCache(name);
    }

    public static synchronized void addCacheSession(String name) {

    }

    public static int getSecureRandomInt(int n) {
        return secureRandom.nextInt(n);
    }

    public static String formatDate(Date inDate, String pattern) {
        DateTimeFormatter dtf;
        dtf = DateTimeFormat.forPattern(pattern);
        return dtf.withLocale(LOCALE_DK).print(inDate.getTime());
    }

    public static Date addDays(Date inDate, int days) {
        DateTime jodaDate = new DateTime(inDate);
        jodaDate = jodaDate.plusDays(days);
        return jodaDate.toDate();
    }

    public static boolean isCircuitNo(String value) {
        return (value != null) && ((value.toUpperCase().startsWith("EN")) || (value.toUpperCase().startsWith("EV")) || (value.toUpperCase().startsWith("EB")) || (value.toUpperCase().startsWith("EM")));
    }

    public static String getRandomCircuitNo(String service) {
        String result = "";
        if (service != null) {
            int randomPhoneNo = 100000 + getSecureRandomInt(899999);
        }
        return result;
    }

    public static String notNull(String value) {
        return value != null ? value : "";
    }

    public static boolean isBlank(Object value) {
        return (value == null) || ("".equals(value));
    }

    public static boolean isBlank(String value) {
        return (value == null) || ("".equals(value.trim()));
    }

    public static boolean hasLeadingZeroes(String value) {
        return (value != null) && (value.startsWith("0"));
    }

    public static String stripeLeadingZeroes(String value) {
        return value != null ? value.replaceAll("^0+", "") : null;
    }

    public static void mapProperties(Properties properties, Callback callback) {
        Enumeration<Object> propKeys = properties.keys();
        while (propKeys.hasMoreElements()) {
            String key = (String) propKeys.nextElement();
            String value = properties.getProperty(key);
            callback.any(key, value);
        }
    }

    public static void writeProperties(PropsModel propsModel, BufferedWriter bufferedWriter) throws IOException {
        int siz = propsModel.getProps().size();
        for(int i=0; i<siz; i++) {
            PropsModel.Prop prop = propsModel.getProps().get(i);
            if(prop == null) {
                bufferedWriter.newLine();
            } else {
                PropsModel.Comments comments = propsModel.getComms().get(i);
                if (comments != null) {
                    for (String comm : comments.getComments()) {
                        TIO.append(bufferedWriter, "# " + comm);
                        bufferedWriter.newLine();
                    }
                }
                TIO.append(bufferedWriter, prop.getKey() + " = " + prop.getVal());
                bufferedWriter.newLine();
            }
        }
    }

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info(e.getLocalizedMessage());
        } catch (NoSuchProviderException e) {
            LOGGER.info(e.getLocalizedMessage());
        }
    }

}
