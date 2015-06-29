import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ClusterH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ClusterMemberH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeH;
import org.humbird.soa.ipc.protoc.vo.ClusterSlaveH.ProbeReplyH;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by david on 15/6/7.
 */
public class T {
    @Test
    public void t1() throws UnknownHostException {

        int master_port = 37271;
        String master_ip = "192.168.137.162";
        String masterServerName = "master_humbird";
        String m_version = "v1.0.0";

        int slave_port = 37272;
        String slave_ip = "192.168.137.162";
        String slaveServerName = "slave_humbird";
        String s_version = "v1.0.0";

        String clusterName = "cluster_humbird";
        int id = 1;

        ClusterH.Builder clusterbuilder = ClusterH.newBuilder();
        ClusterMemberH.Builder membuilder1 = ClusterMemberH.newBuilder();
        ClusterMemberH.Builder membuilder2 = ClusterMemberH.newBuilder();
        ProbeH.Builder probebuilder1 = ProbeH.newBuilder();
        ProbeH.Builder probebuilder2 = ProbeH.newBuilder();
        ProbeReplyH.Builder replybuilder = ProbeReplyH.newBuilder();

        // cluster member builder
        membuilder1.setIp(master_ip)
                .setMors(ClusterMemberH.MasterOrSlaveH.MASTER)
                .setPort(master_port)
                .setServerName(masterServerName);
        membuilder2.setIp(slave_ip)
                .setMors(ClusterMemberH.MasterOrSlaveH.MASTER)
                .setPort(slave_port)
                .setServerName(slaveServerName);
        // probe builder
        probebuilder1.setIp(master_ip)
                .setId(id)
                .setLastSignal(System.currentTimeMillis())
                .setServerName(masterServerName)
                .setProbeMessage(m_version)
                .setVersion(1)
                .setType(ProbeH.ProbeTypeH.HEARTBEAT);
        probebuilder2.setIp(slave_ip)
                .setId(id)
                .setLastSignal(System.currentTimeMillis())
                .setServerName(slaveServerName)
                .setProbeMessage(m_version)
                .setVersion(1)
                .setType(ProbeH.ProbeTypeH.HEARTBEAT);
        // cluster builder
        clusterbuilder.setClusterName(clusterName)
                .setId(id)
                .setIp(master_ip)
                .setServerName(masterServerName)
                .setMemCounts(2)
                .setProbeCounts(2)
                .addMems(membuilder1)
                .addMems(membuilder2)
                .addProbe(probebuilder1)
                .addProbe(probebuilder2)
                .setSignalModel(ClusterH.SinalModelH.UNICAST);

        p(clusterbuilder.build());
        p(InetAddress.getLocalHost().getHostName());

    }

    @Test
    public void t2() {

    }

    public static void p(Object o) {
        System.out.println(o);
    }
}
