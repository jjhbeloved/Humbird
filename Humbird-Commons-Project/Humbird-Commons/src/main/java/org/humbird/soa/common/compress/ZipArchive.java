package org.humbird.soa.common.compress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.*;

/**
 * https://github.com/orgs/Asiainfo-ODC
 * <p/>
 * Created by david on 14-9-2.
 */
public class ZipArchive extends CompressUtils implements IArchive {

    private final static Logger LOG = LoggerFactory.getLogger(ZipArchive.class);

    /**
     * 压缩zip包
     *
     * @param source     源文件(目录/文件)
     * @param fileNames  匹配文件名
     * @param targetDir  目标路径
     * @param targetName 压缩文件名
     * @throws org.humbird.soa.common.compress.CompressException 解压缩异常
     * @throws java.io.FileNotFoundException                       文件不存在
     */
    @Deprecated
    public String zip(String source, List<String> fileNames, String targetDir, String targetName) throws FileNotFoundException, CompressException {
        List<File> fileList = loadFileName(new File(source), fileNames);
        if (fileList.size() == 0) {
            return null;
        }
        try {
            validateTemplateFile(targetDir, targetName);
        } catch (IOException e) {
            throw new CompressException(e.getMessage());
        }
        String tmpZip = targetDir + File.separator + targetName + ZIP_EXT;

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        File zipFile = new File(tmpZip + SUFFIX);

        if (zipFile.exists()) {
            if(!zipFile.delete()) {
                LOG.trace("Delete file {} failed", zipFile.getName());
            }
        }
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            byte[] bufs = new byte[BUFFEREDSIZE * 10];
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                writeZip(file, source, fos, fis, zos, bis, bufs);
            }
            zos.flush();
            zos.closeEntry();
            File zipFile2 = new File(tmpZip);
            if (zipFile2.exists()) {
                if(!zipFile2.delete()) {
                    LOG.trace("Delete file {} failed", zipFile2.getName());
                }
            }
            if(!zipFile.renameTo(zipFile2)) {
                throw new CompressException("Rename" + zipFile.getName() + " to " + zipFile2.getName() + " failed");
            }
        } catch (IOException e) {
            if(!zipFile.delete()) {
                LOG.trace("Delete file {} failed", zipFile.getName());
            }
            throw new CompressException(e.getMessage());
        } finally {
            // 关闭流
            if (null != fos)
                try {
                    fos.close();
                } catch (IOException e) {
                }
            if (null != zos)
                try {
                    zos.close();
                } catch (IOException e) {
                }
            if (null != fis) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
            if (null != bis) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return tmpZip;
    }

    /**
     * 压缩zip包
     *
     * @param srcFiles   源文件(目录/文件)
     * @param targetDir  目标路径
     * @param targetName 压缩文件名
     * @param dels       删除文件目录前缀
     * @param act        压缩后对文件的操作
     * @throws org.humbird.soa.common.compress.CompressException 解压缩异常
     * @throws java.io.FileNotFoundException                       文件不存在
     */
    @Deprecated
    public String zip(List<File> srcFiles, String targetDir, String targetName, List<String> dels, boolean act) throws CompressException, FileNotFoundException {
        if (srcFiles == null || srcFiles.size() == 0) {
            throw new CompressException("Array files must be not null.");
        }
        try {
            validateTemplateFile(targetDir, targetName);
        } catch (IOException e) {
            throw new CompressException(e.getMessage());
        }
        String tmpZip = targetDir + File.separator + targetName + ZIP_EXT;

        File zipFile = new File(tmpZip + SUFFIX);
        if (zipFile.exists()) {
            if(!zipFile.delete()) {
                LOG.trace("Delete file {} failed", zipFile.getName());
            }
        }

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            byte[] bufs = new byte[BUFFEREDSIZE * 10];
            for (int i = 0; i < srcFiles.size(); i++) {
                File file = srcFiles.get(i);
                writeZip(file, dels, fos, fis, zos, bis, bufs, zipFile.getName());
            }
            zos.flush();
            zos.closeEntry();
            File zipFile2 = new File(tmpZip);
            if (zipFile2.exists()) {
                if(!zipFile2.delete()) {
                    LOG.trace("Delete file {} failed", zipFile2.getName());
                }
            }
            if(!zipFile.renameTo(zipFile2)) {
                throw new CompressException("Rename" + zipFile.getName() + " to " + zipFile2.getName() + " failed");
            }
            if (act) {
                for (int i = 0, size = srcFiles.size(); i < size; i++) {
                    File file = srcFiles.get(i);
                    delFiles(file, zipFile2.getPath());
                }
            }
        } catch (IOException e) {
            if(!zipFile.delete()) {
                LOG.trace("Delete file {} failed", zipFile.getName());
            }
            throw new CompressException(e.getMessage());
        } finally {
            // 关闭流
            if (null != fos)
                try {
                    fos.close();
                } catch (IOException e) {
                }
            if (null != zos) {
                try {
                    zos.closeEntry();
                } catch (IOException e) {

                } finally {
                    try {
                        zos.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (null != bis) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return tmpZip;
        }
    }

    /**
     * 压缩zip包
     *
     * @param srcFiles   源文件(目录/文件)
     * @param targetDir  目标路径
     * @param targetName 压缩文件名
     * @param dels       删除文件目录前缀
     * @throws org.humbird.soa.common.compress.CompressException 解压缩异常
     * @throws java.io.IOException                                 IO异常
     */
    @Deprecated
    public String zip(List<File> srcFiles, String targetDir, String targetName, List<String> dels) throws
            IOException, CompressException {
        return zip(srcFiles, targetDir, targetName, dels, false);
    }

    private void writeZip(File file, String source, FileOutputStream fos, FileInputStream fis, ZipOutputStream zos, BufferedInputStream bis, byte[] bufs) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    File file1 = files[i];
                    writeZip(file1, source, fos, fis, zos, bis, bufs);
                }
            }
        } else {
            // 创建ZIP实体,并添加进压缩包
            String tmp = getEntryName(source, file);
            ZipEntry zipEntry = new ZipEntry(tmp);
            zos.putNextEntry(zipEntry);
            // 读取待压缩的文件并写进压缩包里
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, BUFFEREDSIZE * 10);
            int read;
            while ((read = bis.read(bufs, 0, BUFFEREDSIZE * 10)) != -1) {
                zos.write(bufs, 0, read);
            }
            zos.flush();
            zos.closeEntry();
        }
    }

    /**
     * 遍历压缩文件
     *
     * @param file
     * @param dels
     * @param fos
     * @param fis
     * @param zos
     * @param bis
     * @param bufs
     * @param zipName
     * @throws java.io.IOException
     */
    private void writeZip(File file, List<String> dels, FileOutputStream fos, FileInputStream fis, ZipOutputStream zos, BufferedInputStream bis, byte[] bufs, String zipName) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    writeZip(files[i], dels, fos, fis, zos, bis, bufs, zipName);
                }
            }
        } else {
            if (zipName.equals(file.getName())) return;
            // 创建ZIP实体,并添加进压缩包
            String tmp = getEntryName(file, dels);
            ZipEntry zipEntry = new ZipEntry(tmp);
            zos.putNextEntry(zipEntry);
            // 读取待压缩的文件并写进压缩包里
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, BUFFEREDSIZE * 10);
            int read = 0;
            while ((read = bis.read(bufs, 0, BUFFEREDSIZE * 10)) != -1) {
                zos.write(bufs, 0, read);
            }
            zos.flush();
            zos.closeEntry();
        }
    }

    /**
     * 归档
     *
     * @param srcFile  源路径
     * @param zos      ZipOutputStream
     * @param basePath 归档包内相对路径
     * @param keep     目标文件绝对路径
     */
    private void zip(File srcFile, ZipOutputStream zos,
                     String basePath, String keep) throws IOException {
        if (srcFile.isDirectory()) {
            zipDir(srcFile, zos, basePath, keep);
        } else {
            zipFile(srcFile, zos, basePath, keep);
        }
    }

    /**
     * 目录归档
     *
     * @param dir      归档目录
     * @param zos      ZipOutputStream
     * @param basePath
     */
    private void zipDir(File dir, ZipOutputStream zos,
                        String basePath, String keep) throws IOException {
        File[] files = dir.listFiles();
        ZipEntry entry = new ZipEntry(basePath
                + dir.getName() + PATH);
        zos.putNextEntry(entry);
        zos.flush();
        zos.closeEntry();
        if (files != null && files.length > 0) {
            for (int i = 0, size = files.length; i < size; i++) {
                zip(files[i], zos, basePath + dir.getName() + PATH, keep);
            }
        }
    }

    /**
     * 数据归档
     *
     * @param file 待归档文件
     * @param dir  归档目录
     * @param zos  ZipOutputStream
     */
    private void zipFile(File file, ZipOutputStream zos,
                         String dir, String keep) throws IOException {
        if (keep.equals(file.getPath())) return;
        // 创建ZIP实体,并添加进压缩包
        ZipEntry zipEntry = new ZipEntry(dir + file.getName());
        BufferedInputStream bis = null;
        byte bufs[] = new byte[BUFFEREDSIZE * 10];
        try {
            zos.putNextEntry(zipEntry);
            // 读取待压缩的文件并写进压缩包里
            bis = new BufferedInputStream(new FileInputStream(file), BUFFEREDSIZE * 10);
            int read = 0;
            while ((read = bis.read(bufs, 0, BUFFEREDSIZE * 10)) != -1) {
                zos.write(bufs, 0, read);
            }
            zos.flush();
        } finally {
            // 关闭流
            try {
                if (null != bis) bis.close();
            } catch (IOException e) {
            }
            try {
                if (null != zos) zos.closeEntry();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 文件 解压缩
     *
     * @param destFile 目录文件
     * @param zis      ZipInputStream
     */
    private List<File> unzip(File destFile, ZipInputStream zis) throws IOException {
        List<File> files = new ArrayList<File>();
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
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
                try {
                    zis.closeEntry();
                } catch (IOException e) {
                }
                continue;
            } else {
                try {
                    unzipFile(dirFile, zis);
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
                    throw new IOException("Rename" + dirFile.getName() + " to " + dirs.getName() + " failed");
                }
                try {
                    zis.closeEntry();
                } catch (IOException e) {
                }
            }
            files.add(new File(dir));
        }
        return files;
    }

    /**
     * 文件解压缩
     *
     * @param destFile 目标文件
     * @param zis      ZipInputStream
     * @throws java.io.IOException
     */
    private void unzipFile(File destFile, ZipInputStream zis) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(destFile));
        int count;
        byte data[] = new byte[BUFFEREDSIZE * 10];
        while ((count = zis.read(data, 0, BUFFEREDSIZE * 10)) != -1) {
            bos.write(data, 0, count);
        }
        bos.close();
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName) throws CompressException, FileNotFoundException {
        return archive(sourceFiles, targetDirectory, targetFileName, false);
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act) throws CompressException, FileNotFoundException {
        try {
            validateTemplateFile(targetDirectory, targetFileName);
        } catch (IOException e) {
            throw new CompressException(e.getMessage());
        }
        String destPath = targetDirectory + PATH + targetFileName + ZIP_EXT;
        File tarFile = new File(destPath + SUFFIX);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(
                    new FileOutputStream(tarFile));
            for (File file : sourceFiles) {
                zip(file, zos, BASE_DIR, tarFile.getPath());
            }
            zos.flush();
        } catch (IOException e) {
            if(!tarFile.delete()) {
                LOG.trace("Delete file {} failed", tarFile.getName());
            }
            throw new CompressException(e.getMessage());
        } finally {
            // 关闭流
            if (null != zos) {
                try {
                    zos.closeEntry();
                } catch (IOException e) {

                } finally {
                    try {
                        zos.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        File destFile = new File(destPath);
        if(!tarFile.renameTo(destFile)) {
            LOG.trace("Rename {} to {} failed", new Object[]{ tarFile.getName(), destFile.getName()});
        }
        if (act) {
            for (int i = 0, size = sourceFiles.size(); i < size; i++) {
                delFiles(sourceFiles.get(i), destPath);
            }
        }
        return destPath;
    }

    @Override
    public String archive(List<File> sourceFiles, String targetDirectory, String targetFileName, boolean act, boolean isLock) throws FileNotFoundException, CompressException {
        return null;
    }

    @Override
    public void archive(String sourceFilePath) throws IOException {
        archive(new File(sourceFilePath));
    }

    @Override
    public void archive(File sourceFile) throws IOException {
        String name = sourceFile.getName();
        String basePath = sourceFile.getParent();
        String destPath = basePath + File.separator + name + ZIP_EXT;
        archive(sourceFile, new File(destPath));
    }

    @Override
    public void archive(File sourceFile, File targetFile) throws IOException {
        File tarFile = new File(targetFile.getAbsolutePath() + SUFFIX);
        ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(tarFile));
        zip(sourceFile, zos, BASE_DIR, tarFile.getPath());
        zos.flush();
        try {
            zos.closeEntry();
        } catch (IOException e) {

        } finally {
            try {
                zos.close();
            } catch (IOException e) {
            }
        }
        if(!tarFile.renameTo(targetFile)) {
            LOG.trace("Rename {} to {} failed", new Object[]{ tarFile.getName(), targetFile.getName()});
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
                File[] farray = file.listFiles();
                if (farray != null) {
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
        if (!targetDir.isDirectory()) {
            throw new CompressException("Target directory " + targetDir.getAbsolutePath() + " type must be directory.");
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
            throw new CompressException("Unzip file be storage must be directory.");
        } else {
            CheckedInputStream cis = null;
            List<File> files;
            ZipInputStream zis = null;
            try {
                cis = new CheckedInputStream(new FileInputStream(
                        sourceFile), new CRC32());
                zis = new ZipInputStream(cis);
                files = unzip(targetDirectory, zis);
            } finally {
                if (cis != null) {
                    try {
                        cis.close();
                    } catch (IOException e) {

                    }
                }
                if (zis != null) {
                    try {
                        zis.closeEntry();
                    } catch (IOException e) {
                    } finally {
                        try {
                            zis.close();
                        } catch (IOException e) {

                        }
                    }
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