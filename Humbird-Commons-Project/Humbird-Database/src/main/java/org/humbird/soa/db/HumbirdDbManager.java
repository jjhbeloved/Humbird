package org.humbird.soa.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 15/6/6.
 */
public class HumbirdDbManager {

    private final static Logger logger = LoggerFactory.getLogger(HumbirdDbManager.class);

    private HumbirdJdbcTemplate jdbcTemplate;

    public HumbirdJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(HumbirdJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
