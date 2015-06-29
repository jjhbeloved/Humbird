package org.humbird.soa.common.compress;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://github.com/orgs/Asiainfo-ODC
 *
 * Created by david on 14-9-17.
 */
public class TarArchive extends CompressUtils implements IArchive {

    private final static Logger LOG = LoggerFactory.getLogger(TarArchive.class);

    /**
     * 压缩tar包
     *
     * @param srcFiles   源文件(目录/文件)
     * @param targetDir  目标路径
     * @param targetName 压缩文件名
     * @param dels       删除文件目录前缀
     * @param act        压缩后对文件的操作
     */
    @Deprecated
    public String archive(List<File> srcFiles, String targetDir, String targetName, List<String> dels, boolean act) throws CompressException {
        if (srcFiles == null || srcFiles.size() == 0) {
            throw new CompressException("Array files must be not null.");
        }
        try {
            validateTemplateFile(targetDir, targetName);
        } catch (IOException e) {
            throw new CompressException(e.getMessage());
        }
        String tmpTar = targetDir + File.separator + targetName + TAR_EXT;

        File tarFile = new File(tmpTar + SUFFIX);
        if (tarFile.exists()) {
            if(!tarFile.delete()) {
                LOG.trace("Delete file {} failed", tarFile.getName());
            }
        }

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        TarArchiveOutputStream tos = null;
        try {
            fos = new FileOutputStream(tarFile);
            tos = new TarArchiveOutputStream(new BufferedOutputStream(fos));
            byte[] bufs = new byte[BUFFEREDSIZE * 10];
            for (int i = 0; i < srcFiles.size(); i++) {
                File file = srcFiles.get(i);
                writeTar(file, dels, fos, fis, tos, bis, bufs, tarFile.getName());
            }
            tos.flush();
            File tarFile2 = new File(tmpTar);
            if (tarFile2.exists()) {
                if(!tarFile2.delete()) {
                    LOG.trace("Delete file {} failed", tarFile2.getName());
                }
            }
            if(!tarFile.renameTo(tarFile2)) {
                throw new CompressException("Rename" + tarFile.getName() + " to " + tarFile2.getName() + " failed");
            }
            if (act) {
                for (int i = 0, size = srcFiles.size(); i < size; i++) {
                    File file = srcFiles.get(i);
                    delFiles(file, tarFile2.getPath());
                }
            }
        } catch (IOException e) {
            if(!tarFile.delete()) {
                LOG.trace("Delete file {} failed", tarFile.getName());
            }
            throw new CompressException(e.getMessage());
        } finally {
            // 关闭流
            if (null != fos)
            try {
                fos.close();
            } catch (IOException e) {
            }
            if(null != tos) {
                try {
                    tos.closeArchiveEntry();
                } catch (IOException e) {

                } finally {
                    try {
                        tos.close();
                    } catch (IOException e) {
                    }
                }
            }
            if(null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if(null != bis) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return tmpTar;
    }

    /**
     * 遍历压缩文件
     *
     * @param file
     * @param dels
     * @param fos
     * @param fis
     * @param tos
     * @param bis
     * @param bufs
     * @param tarName
     */
    private void writeTar(File file, List<String> dels, FileOutputStream fos, FileInputStream fis, TarArchiveOutputStream tos, BufferedInputStream bis, byte[] bufs, String tarName) throws IOException {
        if (file.isDirectory()) {
            File []files = file.listFiles();
            if(files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    writeTar(files[i], dels, fos, fis, tos, bis, bufs, tarName);
                }
            }
        } else {
            if (tarName.equals(file.getName())) return;
            // 创建Tar实体,并添加进压缩包
            String tmp = getEntryName(file, dels);
            TarArchiveEntry tarEntry = new TarArchiveEntry(tmp);
            tarEntry.setSize(file.length());
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            tos.putArchiveEntry(tarEntry);
            // 读取待压缩的文件并写进压缩包里
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, BUFFEREDSIZE * 10);
            int read = 0;
            while ((read = bis.read(bufs, 0, BUFFEREDSIZE * 10)) != -1) {
                tos.write(bufs, 0, read);
            }
            tos.flush();
            tos.closeArchiveEntry();
        }
    }

    /**
     * 归档
     *
     * @param srcFile  源路径
     * @param taos     TarArchiveOutputStream
     * @param basePath 归档包内相对路径
     * @param keep     目标文件绝对路径
     */
    private void archive(File srcFile, TarArchiveOutputStream taos,
                                String basePath, String keep) throws IOException {
        if (srcFile.isDirectory()) {
            archiveDir(srcFile, taos, basePath, keep);
        } else {
            archiveFile(srcFile, taos, basePath, keep);
        }
    }

