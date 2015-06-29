/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.humbird.soa.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Util class for {@link java.net.InetAddress}
 */
public final class InetAddressUtil {

    private InetAddressUtil() {
        // util class
    }

    /**
     * When using the {@link java.net.InetAddress#getHostName()} method in an
     * environment where neither a proper DNS lookup nor an <tt>/etc/hosts</tt>
     * entry exists for a given host, the following exception will be thrown:
     * <p/>
     * <code>
     * java.net.UnknownHostException: &lt;hostname&gt;: &lt;hostname&gt;
     * at java.net.InetAddress.getLocalHost(InetAddress.java:1425)
     * ...
     * </code>
     * <p/>
     * Instead of just throwing an UnknownHostException and giving up, this
     * method grabs a suitable hostname from the exception and prevents the
     * exception from being thrown. If a suitable hostname cannot be acquired
     * from the exception, only then is the <tt>UnknownHostException</tt> thrown.
     *
     * @return the hostname
     * @throws java.net.UnknownHostException is thrown if hostname could not be resolved
     */
    public static String getLocalHostName() throws UnknownHostException {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage(); // host = "hostname: hostname"
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            throw uhe;
        }
    }

    /**
     * if error return < 0
     * else return long
     *
     * @param ip
     * @return
     */
    public static long convertIpv4ToLong(String ip) throws NumberFormatException {
        long []ips = new long[4];
        int []pos = new int[4];
        pos[0] = ip.indexOf('.');
        pos[1] = ip.indexOf('.', pos[0] + 1);
        pos[2] = ip.indexOf('.', pos[1] + 1);
        pos[3] = pos[2] + 1;
        for(int i=0; i<4; i++) {
            ips[i] = Long.parseLong(ip.substring(i == 0 ? i : pos[i-1] + 1, i == 3 ? ip.length() : pos[i]));
        }
        return (ips[0] << 24) + (ips[1]<<16) + (ips[2]<<8) + ips[3];
    }

    /**
     * if error return null
     *
     * @param ip
     * @return
     */
    public static String convertLongToIpv4(long ip) {
        long ips[] = new long[4];
        ips[0] = ip >>> 24; //  右移时高位补0
        ips[1] = (ip & 0x00FFFFFF) >>> 16;  //  整数高8位置0
        ips[2] = (ip & 0x0000FFFF) >>> 8;  //  整数高16位置0
        ips[3] = ip & 0x000000FF;  //  整数高24位置0
        StringBuffer stringBuffer = new StringBuffer("");
        for(int i=0; i<4; i++) {
            stringBuffer.append(ips[i]).append(i < 3 ? "." : "");
        }
        return stringBuffer.toString();
    }

}
