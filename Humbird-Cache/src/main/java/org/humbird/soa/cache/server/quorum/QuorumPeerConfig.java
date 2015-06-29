/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.humbird.soa.cache.server.quorum;

import org.humbird.soa.cache.server.quorum.flexible.QuorumHierarchical;
import org.humbird.soa.cache.server.quorum.flexible.QuorumMaj;
import org.humbird.soa.cache.server.quorum.flexible.QuorumVerifier;
import org.humbird.soa.db.HumbirdJdbcTemplate.DriverType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class QuorumPeerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(QuorumPeerConfig.class);

    protected InetSocketAddress clientPortAddress;
    protected String dataDir;
    protected String dataLogDir;
    protected int tickTime = 3000;
    protected int maxClientCnxns = 60;
    /** defaults to -1 if not set explicitly */
    protected int minSessionTimeout = -1;
    /** defaults to -1 if not set explicitly */
    protected int maxSessionTimeout = -1;

    protected int initLimit;
    protected int syncLimit;
    protected int electionAlg = 3;
    protected int electionPort = 2182;
    protected final HashMap<Long,QuorumServer> servers =
        new HashMap<Long, QuorumServer>();
    protected final HashMap<Long,QuorumServer> observers =
        new HashMap<Long, QuorumServer>();

    protected long serverId;
    protected HashMap<Long, Long> serverWeight = new HashMap<Long, Long>();
    protected HashMap<Long, Long> serverGroup = new HashMap<Long, Long>();
    protected int numGroups = 0;
    protected QuorumVerifier quorumVerifier;
    protected int snapRetainCount = 3;
    protected int purgeInterval = 0;

    protected LearnerType peerType = LearnerType.PARTICIPANT;
    protected String []springFile;
    protected CacheType cacheType = CacheType.MEMCACHED;
    protected File cacheFile;
    protected DriverType driverType = DriverType.MYSQL;

    /**
     * Minimum snapshot retain count.
     */
    private final int MIN_SNAP_RETAIN_COUNT = 3;

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
     * @throws org.humbird.soa.cache.server.quorum.QuorumPeerConfig.ConfigException error processing configuration
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

    /**
     * Parse config from a Properties.
     * @param zkProp Properties to parse from.
     * @throws java.io.IOException
     * @throws org.humbird.soa.cache.server.quorum.QuorumPeerConfig.ConfigException
     */
    public void parseProperties(Properties zkProp)
    throws IOException, ConfigException {
        int clientPort = 0;
        String clientPortAddress = null;
        for (Entry<Object, Object> entry : zkProp.entrySet()) {
            String key = entry.getKey().toString().trim();
            String value = entry.getValue().toString().trim();
            if (key.equals("dataDir")) {
                dataDir = value;
            } else if (key.equals("dataLogDir")) {
                dataLogDir = value;
            } else if (key.equals("clientPort")) {
                clientPort = Integer.parseInt(value);
            } else if (key.equals("clientPortAddress")) {
                clientPortAddress = value.trim();
            } else if (key.equals("tickTime")) {
                tickTime = Integer.parseInt(value);
            } else if (key.equals("maxClientCnxns")) {
                maxClientCnxns = Integer.parseInt(value);
            } else if (key.equals("minSessionTimeout")) {
                minSessionTimeout = Integer.parseInt(value);
            } else if (key.equals("maxSessionTimeout")) {
                maxSessionTimeout = Integer.parseInt(value);
            } else if (key.equals("initLimit")) {
                initLimit = Integer.parseInt(value);
            } else if (key.equals("syncLimit")) {
                syncLimit = Integer.parseInt(value);
            } else if (key.equals("electionAlg")) {
                electionAlg = Integer.parseInt(value);
            } else if(key.equals("cacheType")) {
                if (value.toLowerCase().equals("memcached")) {
                    cacheType = CacheType.MEMCACHED;
                } else if (value.toLowerCase().equals("redis")) {
                    cacheType = CacheType.REDIS;
                } else {
                    throw new ConfigException("Unrecognised cache type: " + value);
                }
            } else if(key.equals("cacheFile")) {
                cacheFile = new File(value);
                if(!cacheFile.exists()) {
                    throw new ConfigException("not exsist cache file: " + value);
                }
            } else if(key.equals("springFile")) {
                springFile = value.split(",");
            } else if(key.equals("driverType")) {
                if (value.toLowerCase().equals("mysql")) {
                    driverType = DriverType.MYSQL;
                } else if (value.toLowerCase().equals("oracle")) {
                    driverType = DriverType.ORACLE;
                } else {
                    throw new ConfigException("Unrecognised driver type: " + value);
                }
            } else if (key.equals("peerType")) {
                if (value.toLowerCase().equals("observer")) {
                    peerType = LearnerType.OBSERVER;
                } else if (value.toLowerCase().equals("participant")) {
                    peerType = LearnerType.PARTICIPANT;
                } else {
                    throw new ConfigException("Unrecognised peertype: " + value);
                }
            } else if (key.equals("autopurge.snapRetainCount")) {
                snapRetainCount = Integer.parseInt(value);
            } else if (key.equals("autopurge.purgeInterval")) {
                purgeInterval = Integer.parseInt(value);
            } else if (key.startsWith("server.")) {
                int dot = key.indexOf('.');
                long sid = Long.parseLong(key.substring(dot + 1));
                String parts[] = value.split(":");
                if ((parts.length != 2) && (parts.length != 3) && (parts.length !=4)) {
                    LOG.error(value
                       + " does not have the form host:port or host:port:port " +
                       " or host:port:port:type");
                }
                InetSocketAddress addr = new InetSocketAddress(parts[0],
                        Integer.parseInt(parts[1]));
                if (parts.length == 2) {
                    servers.put(Long.valueOf(sid), new QuorumServer(sid, addr));
                } else if (parts.length == 3) {
                    InetSocketAddress electionAddr = new InetSocketAddress(
                            parts[0], Integer.parseInt(parts[2]));
                    servers.put(Long.valueOf(sid), new QuorumServer(sid, addr,
                            electionAddr));
                } else if (parts.length == 4) {
                    InetSocketAddress electionAddr = new InetSocketAddress(
                            parts[0], Integer.parseInt(parts[2]));
                    LearnerType type = LearnerType.PARTICIPANT;
                    if (parts[3].toLowerCase().equals("observer")) {
                        type = LearnerType.OBSERVER;
                        observers.put(Long.valueOf(sid), new QuorumServer(sid, addr,
                                electionAddr,type));
                    } else if (parts[3].toLowerCase().equals("participant")) {
                        type = LearnerType.PARTICIPANT;
                        servers.put(Long.valueOf(sid), new QuorumServer(sid, addr,
                                electionAddr,type));
                    } else {
                        throw new ConfigException("Unrecognised peertype: " + value);
                    }
                }
            } else if (key.startsWith("group")) {
                int dot = key.indexOf('.');
                long gid = Long.parseLong(key.substring(dot + 1));

                numGroups++;

                String parts[] = value.split(":");
                for(String s : parts){
                    long sid = Long.parseLong(s);
                    if(serverGroup.containsKey(sid))
                        throw new ConfigException("Server " + sid + "is in multiple groups");
                    else
                        serverGroup.put(sid, gid);
                }

            } else if(key.startsWith("weight")) {
                int dot = key.indexOf('.');
                long sid = Long.parseLong(key.substring(dot + 1));
                serverWeight.put(sid, Long.parseLong(value));
            } else {
                System.setProperty("zookeeper." + key, value);
            }
        }
        
        // Reset to MIN_SNAP_RETAIN_COUNT if invalid (less than 3)
        // PurgeTxnLog.purge(File, File, int) will not allow to purge less
        // than 3.
        if (snapRetainCount < MIN_SNAP_RETAIN_COUNT) {
            LOG.warn("Invalid autopurge.snapRetainCount: " + snapRetainCount
                    + ". Defaulting to " + MIN_SNAP_RETAIN_COUNT);
            snapRetainCount = MIN_SNAP_RETAIN_COUNT;
        }

        if (dataDir == null) {
            throw new IllegalArgumentException("dataDir is not set");
        }
        if (dataLogDir == null) {
            dataLogDir = dataDir;
        } else {
            if (!new File(dataLogDir).isDirectory()) {
                throw new IllegalArgumentException("dataLogDir " + dataLogDir
                        + " is missing.");
            }
        }
        if (clientPort == 0) {
            throw new IllegalArgumentException("clientPort is not set");
        }
        if (clientPortAddress != null) {
            this.clientPortAddress = new InetSocketAddress(
                    InetAddress.getByName(clientPortAddress), clientPort);
        } else {
            this.clientPortAddress = new InetSocketAddress(clientPort);
        }

        if (tickTime == 0) {
            throw new IllegalArgumentException("tickTime is not set");
        }
        if (minSessionTimeout > maxSessionTimeout) {
            throw new IllegalArgumentException(
                    "minSessionTimeout must not be larger than maxSessionTimeout");
        }
        if (servers.size() == 0) {
            if (observers.size() > 0) {
                throw new IllegalArgumentException("Observers w/o participants is an invalid configuration");
            }
            // Not a quorum configuration so return immediately - not an error
            // case (for b/w compatibility), server will default to standalone
            // mode.
            return;
        } else if (servers.size() == 1) {
            if (observers.size() > 0) {
                throw new IllegalArgumentException("Observers w/o quorum is an invalid configuration");
            }

            // HBase currently adds a single server line to the config, for
            // b/w compatibility reasons we need to keep this here.
            LOG.error("Invalid configuration, only one server specified (ignoring)");
            servers.clear();
        } else if (servers.size() > 1) {
            if (servers.size() == 2) {
                LOG.warn("No server failure will be tolerated. " +
                    "You need at least 3 servers.");
            } else if (servers.size() % 2 == 0) {
                LOG.warn("Non-optimial configuration, consider an odd number of servers.");
            }
            if (initLimit == 0) {
                throw new IllegalArgumentException("initLimit is not set");
            }
            if (syncLimit == 0) {
                throw new IllegalArgumentException("syncLimit is not set");
            }
            /*
             * If using FLE, then every server requires a separate election
             * port.
             */
            if (electionAlg != 0) {
                for (QuorumServer s : servers.values()) {
                    if (s.electionAddr == null)
                        throw new IllegalArgumentException(
                                "Missing election port for server: " + s.id);
                }
            }

            /*
             * Default of quorum config is majority
             */
            if(serverGroup.size() > 0){
                if(servers.size() != serverGroup.size())
                    throw new ConfigException("Every server must be in exactly one group");
                /*
                 * The deafult weight of a server is 1
                 */
                for(QuorumServer s : servers.values()){
                    if(!serverWeight.containsKey(s.id))
                        serverWeight.put(s.id, (long) 1);
                }

                /*
                 * Set the quorumVerifier to be QuorumHierarchical
                 */
                quorumVerifier = new QuorumHierarchical(numGroups,
                        serverWeight, serverGroup);
            } else {
                /*
                 * The default QuorumVerifier is QuorumMaj
                 */

                LOG.info("Defaulting to majority quorums");
                quorumVerifier = new QuorumMaj(servers.size());
            }

            // Now add observers to servers, once the quorums have been
            // figured out
            servers.putAll(observers);
    
            File myIdFile = new File(dataDir, "myid");
            if (!myIdFile.exists()) {
                throw new IllegalArgumentException(myIdFile.toString()
                        + " file is missing");
            }
            BufferedReader br = new BufferedReader(new FileReader(myIdFile));
            String myIdString;
            try {
                myIdString = br.readLine();
            } finally {
                br.close();
            }
            try {
                serverId = Long.parseLong(myIdString);
                MDC.put("myid", myIdString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("serverid " + myIdString
                        + " is not a number");
            }
            
            // Warn about inconsistent peer type
            LearnerType roleByServersList = observers.containsKey(serverId) ? LearnerType.OBSERVER
                    : LearnerType.PARTICIPANT;
            if (roleByServersList != peerType) {
                LOG.warn("Peer type from servers list (" + roleByServersList
                        + ") doesn't match peerType (" + peerType
                        + "). Defaulting to servers list.");
    
                peerType = roleByServersList;
            }
        }
    }

    public InetSocketAddress getClientPortAddress() { return clientPortAddress; }
    public String getDataDir() { return dataDir; }
    public String getDataLogDir() { return dataLogDir; }
    public int getTickTime() { return tickTime; }
    public int getMaxClientCnxns() { return maxClientCnxns; }
    public int getMinSessionTimeout() { return minSessionTimeout; }
    public int getMaxSessionTimeout() { return maxSessionTimeout; }

    public int getInitLimit() { return initLimit; }
    public int getSyncLimit() { return syncLimit; }
    public int getElectionAlg() { return electionAlg; }
    public int getElectionPort() { return electionPort; }    
    
    public int getSnapRetainCount() {
        return snapRetainCount;
    }

    public int getPurgeInterval() {
        return purgeInterval;
    }

    public QuorumVerifier getQuorumVerifier() {   
        return quorumVerifier;
    }

    public Map<Long,QuorumServer> getServers() {
        return Collections.unmodifiableMap(servers);
    }

    public long getServerId() { return serverId; }

    public boolean isDistributed() { return servers.size() > 1; }

    public LearnerType getPeerType() {
        return peerType;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public String[] getSpringFile() {
        return springFile;
    }

    public static class QuorumServer {
        public QuorumServer(long id, InetSocketAddress addr,
                            InetSocketAddress electionAddr) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = electionAddr;
        }

        public QuorumServer(long id, InetSocketAddress addr) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = null;
        }

        public QuorumServer(long id, InetSocketAddress addr,
                            InetSocketAddress electionAddr, LearnerType type) {
            this.id = id;
            this.addr = addr;
            this.electionAddr = electionAddr;
            this.type = type;
        }

        public InetSocketAddress addr;

        public InetSocketAddress electionAddr;

        public long id;

        public LearnerType type = LearnerType.PARTICIPANT;
    }

    public enum ServerState {
        LOOKING, FOLLOWING, LEADING, OBSERVING
    }

    /*
     * A peer can either be participating, which implies that it is willing to
     * both vote in instances of consensus and to elect or become a Leader, or
     * it may be observing in which case it isn't.
     *
     * We need this distinction to decide which ServerState to move to when
     * conditions change (e.g. which state to become after LOOKING).
     */
    public enum LearnerType {
        PARTICIPANT, OBSERVER
    }

    /*
     * To enable observers to have no identifier, we need a generic identifier
     * at least for QuorumCnxManager. We use the following constant to as the
     * value of such a generic identifier.
     */
    static final long OBSERVER_ID = Long.MAX_VALUE;

    public enum CacheType {
        REDIS, MEMCACHED
    }

    /*
     * Record leader election time
     */
    public long start_fle, end_fle;

    /*
     * Default value of peer is participant
     */
    private LearnerType learnerType = LearnerType.PARTICIPANT;

    public LearnerType getLearnerType() {
        return learnerType;
    }

    /**
     * Sets the LearnerType both in the QuorumPeer and in the peerMap
     */
    public void setLearnerType(LearnerType p) {
        learnerType = p;
        if (quorumPeers.containsKey(this.myid)) {
            this.quorumPeers.get(myid).type = p;
        } else {
            LOG.error("Setting LearnerType to " + p + " but " + myid
                    + " not in QuorumPeers. ");
        }

    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    /**
     * The servers that make up the cluster
     */
    protected Map<Long, QuorumServer> quorumPeers;

    /**
     * My id
     */
    private long myid;


    /**
     * get the id of this quorum peer.
     */
    public long getId() {
        return myid;
    }

    public void setMyid(long myid) {
        this.myid = myid;
    }
}
