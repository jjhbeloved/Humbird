package org.humbird.soa.db;

import org.humbird.soa.common.utils.SpringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by david on 15/6/6.
 */
public class HumbirdJdbcTemplate extends JdbcTemplate {

    private String datasourceType;

    public void setDatasourceType(String datasourceType) {
        if(datasourceType.toLowerCase().equals("c3p0")) {
            this.setDataSource((DataSource) SpringUtils.get().getBean("dataSourceC3p0"));
        } else if (datasourceType.toLowerCase().equals("jdbc")) {
            this.setDataSource((DataSource) SpringUtils.get().getBean("dataSourceJdbc"));
        } else if (datasourceType.toLowerCase().equals("dbcp")) {
            this.setDataSource((DataSource) SpringUtils.get().getBean("dataSourceDbcp"));
        } else {
            logger.error("Unrecognised database tools type: " + datasourceType);
            System.exit(1);
        }
        this.datasourceType = datasourceType;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public static enum DatabaseToolsType {
        C3P0, JDBC, DBCP;
    }

    public static enum DriverType {
        MYSQL, ORACLE;
    }
}
