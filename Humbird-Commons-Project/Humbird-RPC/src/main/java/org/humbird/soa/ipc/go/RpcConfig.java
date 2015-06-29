package org.humbird.soa.ipc.go;

import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ClusterH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ClusterMemberH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH.ProbeTypeH;
import org.humbird.soa.ipc.wire.CacheStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by david on 15/6/8.
 */
public class RpcConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RpcConfig.class);

    public static ClusterH.Builder clusterbuilder = ClusterH.newBuilder();
    public static ClusterMemberH.Builder local = ClusterMemberH.newBuilder();
    public static ClusterMemberH.Builder masterMember =  ClusterMemberH.newBuilder()
            .setMors(ClusterMemberH.MasterOrSlaveH.MASTER);
    public static Map<String, ClusterMemberH.Builder> mems = new HashMap<String, ClusterMemberH.Builder>();
    public static Map<String, CacheStat> caches = new HashMap<String, CacheStat>();
    public static Map<ProbeTypeH, Integer> versions = new HashMap<ProbeTypeH, Integer>();

    @SuppressWarnings("serial")
    public static class ConfigException extends Exception {
        public ConfigException(String msg) {
            super(msg);
        }
        public ConfigException(String msg, Exception e) {
            super(msg, e);
        }
    }

    /**
     * Parse a ZooKeeper configuration file
     * @param path the patch of the configuration file
     * @throws org.humbird.soa.ipc.go.RpcConfig.ConfigException error processing configuration
     */
    public void parse(String path) throws ConfigException {
        File configFile = new File(path);

        LOG.info("Reading configuration from: " + configFile);

        try {
            if (!configFile.exists()) {
                throw new IllegalArgumentException(configFile.toString()
                        + " file is missing");
            }

            Properties cfg = new Properties();
            FileInputStream in = new FileInputStream(configFile);
            try {
                cfg.load(in);
            } finally {
                in.close();
            }

            parseProperties(cfg);

        } catch (IOException e) {
            throw new ConfigException("Error processing " + path, e);
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Error processing " + path, e);
        }
    }

    public void parseProperties(Properties prop) throws ConfigException {

        Map<Integer, ClusterMemberH.Builder> mems = new HashMap();

        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            String key = entry.getKey().toString().trim();
            String value = entry.getValue().toString().trim();
            if("cluster.name".equals(key)) {
                clusterbuilder.setClusterName(value);
            } else if("cluster.signal".equals(key)) {
                if("unicast".equals(value)) {
                    clusterbuilder.setSignalModel(ClusterH.SinalModelH.UNICAST);
                } else if("multicast".equals(value)) {
                    clusterbuilder.setSignalModel(ClusterH.SinalModelH.MULTICAST);
                } else {
                    throw new ConfigException("Unrecognised signal type : " + value);
                }
            } else if("cluster.id".equals(key)) {
                clusterbuilder.setId(Integer.parseInt(value));
            } else if("local.ip".equals(key)) {
                clusterbuilder.setIp(value);
                local.setIp(value);
            } else if("local.name".equals(key)) {
                local.setServerName(value);
            } else if("local.port".equals(key)) {
                local.setPort(Integer.parseInt(value));
            } else if("master.ip".equals(key)) {
                masterMember.setIp(value);
            } else if("master.port".equals(key)) {
                masterMember.setPort(Integer.parseInt(value));
            } else if("master.name".equals(key)) {
                masterMember.setServerName(value);
            } else if(key.startsWith("slave")) {
                String parts [] = key.split("\\.");
                int num = Integer.parseInt(parts[2]);
                ClusterMemberH.Builder memBuilder = null;
                if(!mems.containsKey(num)) {
                    memBuilder = ClusterMemberH.newBuilder()
                            .setMors(ClusterMemberH.MasterOrSlaveH.SLAVE);
                    mems.put(num, memBuilder);
                } else {
                    memBuilder = mems.get(num);
                }
                if("ip".equals(parts[1])) {
                    memBuilder.setIp(value);
                } else if("port".equals(parts[1])) {
                    memBuilder.setPort(Integer.parseInt(value));
                } else if("name".equals(parts[1])) {
                    memBuilder.setServerName(value);
                }
            }
        }
        if(!masterMember.isInitialized()) {
            throw new ConfigException("Not Master Initialized.");
        } else {
            this.clusterbuilder.addMems(masterMember);
        }

        Iterator<Map.Entry<Integer, ClusterMemberH.Builder>> iterator =  mems.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<Integer, ClusterMemberH.Builder> entry = iterator.next();
            ClusterMemberH.Builder mem = entry.getValue();
            if(!entry.getValue().isInitialized()) {
                throw new ConfigException("Not Slave Initialized.");
            } else {
                this.mems.put(mem.getServerName(), mem);
                clusterbuilder.addMems(mem);
                caches.put(mem.getServerName(), new CacheStat());
            }
        }
        if(local.getServerName().equals(masterMember.getServerName())) {
            initVersion(0);
        } else {
            initVersion(-1);
        }
    }

    private void initVersion(int version) {
        versions.put(ProbeTypeH.CACHE_FEED_BACK, version);
        versions.put(ProbeTypeH.CACHE_NOTIFY, version);
        versions.put(ProbeTypeH.HEARTBEAT, version);
    }

}
