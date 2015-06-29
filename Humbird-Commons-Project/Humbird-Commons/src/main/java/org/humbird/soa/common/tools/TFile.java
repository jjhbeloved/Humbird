package org.humbird.soa.common.tools;

import org.apache.commons.io.FileUtils;
import org.humbird.soa.common.io.EFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.forceMkdir;

/**
 * Created by david on 14/12/12.
 * <p/>
 * 文件工具类, 80%保证事务一致性
 */
public class TFile {

    private final static Logger LOG = LoggerFactory.getLogger(TFile.class);

    public final static String TMP_FILE_NAME_PREFIX = "tmp_";
    public final static String HORIZONTAL = "_";
    public final static String NEW_LINE = System.getProperty("line.separator");
    public final static String NEW_SPLIT = " ";
    public final static int LINES = 512;
    public final static int COLUMNS = 512;
    public final static String ENCODING = "UTF-8";
    public final static String SUFFIX = ".tmp";
    public final static String LOCK = ".lock";
    public final static String INPROGRESS = ".inprogress";

    // 遍历打印文件
    public static void listFiles(File fs) {
        if (fs.isDirectory()) {
            for (File f : fs.listFiles()) {
                listFiles(f);
            }
        } else {
            p(fs.getAbsolutePath());
        }
    }

    /**
     * 遍历添加文件目录列表
     * 文件每行按照<<换行符>>分割
     * 内部保证一定是文件(非目录)
     *
     * @param fs   扫描文件
     * @param cols 每列的个数
     * @return 拆分临时目标文件列表, 内部保证一定是文件(非目录)
     * @throws java.io.IOException 处理完冗余文件后, 上抛
     */
    public static List<File> create(File fs, String fname, int cols) throws IOException {
        return create(fs, fname, cols, null);
    }

    /**
     * 保证事务性
     * 遍历添加文件目录列表
     * 文件每行按照<<换行符>>分割
     * 内部保证一定是文件(非目录)
     *
     * @param fs     扫描文件
     * @param cols   每列的个数
     * @param suffix 过滤后缀
     * @return 拆分临时目标文件列表, 内部保证一定是文件(非目录)
     * @throws java.io.IOException 处理完冗余文件后, 上抛
     */
    public static List<File> create(File fs, String fname, int cols, String suffix) throws IOException {
        List<File> fileList = new ArrayList<File>();
        List<Integer> fnum = new ArrayList<Integer>();
        fnum.add(0);
        StringBuilder sb = new StringBuilder("");
        try {
            create(fs, fileList, fname, sb, 0, cols, fnum, suffix);
            appendLastStr(fileList, fname, sb, fnum);
        } catch (IOException e) {   // 如若捕获异常, 需要删除之前产生的临时文件
            if (fileList != null && fileList.size() > 0) {
                for (int i = 0, size = fileList.size(); i < size; i++) {
                    if(!fileList.get(i).delete()) {
                        LOG.trace("Delete file {} failed", fileList.get(i).getName());
                    }
                }
            }
            throw e;
        }
        return fileList;
    }

