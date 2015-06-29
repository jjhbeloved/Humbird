package org.humbird.soa.core.invoke;

import org.humbird.soa.core.model.ProtocolModel;

/**
 * Created by david on 15/3/22.
 */
public interface Done {

    void initial() throws Exception;

    void invoke(ProtocolModel protocolModel) throws Exception;

    void refresh() throws Exception;

    void destory() throws Exception;
}
