package org.humbird.soa.cache.server.quorum;

import org.humbird.soa.cache.data.Cache;
import org.humbird.soa.cache.data.CacheReferee;
import org.humbird.soa.cache.server.DatadirCleanupManager;
import org.humbird.soa.cache.server.quorum.QuorumPeerConfig.ConfigException;
import org.humbird.soa.common.utils.SpringUtils;
import org.humbird.soa.db.HumbirdDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/6/4.
 */
public class HumbirCMain {

    private static final Logger LOG = LoggerFactory.getLogger(HumbirCMain.class);

    private static final String USAGE = "Usage: HumbirCMain configfile";

//    protected QuorumPeer quorumPeer;

    public static void main(String[] args) {
        HumbirCMain humbirCMain = new HumbirCMain();
        try {
            humbirCMain.initializeAndRun(args);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid arguments, exiting abnormally", e);
            LOG.info(USAGE);
            System.err.println(USAGE);
            System.exit(3);
        } catch (ConfigException e) {
            LOG.error("Invalid config, exiting abnormally", e);
            System.err.println("Invalid config, exiting abnormally");
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Unexpected exception, exiting abnormally", e);
            System.exit(1);
        }
    }

    protected void initializeAndRun(String[] args) throws ConfigException, IOException {
        QuorumPeerConfig config = new QuorumPeerConfig();
//        if (args.length == 1) {
//            config.parse(args[0]);
//        }
        // TODO replace
        config.parse("/install_apps/test_bak/Humbird/Humbird-Cache/src/main/resources/humbirdC.cfg");
        SpringUtils.init(config.getSpringFile());
        // Start and schedule the the purge task
        DatadirCleanupManager purgeMgr = new DatadirCleanupManager(config
                .getDataDir(), config.getDataLogDir(), config
                .getSnapRetainCount(), config.getPurgeInterval());
        purgeMgr.start();

        if (args.length == 1 && config.servers.size() > 0) {
            runFromConfig(config);
        } else {
            LOG.error("Either no config or no quorum defined in config, running "
                    + " in standalone mode");
            // there is only server in the quorum -- run as standalone
//            HumbirCMain.main(args);
            System.exit(1);
        }
    }

    public void runFromConfig(QuorumPeerConfig config) throws IOException {
//        try {
//            ManagedUtil.registerLog4jMBeans();
//        } catch (JMException e) {
//            LOG.warn("Unable to register log4j JMX control", e);
//        }

        LOG.info("Starting quorum peer");
        CacheReferee cacheReferee = new CacheReferee();
        try {
            cacheReferee.createCacheWrapper(config.getCacheType(), config.getCacheFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cache cache = cacheReferee.getCache();

        HumbirdDbManager humbirdDbManager = (HumbirdDbManager) SpringUtils.get().getBean("humbirdDbManager");
        List<Map<String, Object>> list = humbirdDbManager.getJdbcTemplate().queryForList("select 1 from dual");
        System.out.println("--- " + list.size());
        System.out.println("--- " + list.get(0).get("1"));
    }
}
