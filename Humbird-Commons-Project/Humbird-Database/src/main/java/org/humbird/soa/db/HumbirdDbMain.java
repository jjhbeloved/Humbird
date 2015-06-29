package org.humbird.soa.db;

import org.humbird.soa.common.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 15/6/6.
 */
public class HumbirdDbMain {
    private static final Logger LOG = LoggerFactory.getLogger(HumbirdDbMain.class);

    private static final String USAGE = "Usage: HumbirdDbMain configfile";

    public static void main(String[] args) {
        HumbirdDbMain humbirdDbMain = new HumbirdDbMain();
        try {
            humbirdDbMain.initializeAndRun(args);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid arguments, exiting abnormally", e);
            LOG.info(USAGE);
            System.err.println(USAGE);
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Unexpected exception, exiting abnormally", e);
            System.exit(1);
        }
    }

    protected void initializeAndRun(String[] args) throws IOException {
        SpringUtils.init(new String[] {"classpath:/spring/spring-datasource.xml"});

        HumbirdDbManager humbirdDbManager = (HumbirdDbManager) SpringUtils.get().getBean("humbirdDbManager");
        System.out.println(humbirdDbManager.getJdbcTemplate().getQueryTimeout());
        List<Map<String, Object>> maps = humbirdDbManager.getJdbcTemplate().queryForList("select 1 from dual");
        System.out.println(maps.size());
        System.out.println(maps.get(0).get("1"));
    }
}
