package org.humbird.soa.common.io;

import java.io.IOException;

/**
 * Created by david on 15/6/4.
 */
public interface Record {

    public void serialize(OutputArchive archive, String tag)
            throws IOException;

    public void deserialize(InputArchive archive, String tag)
            throws IOException;
}
