package org.humbird.soa.common.compress;

import org.apache.commons.lang.StringUtils;
import org.humbird.soa.common.tools.TFile;
import org.humbird.soa.common.tools.TTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/orgs/Asiainfo-ODC
 * 此接口提供 文件(不支持目录) 压缩/解压
 * 保证为事务级别处理
 *
 * Created by david on 15/1/2.
 */
abstract class CompressUtils {

    private final static Logger LOG = LoggerFactory.getLogger(CompressUtils.class);

    // zip
    protected static final CharSequence ZIP_EXT = ".zip";
    // gzip
    protected static final CharSequence GZIP_EXT = ".gz";
    // tar
    protected static final CharSequence TAR_EXT = ".tar";
    // bzip2
    protected static final CharSequence BZIP_EXT = ".bz2";
    // 缓冲
    protected static final int BUFFEREDSIZE = 1024;

    protected static final String BASE_DIR = "";
    // 符号"/"用来作为目录标识判断符
    protected static final String PATH = File.separator;
    // 后缀.tmp代表临时文件
    protected static final String SUFFIX = TFile.SUFFIX;

    /**
     * 内置函数名
     */
    private final static String GET_TIME = "current_time";

    private final static char WILDCAR = '%';
    private final static char BEIGN_CHAR = '{';
    private final static char END_CHAR = '}';
    private final static String WILDCAR_STR = "%";

    /**
     * 获得临时压缩文件
     *
     * @param targetDir  目标目录
     * @param targetName 目标文件名
     */
    protected static void validateTemplateFile(String targetDir, String targetName) throws CompressException, IOException {
        if (StringUtils.isEmpty(targetName)) {
            throw new CompressException("Target name " + targetName + " is empty");
        }
        TFile.createTemplateDirectory(targetDir);
    }

    /**
     * 动态压缩文件名
     *
     * @param f 文件名
     * @return 真实名
     */
    protected static String dynamicFileName(String f) {
        String str = f;
        int begin = 0;
        int end = -1;
        f = "";
        for (int j = 0; j < str.length(); j++) {
            if (str.charAt(j) == BEIGN_CHAR) {
                begin = j;
                String start = str.substring(end + 1, begin);
                f += start;
            }
            if (str.charAt(j) == END_CHAR) {
                end = j;
                String stop = str.substring(begin + 1, end);
                f += getPathName(stop);
            }
        }
        f += str.substring(end + 1);
        return f;
    }

    /**
     * 获得动态文件名
     *
     * @param param 表达式
     * @return 真实路径名
     */
    protected static String getPathName(String param) {
        String s = param;
        String[] params = param.split(":");
        if (GET_TIME.equalsIgnoreCase(params[0])) {
            s = TTimestamp.getDate(params[1]);
        }
        return s;
    }

    /**
     * 遍历删除文件
     *
     * @param file 文件
     * @param keep 保留文件
     */
    protected static void delFiles(File file, String keep) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    File f = files[i];
                    delFiles(f, keep);
                }
            }
        } else {
            if (keep.equals(file.getPath())) return;
            if(!file.delete()) {
                LOG.trace("Delete file {} failed", file.getName());
            }
        }
    }

    /**
     * 遍历删除文件
     *
     * @param file 文件
     */
    protected static void delFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    File f = files[i];
                    delFiles(f);
                }
            }
        } else {
            if(!file.delete()) {
                LOG.trace("Delete file {} failed", file.getName());
            }
        }
    }

    /**
     * 判断 f1 动态文件名 f2静态文件名
     *
     * @param f1 动态文件名
     * @param f2 静态文件名
     * @return 是否相等
     */
    protected static boolean isEqualPath(String f1, String f2) {
        boolean isTrue = true;
        String tmp = f1.toLowerCase();
        f2 = f2.toLowerCase();
        if (!tmp.equals(f2)) {
            isTrue = false;
            for (int j = 0; j < tmp.length(); j++) {
                if (j == 0 && WILDCAR == tmp.charAt(j) && WILDCAR == tmp.charAt(tmp.length() - 1)) { // %ss%
                    if (f2.contains(tmp.substring(1, tmp.length() - 1))) {
                        isTrue = true;
                        break;
                    }
                } else if (j != 0 && j != tmp.length() - 1 && WILDCAR == tmp.charAt(j)) {   //ss%ss
                    if (f2.startsWith(tmp.substring(0, j)) && f2.endsWith(tmp.substring(j + 1))) {
                        isTrue = true;
                        break;
                    }
                } else if (j == 0 && WILDCAR == tmp.charAt(j) && !tmp.substring(j + 1).contains(WILDCAR_STR)) { // %ss
                    if (f2.endsWith(tmp.substring(1, tmp.length()))) {
                        isTrue = true;
                        break;
                    }
                } else if (j == tmp.length() - 1 && WILDCAR == tmp.charAt(j) && !tmp.substring(0, tmp.length() - 1).contains(WILDCAR_STR)) { // %ss
                    if (f2.startsWith(tmp.substring(0, tmp.length() - 1))) {
                        isTrue = true;
                        break;
                    }
                }
            }
        }
        return isTrue;
    }

    /**
     * 递归加载文件名
     *
     * @param file 文件
     * @return
     */
    protected static List<File> loadFileName(File file, List<String> names) {
        List<File> filenameList = new ArrayList<File>();
        if (file.isFile()) {
            if (names.size() == 0) {
                filenameList.add(file);
            } else {
                for (String name : names) {
                    if (isEqualPath(dynamicFileName(name), file.getName())) {
                        filenameList.add(file);
                    }
                }
            }
        }
        if (file.isDirectory()) {
            File []files = file.listFiles();
            if(files != null) {
                for (int i=0, size = files.length; i<size; i++) {
                    File f = files[i];
                    filenameList.addAll(loadFileName(f, names));
                }
            }
        }
        return filenameList;
    }

    /**
     * 获得压缩文件名
     *
     * @param file 要压缩的地址
     * @param dels 要删除的路径前缀
     * @return
     */
    protected static String getEntryName(File file, List<String> dels) {
        String ret = null;
        for (String del : dels) {
            String str = file.getPath().substring(0, del.length());
            if (str.equalsIgnoreCase(del)) {
                ret = file.getPath().substring(del.length() + 1);
                break;
            }
        }
        return ret == null ? file.getPath() : ret;
    }


    /**
     * 获得压缩文件名
     *
     * @param base 源地址
     * @param file 要压缩的地址
     * @return
     */
    protected static String getEntryName(String base, File file) {
        File baseFile = new File(base);
        String filename = file.getPath();
        if (baseFile.isFile()) {
            base = baseFile.getParent();
        }
        return filename.substring(base.length() + 1);
    }


    /**
     * 文件探针
     * <p/>
     * <pre>
     * 当父目录不存在时，创建目录！
     * </pre>
     *
     * @param dirFile
     */
    protected static void fileProber(File dirFile) {
        File parentFile = dirFile.getParentFile();
        if (!parentFile.exists()) {
            // 递归寻找上级目录
            fileProber(parentFile);
            if(!parentFile.mkdir()) {
                LOG.trace("Mkdir {} failed", parentFile.getName());
            }
        }
    }
}
