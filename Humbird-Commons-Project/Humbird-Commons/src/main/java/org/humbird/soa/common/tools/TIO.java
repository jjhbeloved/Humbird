package org.humbird.soa.common.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by david on 15/2/12.
 */
public class TIO {

    protected static final Logger LOG = LoggerFactory.getLogger(TIO.class);

    /**
     * salt key
     */
    private final static byte[] salt = {
            (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
            (byte) 0x72, (byte) 0xc8, (byte) 0xee, (byte) 0x99
    };

    public static BufferedWriter create(File file) throws IOException {
        if (!file.exists()) {
            return new BufferedWriter(new FileWriter(file));
        } else {
            return new BufferedWriter(new FileWriter(file, true));
        }
    }

    public static BufferedWriter create(File file, boolean append) throws IOException {
        if (!append) {
            return new BufferedWriter(new FileWriter(file));
        } else {
            return new BufferedWriter(new FileWriter(file, true));
        }
    }

    /**
     * @param writer
     * @param sb
     * @param count
     * @return
     * @throws java.io.IOException
     */
    public static boolean write(BufferedWriter writer, StringBuilder sb, int count) throws IOException {
        if (0 == count % (TFile.LINES / 2)) {
            append(writer, sb.toString());
            return true;
        }
        return false;
    }

    /**
     * @param writer
     * @param sb
     * @throws java.io.IOException
     */
    public static void append(BufferedWriter writer, StringBuilder sb) throws IOException {
        append(writer, sb.toString());
    }

    /**
     * @param writer
     * @param str
     * @throws java.io.IOException
     */
    public static void append(BufferedWriter writer, String str) throws IOException {
        if (str.length() > 0) {
            writer.write(str);
            writer.flush();
        }
    }

    public static void close(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException e) {
                // ...
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ...
                }
            }
        }
    }

    public static void releaseLock(List<File> files) {
        for (File file : files) {
            File lock = new File(file + ".lock");
            if(!lock.delete()) {
                LOG.trace("Cannot delete file: {}", lock.getName());
            }
        }
        files.clear();
    }

    /**
     * @param password
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(String password) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(salt);
        //创建一个密匙工厂，然后用它把DESKeySpec转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");
        //用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
        //现在，获取数据并加密
        //正式执行加密操作
        return cipher.doFinal(password.getBytes());
    }

    /**
     * @param password
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] password) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(salt);
        //创建一个密匙工厂，然后用它把DESKeySpec转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");
        //用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        //现在，获取数据并加密
        //正式执行加密操作
        return cipher.doFinal(password);
    }



}
