package org.humbird.soa.component.esign.log;

import org.humbird.soa.common.tools.TTimestamp;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by david on 15/4/7.
 */
public class LogProxy
{
    public static final Map<String, String> systems = new HashMap();
    public static final String TIME = "time";
    public static final String USER = "user.name";
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String HOSTNAME = "hostname";
    public static final String APP_NAME = "user.home";
    public static final String APP_PATH = "user.dir";
    public static final String PID = "pid";
    public static final String INIT_PARAMETER = "user.home";
    public static final String RESULT = "user.home";
    public static final String HEAP = "heap";
    public static final String NOHEAP = "no-heap";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    public static String assembly()
    {
        return null;
    }

    public static String assembly(String system_name, String method_name, String status, UUID global, UUID single, String message) {
        systems.put("time", TTimestamp.getUTCDate("yyyy'-'MM'-'dd'T'hh':'mm':'ss'.'sss"));
        StringBuilder result = new StringBuilder((String)systems.get("time"));
        result.append(assemblySpec((String)systems.get("user.home"))).append(assemblySpec(method_name)).append(assemblySpec(status));
        result.append(assemblySpec(system_name)).append(assemblySpec(global.toString())).append(assemblySpec(single.toString()));
        result.append(assemblySpec(message)).append(Thread.currentThread().getName());
        return result.toString();
    }

    private static String assemblySpec(String param) {
        return "|" + param;
    }

    static
    {
        String bean = ManagementFactory.getRuntimeMXBean().getName();
        String[] params = bean.split("@");
        systems.put("pid", params[0]);
        systems.put("hostname", params[1]);
        systems.put("user.dir", System.getProperty("user.dir"));
        systems.put("user.name", System.getProperty("user.name"));
        String projectName = System.getProperty("user.dir");
        systems.put("user.home", projectName.substring(projectName.lastIndexOf(File.separator) + 1));
    }
}