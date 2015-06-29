package org.humbird.soa.common.compress;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * https://github.com/orgs/Asiainfo-ODC
 *
 * Created by david on 15/1/3.
 */
public interface ICompress {

    /* 压缩2进制文件流, 获得压缩后的2进制文件流 */
    byte[] compress(byte[] bytes) throws IOException;

    List<File> compress(String sourceFilePath) throws CompressException;

    List<File> compress(File sourceFile) throws CompressException;

    List<File> compress(List<File> sourceFiles) throws CompressException;

    List<File> compress(List<File> sourceFiles, Boolean delete) throws CompressException;

    List<File> compress(String sourceFilePath, boolean delete) throws CompressException;

    List<File> compress(File sourceFile, boolean delete) throws CompressException;

    byte[] uncompress(byte[] bytes) throws IOException;

    List<File> uncompress(String sourceFilePath) throws CompressException;

    List<File> uncompress(File sourceFile) throws CompressException;

    List<File> uncompress(List<File> sourceFiles) throws CompressException;

    List<File> uncompress(String sourceFilePath, boolean delete) throws CompressException;

    List<File> uncompress(File sourceFile, boolean delete) throws CompressException;

    List<File> uncompress(List<File> sourceFiles, Boolean delete) throws CompressException;
}
