package org.humbird.soa.component.esign.invoke;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 15/4/7.
 */
public abstract class EsignInvoke {

    protected static final Map<String, String> names = new HashMap<String, String>();

    protected static final String NAME = "org.humbird.soa.component.esign.name";
    protected static final String SUCCESS_EXT = "org.humbird.soa.component.esign.success.ext";
    protected static final String FAILED_EXT = "org.humbird.soa.component.esign.failed.ext";
    protected static final String FROM_SCAN_DIR_NAME_URI = "org.humbird.soa.component.esign.from.scan.dir.uri.{code}";
    protected static final String FROM_SCANED_DIR_NAME_URI = "org.humbird.soa.component.esign.from.scaned.dir.uri.{code}";
    protected static final String DAY = "org.humbird.soa.component.esign.day";
    protected static final String ERROR_Dir = "org.humbird.soa.component.esign.error.uri.{code}";
    protected static final String TO_URI = "org.humbird.soa.component.esign.to.uri.{code}";
    protected static final String TEMPLATE_DIRECTORY = "org.humbird.soa.component.esign.tmp.dir.{code}";
    protected static final String SCAN_SUFFIX = "org.humbird.soa.component.esign.scan.suffix";
    protected static final String SCANED_SUFFIX = "org.humbird.soa.component.esign.scaned.suffix";
    protected static final String MATCH = "org.humbird.soa.component.esign.match";
    protected static final String MATCH_TYPE = "org.humbird.soa.component.esign.match.type";
    protected static final String NXML_DATE = "org.humbird.soa.component.esign.nxml.date";
    protected static final String NPDF_DATE = "org.humbird.soa.component.esign.npdf.date";
    protected static final String DELETE_MAPPING_TYPE = "org.humbird.soa.component.esign.delete.mapping.type";
    protected static final String DELETE_MAPPING_DATE = "org.humbird.soa.component.esign.delete.mapping.date";
    protected static final String OUTPUT = "org.humbird.soa.component.esign.output";
    protected static final String DEFAULT_OUTPUT = "org.humbird.soa.common.tools.TPatternLayout";
    protected static final String OUTPUT_TYPE = "org.humbird.soa.component.esign.output.Type";
    protected static final String OUTPUT_PATTERN = "org.humbird.soa.component.esign.output.Pattern";
    protected static final String VAILD_SPLIT = "org.humbird.soa.component.esign.vaild.module.split";
    protected static final String VAILD_HEAD = "org.humbird.soa.component.esign.vaild.module.head";
    protected static final String VAILD_BODY = "org.humbird.soa.component.esign.vaild.module.body";
    protected static final String VAILD_TAIL = "org.humbird.soa.component.esign.vaild.module.tail";
    protected static final String INVAILD_SPLIT = "org.humbird.soa.component.esign.invaild.module.split";
    protected static final String INVAILD_HEAD = "org.humbird.soa.component.esign.invaild.module.head";
    protected static final String INVAILD_BODY = "org.humbird.soa.component.esign.invaild.module.body";
    protected static final String INVAILD_TAIL = "org.humbird.soa.component.esign.invaild.module.tail";

    public abstract void invoke()
            throws Exception;

    public abstract void init()
            throws Exception;

    public abstract void destroy()
            throws Exception;
}