    /**
     * 目录归档
     *
     * @param dir      归档目录
     * @param taos     TarArchiveOutputStream
     * @param basePath
     */
    private void archiveDir(File dir, TarArchiveOutputStream taos,
                                   String basePath, String keep) throws IOException {
        File[] files = dir.listFiles();
        TarArchiveEntry entry = new TarArchiveEntry(basePath
                + dir.getName() + PATH);
        taos.putArchiveEntry(entry);
        taos.flush();
        taos.closeArchiveEntry();
        if(files != null && files.length > 0) {
            for (int i=0, size = files.length; i<size; i++) {
                archive(files[i], taos, basePath + dir.getName() + PATH, keep);
            }
        }
    }

    /**
     * 数据归档
     *
     * @param file 待归档文件
     * @param dir  归档目录
     * @param taos TarArchiveOutputStream
     */
    private void archiveFile(File file, TarArchiveOutputStream taos,
                                    String dir, String keep) throws IOException {
        /**
         * 归档内文件名定义
         *
         * <pre>
         * 如果有多级目录，那么这里就需要给出包含目录的文件名
         * 如果用WinRAR打开归档包，中文名将显示为乱码
         * </pre>
         */

        if (keep.equals(file.getPath())) return;
        TarArchiveEntry entry = new TarArchiveEntry(dir + file.getName());
        entry.setSize(file.length());
//        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        BufferedInputStream bis = null;
        int count;
        byte data[] = new byte[BUFFEREDSIZE * 10];
        try {
            taos.putArchiveEntry(entry);
            bis = new BufferedInputStream(new FileInputStream(
                    file));
            while ((count = bis.read(data, 0, BUFFEREDSIZE * 10)) != -1) {
                taos.write(data, 0, count);
            }
            taos.flush();
            taos.closeArchiveEntry();
        } finally {
            // 关闭流
            try {
                if (null != bis) bis.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 文件 解归档
     *
     * @param destFile 目标文件
     * @param tais     ZipInputStream
     */
    private List<File> dearchive(File destFile, TarArchiveInputStream tais) throws IOException {
        List<File> files = new ArrayList<File>();
        TarArchiveEntry entry = null;
        while ((entry = tais.getNextTarEntry()) != null) {
            // 文件
            String dir = destFile.getPath() + File.separator + entry.getName();
            File dirFile = new File(dir + SUFFIX);
            // 文件检查
            fileProber(dirFile);
            if (entry.isDirectory()) {
                File dirs = new File(dir);
                if(!dirFile.renameTo(dirs)) {
                    LOG.trace("Rename {} to {} failed", new Object[]{ dirFile.getName(), dirs.getName()});
                }
                if(!dirFile.mkdirs()) {
                    LOG.trace("Mkdir {} failed", dirFile.getName());
                }
                continue;
            } else {
                try {
                    dearchiveFile(dirFile, tais);
                } catch (IOException e) {   // 保证不留垃圾文件
                    if(!dirFile.delete()) {
                        LOG.trace("Delete file {} failed", dirFile.getName());
                    }
                    for (File file : files) {
                        delFiles(file);
                    }
                    throw e;
                }
                File dirs = new File(dir);
                if(!dirFile.renameTo(dirs)) {
                    LOG.trace("Rename {} to {} failed", new Object[]{ dirFile.getName(), dirs.getName()});
                }
            }
            files.add(new File(dir));
        }
        return files;
    }

    /**
     * 文件解归档
     *
     * @param destFile 目标文件
     * @param tais     TarArchiveInputStream
     * @throws java.io.IOException
     */
    private void dearchiveFile(File destFile, TarArchiveInputStream tais)
            throws IOException {

        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(destFile));
        int count;
        byte data[] = new byte[BUFFEREDSIZE * 10];
        while ((count = tais.read(data, 0, BUFFEREDSIZE * 10)) != -1) {
            bos.write(data, 0, count);
        }
        bos.flush();
        bos.close();
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName) throws FileNotFoundException, CompressException {
        return archive(sourceFiles, targetDirectory, targetFileName, false);
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act) throws FileNotFoundException, CompressException {
        return archive(sourceFiles, targetDirectory, targetFileName, act, false);
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act, boolean isLock) throws FileNotFoundException, CompressException {
        try {
            validateTemplateFile(targetDirectory, targetFileName);
        } catch (IOException e) {
            throw new CompressException(e.getMessage());
        }
        String destPath = targetDirectory + PATH + targetFileName + TAR_EXT;
        File tarFile = new File(destPath + SUFFIX);

        TarArchiveOutputStream taos = null;
        try {
            taos = new TarArchiveOutputStream(
                    new FileOutputStream(tarFile));
            for (File file : sourceFiles) {
                archive(file, taos, BASE_DIR, tarFile.getPath());
            }
            taos.flush();
        } catch (IOException e) {
            if(!tarFile.delete()) {
                LOG.trace("Delete file {} failed", tarFile.getName());
            }
            throw new CompressException(e.getMessage());
        } finally {
            // 关闭流
            if (null != taos) {
                try {
                    taos.closeArchiveEntry();
                } catch (IOException e) {

                } finally {
                    try {
                        taos.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        File targetFile2 = new File(destPath);
        if(!tarFile.renameTo(targetFile2)) {
            throw new CompressException("Rename " + tarFile.getName() + " to " + targetFile2.getName() + " failed.");
        }

        if (act) {
            for(int i=0, size=sourceFiles.size(); i<size; i++) {
                delFiles(sourceFiles.get(i), destPath);
            }
        }
        return destPath;
    }

    @Override
    public void archive(String sourceFilePath) throws IOException {
        archive(new File(sourceFilePath));
    }

    @Override
    public void archive(File sourceFile) throws IOException {
        String name = sourceFile.getName();
        String basePath = sourceFile.getParent();
        String destPath = basePath + File.separator + name + TAR_EXT;
        archive(sourceFile, new File(destPath));
    }

    @Override
    public void archive(File sourceFile, File targetFile) throws IOException {
        File tarFile = new File(targetFile.getAbsolutePath() + SUFFIX);
        TarArchiveOutputStream taos = new TarArchiveOutputStream(
                new FileOutputStream(targetFile));
        try {
            archive(sourceFile, taos, BASE_DIR, targetFile.getPath());
            taos.flush();
            taos.closeArchiveEntry();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                taos.close();
            } catch (IOException e) {
            }
        }
        if(!tarFile.renameTo(targetFile)) {
            throw new IOException("Rename " + tarFile.getName() + " to " + targetFile.getName() + " failed.");
        }
    }

    @Override
    public void archive(String sourceFilePath, String targetFilePath) throws IOException {
        archive(new File(sourceFilePath), new File(targetFilePath));
    }

    @Override
    public List<File> dearchive(List<File> files, File targetDir) throws IOException, CompressException {
        if (files == null || files.size() == 0) {
            throw new CompressException("Array files must be not null.");
        }
        List<File> files1 = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) {
                File []farray = file.listFiles();
                if(farray != null) {
                    files1.addAll(dearchive(Arrays.asList(farray), targetDir));
                }
            } else {
                if (targetDir == null) {
                    files1.addAll(dearchive(file));
                } else {
                    files1.addAll(dearchive(file, targetDir));
                }
            }
        }
        return files1;
    }

    @Override
    public List<File> dearchive(List<File> files, File targetDir, boolean act) throws IOException, CompressException {
        if(!targetDir.isDirectory()) {
            throw new CompressException("Target directory "+ targetDir.getAbsolutePath() + " type must be directory.");
        }
        List<File> fileList = dearchive(files, targetDir);
        if (act) {
            for (File file : files) {
                delFiles(file);
            }
        }
        return fileList;
    }

    @Override
    public List<File> dearchive(String sourceFilePath) throws IOException, CompressException {
        return dearchive(new File(sourceFilePath));
    }

    @Override
    public List<File> dearchive(File sourceFile) throws IOException, CompressException {
        return dearchive(sourceFile, new File(sourceFile.getParent()));
    }

    @Override
    public List<File> dearchive(File sourceFile, File targetDirectory) throws IOException, CompressException {
        if (!targetDirectory.isDirectory()) {
            throw new CompressException("Dearchive file be storage must be directory.");
        } else {
            TarArchiveInputStream tis = null;
            List<File> files;
            try {
                tis = new TarArchiveInputStream(new FileInputStream(
                        sourceFile));
                files = dearchive(targetDirectory, tis);
            } finally {
                try {
                    if (tis != null) tis.close();
                } catch (IOException e) {
                }
            }
            return files;
        }
    }

    @Override
    public List<File> dearchive(String sourceFilePath, String targetDirectoryPath) throws IOException, CompressException {
        return dearchive(new File(sourceFilePath), new File(targetDirectoryPath));
    }
}