    /**
     * @param fs             扫描文件
     * @param bufferedWriter 目标文件
     * @param sb             字符串
     * @param count          缓冲个数
     * @param suffix         过滤后缀
     * @return 记录了还有多少条数据
     * @throws java.io.IOException 写文件失败, 不处理, 将冗余问题上抛
     * @@ 非事务级别
     * 遍历添加目录文件列表, 生成一个文件
     * 文件每行按照<<空格符>>分割
     */
    public static int create(File fs, BufferedWriter bufferedWriter, StringBuilder sb, int count, String suffix) throws IOException {
        if (fs.isDirectory()) {
            for (File file : fs.listFiles()) {
                count = create(file, bufferedWriter, sb, count, suffix);
            }
        } else {
            String name = fs.getName();
            if (suffix == null || suffix.equals(name.substring(name.lastIndexOf(".") + 1))) {   // 需要校验后缀, 并且不符合后缀
                if (count == (LINES * 4 - 1)) {
                    sb.append(NEW_SPLIT).append(fs.getName()).append(NEW_LINE);
                    bufferedWriter.write(sb.toString());
                    bufferedWriter.flush();
                    sb.setLength(0);
                    count = 0;
                } else {
                    if (count > 0) sb.append(NEW_SPLIT);
                    sb.append(fs.getName());
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * @param fs             扫描文件
     * @param bufferedWriter 目标文件
     * @param sb             字符串
     * @param count          缓冲队列大小
     * @param time           校验天数
     * @param suffix         过滤后缀
     * @return 记录了还有多少条数据
     * @throws java.io.IOException 写文件失败, 不处理, 将冗余问题上抛
     * @@ 非事务级别
     * 遍历添加目录文件列表, 生成一个文件
     * 文件每行按照<<空格符>>分割
     * 删除时间最后修改时间超过time的文件
     */
    public static int createAndDel(File fs, BufferedWriter bufferedWriter, StringBuilder sb, int count, long time, String suffix) throws IOException {
        if (fs.isDirectory()) {
            for (File file : fs.listFiles()) {
                count = createAndDel(file, bufferedWriter, sb, count, time, suffix);
            }
        } else {
            String name = fs.getName();
            if (suffix == null || suffix.equals(name.substring(name.lastIndexOf(".") + 1))) {   // 需要校验后缀
                if ((System.currentTimeMillis() - fs.lastModified()) / time > 1.0) {    // 校验时间
                    if(!fs.delete()) {
                        LOG.trace("Delete file {} failed", fs.getName());
                    }
                } else {
                    if (count == (LINES * 4 - 1)) {
                        sb.append(NEW_SPLIT).append(fs.getName()).append(NEW_LINE);
                        bufferedWriter.write(sb.toString());
                        bufferedWriter.flush();
                        sb.setLength(0);
                        count = 0;
                    } else {
                        if (count > 0) sb.append(NEW_SPLIT);
                        sb.append(fs.getName());
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @param file       被扫描文件
     * @param filter     自定义过滤器
     * @param prefixName 文件名前缀(包含完整路径)
     * @param range      过滤范围
     * @param split      新生成文件内容的分隔符
     * @param count      缓冲队列大小
     * @param index      计数器
     * @param fs
     * @throws java.io.IOException 写入异常, 不处理上抛
     * @@ 无法保证事务一致性, 可以上抛处理
     * 扫描文件, 每行生成 count 列个数数据
     */
    public static void createAndDel(File file, EFileFilter filter, String prefixName, int[] range, String split, int count, int index, Map<String, StringBuilder> fs) throws IOException {
        if (file.isDirectory()) {
            String ss[] = file.list();
            if (ss == null || ss.length == 0) return;
            for (int i = 0, size = ss.length; i < size; i++) {
                createAndDel(new File(file.getAbsolutePath() + File.separator + ss[i]), filter, prefixName, range, split, count, i, fs);
            }
        } else {
            String dirName = null;
            if ((filter == null) || (dirName = filter.accept(file, range)) != null) {
                StringBuilder line = fs.containsKey(dirName) ? fs.get(dirName).append(split) : new StringBuilder("");
                fs.put(dirName, line.append(file.getName()));
                if ((index + 1) % count == 0) {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(prefixName + dirName + SUFFIX, true));   // 追加模式
                        writer.write(line.toString());
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {   // 删除冗余文件
                        File file1 = new File(prefixName + dirName + SUFFIX);
                        if (file1.exists()) {
                            if(!file1.delete()) {
                                LOG.trace("Delete file {} failed", file1.getName());
                            }
                        }
                        fs.remove(dirName); // 删除表引用
                        throw e;
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                    fs.get(dirName).setLength(0);
                }
            }
        }
    }

    /**
     * 遍历添加文件目录列表, 生成多个文件
     * 文件每行按照<<换行符>>分割
     *
     * @param fs       扫描文件目录
     * @param fileList 返回文件列表
     * @param fname    临时文件所属项目名
     * @param sb       字符串
     * @param count    缓存队列
     * @param cols     列数
     * @param fnum     计数器清空
     * @param suffix   过滤后缀
     * @return 记录了还有多少条数据
     * @throws java.io.IOException 写入异常, 上抛
     */
    public static int create(File fs, List<File> fileList, String fname, StringBuilder sb, int count, int cols, List<Integer> fnum, String suffix) throws IOException {
        if (fs.isDirectory()) {
            for (File file : fs.listFiles()) {
                count = create(file, fileList, fname, sb, count, cols, fnum, suffix);
            }
        } else {
            String name = fs.getName();
            if (suffix == null || suffix.equals(name.substring(name.lastIndexOf(".") + 1))) {   // 需要校验后缀, 并且不符合后缀
                if (count == (cols - 1)) {
                    BufferedWriter bufferedWriter = null;
                    String tmp = FileUtils.getTempDirectoryPath() + File.separator + TMP_FILE_NAME_PREFIX + fnum.get(0) + HORIZONTAL + fname + HORIZONTAL + UUID.randomUUID().toString() + SUFFIX;
                    sb.append(NEW_LINE).append(fs.getName());
                    try {
                        bufferedWriter = new BufferedWriter(new FileWriter(tmp));
                        bufferedWriter.write(sb.toString());
                        bufferedWriter.flush();
                    } catch (IOException e) {   // 处理异常造成的临时文件冗余
                        File file = new File(tmp);
                        if (file.exists()) {
                            if(!file.delete()) {
                                LOG.trace("Delete file {} failed", file.getName());
                            }
                        }
                        throw e;
                    } finally {
                        if (null != bufferedWriter)
                            try {
                                bufferedWriter.close();
                            } catch (IOException e) {
                            }
                    }
                    sb.setLength(0);
                    count = 0;
                    fileList.add(new File(tmp));
                    fnum.set(0, fnum.get(0) + 1);
                } else {
                    if (count > 0) sb.append(NEW_LINE);
                    sb.append(fs.getName());
                    count++;
                }
            }
        }
        return count;
    }

    //

    /**
     * 扫描结余文件路径字符串
     *
     * @param bufferedWriter
     * @param sb
     * @throws java.io.IOException 异常未处理, 上抛
     */
    public static void appendLastStr(BufferedWriter bufferedWriter, StringBuilder sb) throws IOException {
        if (sb.length() > 0) {
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            sb.setLength(0);
        }
    }

    //

    /**
     * 扫描结余文件路径字符串
     *
     * @param fileList 存储所有临时文件的队列
     * @param fname    临时文件核心名
     * @param sb       字符串
     * @param fnum     临时文件索引
     * @throws java.io.IOException 异常处理后, 上抛
     */
    public static void appendLastStr(List<File> fileList, String fname, StringBuilder sb, List<Integer> fnum) throws IOException {
        if (sb.length() > 0) {
            BufferedWriter bufferedWriter = null;
            String tmp = FileUtils.getTempDirectoryPath() + File.separator + TMP_FILE_NAME_PREFIX + fnum.get(0) + HORIZONTAL + fname + HORIZONTAL + UUID.randomUUID().toString() + SUFFIX;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(tmp));
                bufferedWriter.write(sb.toString());
                bufferedWriter.flush();
            } catch (IOException e) {
                File file = new File(tmp);
                if (file.exists()) {
                    if(!file.delete()) {
                        LOG.trace("Delete file {} failed", file.getName());
                    }
                }
                throw e;
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {

                    }
                }
            }
            sb.setLength(0);
            fileList.add(new File(tmp));
        }
    }

    // 遍历删除目录(文件)及目录下文件
    public static void deleteFiles(File fs) {
        deleteFiles(fs, 1);
    }

    /**
     * 遍历删除目录(文件)及目录下文件, 第一个目录不删除
     *
     * @param fs    删除文件
     * @param count 如果文件是目录, 不删除
     */
    public static void deleteFiles(File fs, int count) {
        if (fs.isDirectory()) {
            for (File f : fs.listFiles()) {
                deleteFiles(f, count + 1);
            }
            if (count != 0) {
                if(!fs.delete()) {
                    LOG.trace("Delete file {} failed", fs.getName());
                }
            }
        } else {
            if(!fs.delete()) {
                LOG.trace("Delete file {} failed", fs.getName());
            }
        }
    }

    /**
     * 创建指定临时目录
     *
     * @param directory
     * @throws java.io.IOException 目录已存在, 创建失败
     */
    public static void createTemplateDirectory(File directory) throws IOException {
        forceMkdir(directory);
    }

    /**
     * 创建指定临时目录
     *
     * @param directory
     * @throws java.io.IOException 目录已存在, 创建失败
     */
    public static void createTemplateDirectory(String directory) throws IOException {
        createTemplateDirectory(new File(directory));
    }


    public static void p(Object o) {
        System.out.println(o);
    }
}
