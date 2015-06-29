package org.humbird.soa.common.net.ftp;

import com.jcraft.jsch.ChannelSftp;

import java.util.List;

/**
 * Created by david on 15/3/10.
 */
public interface FTPCallback extends ChannelSftp.LsEntrySelector {

    int select(FTPFile file);

    boolean any(Object object);

    List<StringBuilder> getBuf();

    int getCounts();

    boolean isFlag();
}
