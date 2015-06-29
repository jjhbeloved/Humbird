package org.humbird.soa.common.compress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * https://github.com/orgs/Asiainfo-ODC
 * 此接口提供 文件/目录 归档/解归档
 * 保证为事务级别处理
 *
 * Created by david on 15/1/3.
 */
public interface IArchive {
    /**
     * 默认删除源文件
     *
     * @param sourceFiles 源文件对象
     * @param targetDirectory 目标所在目录
     * @param targetFileName 目标文件名
     * @return 压缩文件全路径
     * @throws java.io.FileNotFoundException 文件不存在
     * @throws org.humbird.soa.common.compress.CompressException 压缩文件异常
     */
    String archive(List<File> sourceFiles, String targetDirectory, String targetFileName) throws FileNotFoundException, CompressException;


    /**
     * 选择是否删除源文件
     *
     * @param sourceFiles 源文件对象
     * @param targetDirectory 目标所在目录
     * @param targetFileName 目标文件名
     * @param act 压缩后动作
     * @return 压缩文件全路径
     * @throws java.io.FileNotFoundException 文件不存在
     * @throws org.humbird.soa.common.compress.CompressException 压缩文件异常
     */
    String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act) throws FileNotFoundException, CompressException;

    String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act, boolean isLock) throws FileNotFoundException, CompressException;

    /* 默认压缩成如文件名 */
    void archive(String sourceFilePath) throws IOException;

    /* 默认压缩成源文件名 */
    void archive(File sourceFile) throws IOException;

    /* 将源文件/目录压缩到目标文件 */
    void archive(File sourceFile, File targetFile) throws IOException;

    /* 将源文件/目录压缩到目标文件 */
    void archive(String sourceFilePath, String targetFilePath) throws IOException;


    /* 默认删除源文件 */
    List<File> dearchive(List<File> files, File targetDir) throws IOException, CompressException;

    /* 选择是否删除源文件 */
    List<File> dearchive(List<File> files, File targetDir, boolean act) throws IOException, CompressException;

    /* 默认解压成源文件名, 返回解压出来所有文件队 */
    List<File> dearchive(String sourceFilePath) throws IOException, CompressException;

    /* 默认解压成源文件名, 返回解压出来所有文件队列 */
    List<File> dearchive(File sourceFile) throws IOException, CompressException;

    /* 将源文件/目录解压到目标目录 */
    List<File> dearchive(File sourceFile, File targetDirectory) throws IOException, CompressException;

    List<File> dearchive(String sourceFilePath, String targetDirectoryPath) throws IOException, CompressException;

}
