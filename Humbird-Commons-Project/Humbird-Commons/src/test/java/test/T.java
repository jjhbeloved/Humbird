package test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * Created by david on 15/3/11.
 */
public class T {

    private static final Logger LOG = Logger.getLogger("T");

    private static File tmp = new File("/home/soa/comm/test/TMP");

    private static File tmpDataDir = new File(tmp, "data");

    public static final int BUFFER_SIZE = 128 * 1024;

    public static void main(String []argvs) throws IOException {
        File file = new File("/home/soa/comm/test/XML");
        File f = new File("/home/soa/comm/test/XML/TNOR_CYCLE_C1_150000000197_32100129020000_E_PDAC_0_P_20150312_100954.XML");
        long begin = System.currentTimeMillis();
        System.out.println(f.exists());
        System.out.println(System.currentTimeMillis() - begin);

        File dir = new File("/home/soa/comm/test/PDF");
        File []files = dir.listFiles();
        if(files.length == 0) {
            System.out.println("ePDF is null.");
            System.exit(0);
        } else {
            if(!tmpDataDir.exists()) {
                tmpDataDir.mkdirs();
            }
            for(int i=0; i<files.length; i++) {
                File file1 = files[i];
                System.out.println("Deleted " + file1.getAbsolutePath() + " is " + renameFile(file1, new File(tmpDataDir, file1.getName()), true));
            }
        }
    }

    public static boolean renameFile(File from, File to, boolean copyAndDeleteOnRenameFail) throws IOException {
        if (!from.exists()) {
            return false;
        }

        boolean renamed = false;
        int count = 0;
        while (!renamed && count < 3) {

            renamed = from.renameTo(to);
            if (!renamed && count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            count++;
        }

        if (!renamed && copyAndDeleteOnRenameFail) {
            renamed = renameFileUsingCopy(from, to);
        }

        return renamed;
    }

    public static boolean renameFileUsingCopy(File from, File to) throws IOException {
        if (!from.exists()) {
            return false;
        }

        copyFile(from, to);
        if (!deleteFile(from)) {
            System.out.println(from.getAbsolutePath() + " is deleted");
        }

        return true;
    }

    public static boolean deleteFile(File file) {
        if (!file.exists()) {
            return false;
        }

        boolean deleted = false;
        int count = 0;
        while (!deleted && count < 3) {
            deleted = file.delete();
            if (!deleted && count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            count++;
        }

        return deleted;
    }

    public static void copyFile(File from, File to) throws IOException {
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(from).getChannel();
            out = new FileOutputStream(to).getChannel();

            long size = in.size();
            long position = 0;
            while (position < size) {
                position += in.transferTo(position, BUFFER_SIZE, out);
            }
        } finally {
            close(in, from.getName(), LOG);
            close(out, to.getName(), LOG);
        }
    }

    public static void close(Closeable closeable, String name, Logger log) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (log == null) {
                    log = LOG;
                }
                if (name != null) {
                    log.info("Cannot close: " + name + ". Reason: " + e.getMessage());
                } else {
                    log.info("Cannot close. Reason: " + e.getMessage());
                }
            }
        }
    }
}
