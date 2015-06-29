package org.humbird.soa.common.codec;

import org.humbird.soa.common.model.common.PropsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Properties;

/**
 * Created by david on 15/3/4.
 */
public class Codec {

    private final static Logger LOG = LoggerFactory.getLogger(Codec.class);

    public final static int MD5 = 0;

    public final static int SHA = 1;

    public final static int PBKDF2 = 2;

    public final static int DES = 3;

    public final static int BASE64 = 4;

    public final static String VALIDATE = "MD5|SHA|PBK|DES|B64";

    public final static String []ATTRS = "MD5|SHA|PBK|DES|B64".split("\\|");

    /**
     * salt key
     */
    private final static byte[] salt = {
            (byte) 0x08, (byte) 0xE9, (byte) 0xE5, (byte) 0xBA,
            (byte) 0xC4, (byte) 0x7A, (byte) 0x7C, (byte) 0x43,
            (byte) 0x16, (byte) 0x45, (byte) 0xCD, (byte) 0xA4,
            (byte) 0x16, (byte) 0x4C, (byte) 0x62, (byte) 0xF4,
            (byte) 0xAB, (byte) 0x10, (byte) 0x02, (byte) 0x2F,
            (byte) 0x8A,
    };

    private final static SecureRandom random = new SecureRandom();

    private static SecretKey deskey;

    private final static BASE64Encoder b64Encoder = new BASE64Encoder();

    private final static BASE64Decoder b64Decoder = new BASE64Decoder();

    static {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {
            DESKeySpec desKey = new DESKeySpec(salt);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            deskey = keyFactory.generateSecret(desKey);
        } catch (Exception e) {
            LOG.trace("Initial {} error", Codec.class);
        }
    }

    public static String enc(int f, String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException {
        switch (f) {
            case MD5:
                return md5Encode(key);
            case SHA:
                return shaEncode(key);
            case PBKDF2:
                return pbkEncode(key);
            case DES:
                return b64Encode(desEncode(key));
            case BASE64:
                return b64Encode(key.getBytes());
            default:
                return b64Encode(desEncode(key));
        }
    }

    public static String dec(int f, String key) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        switch (f) {
            case DES:
                return new String(desDecode(b64Decode(key)));
            case BASE64:
                return new String(b64Decode(key));
            default:
                return new String(desDecode(b64Decode(key)));
        }
    }

    /**
     * MD5 32 byte md5
     *
     * @param key
     * @return 32 byte md5
     */
    public static String md5Encode(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = null;
        md5 = MessageDigest.getInstance("MD5");

        byte[] byteArray = key.getBytes("UTF-8");
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * SHA 40 byte SHA
     *
     * @param key
     * @return 40 byte SHA
     */
    public static String shaEncode(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha = null;
        sha = MessageDigest.getInstance("SHA");

        byte[] byteArray = key.getBytes("UTF-8");
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    // The following constants may be changed without breaking existing hashes.
    public static final int SALT_BYTE_SIZE = 24;
    public static final int HASH_BYTE_SIZE = 24;
    public static final int PBKDF2_ITERATIONS = 1000;

    public static final int ITERATION_INDEX = 0;
    public static final int SALT_INDEX = 1;
    public static final int PBKDF2_INDEX = 2;

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param password the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String pbkEncode(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return pbkEncode(password.toCharArray());
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param password the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String pbkEncode(char[] password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
        // format iterations:salt:hash
        return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Validates a password using a hash.
     *
     * @param password    the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean validatePassword(String password, String correctHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return validatePassword(password.toCharArray(), correctHash);
    }

    /**
     * Validates a password using a hash.
     *
     * @param password    the password to check
     * @param correctHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public static boolean validatePassword(char[] password, String correctHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the hash into its parameters
        String[] params = correctHash.split(":");
        int iterations = Integer.parseInt(params[ITERATION_INDEX]);
        byte[] salt = fromHex(params[SALT_INDEX]);
        byte[] hash = fromHex(params[PBKDF2_INDEX]);
        // Compute the hash of the provided password, using the same salt,
        // iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.
        return slowEquals(hash, testHash);
    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line
     * system using a timing attack and then attacked off-line.
     *
     * @param a the first byte array
     * @param b the second byte array
     * @return true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    /**
     * Computes the PBKDF2 hash of a password.
     *
     * @param password   the password to hash.
     * @param salt       the salt
     * @param iterations the iteration count (slowness factor)
     * @param bytes      the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param hex the hex string
     * @return the hex string decoded into a byte array
     */
    private static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param array the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    /**
     * encrypt context
     *
     * @param key
     * @return
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException
     */
    public static byte[] desEncode(String key) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, deskey, random);
        return cipher.doFinal(key.getBytes());
    }

    /**
     * decrypt context
     *
     * @param buff
     * @return
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException
     */
    public static byte[] desDecode(byte[] buff) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, deskey, random);
        return cipher.doFinal(buff);
    }

    public static String b64Encode(byte[] buff) {
        return b64Encoder.encode(buff);
    }

    public static byte[] b64Decode(String key) throws IOException {
        return b64Decoder.decodeBuffer(key);
    }

    public static boolean isEncrpyt(String value) {
        int begin = value.indexOf('{');
        int end = value.indexOf('}');
        if (begin > -1 && end > -1 && VALIDATE.contains(value.substring(begin + 1, end))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getTrue(Properties properties, String key, String value) throws IOException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        int begin = value.indexOf('{');
        int end = value.indexOf('}');
        if (begin > -1 && end > -1 && "DES".equals(value.substring(begin + 1, end))) {
//            byte[] bytes = Codec.b64Decode(value.substring(end + 1));
//            value = new String(Codec.desDecode(bytes));
//            properties.setProperty(key, value);
            return false;
        } else {
            byte[] bytes = Codec.desEncode(value);
            value = "{DES}" + Codec.b64Encode(bytes);
            properties.setProperty(key, value);
            return true;
        }
    }

    public static List getTrue(String value, List vals) throws IOException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        int begin = value.indexOf('{');
        int end = value.indexOf('}');
        if (begin > -1 && end > -1 && "DES".equals(value.substring(begin + 1, end))) {
            byte[] bytes = Codec.b64Decode(value.substring(end + 1));
            value = new String(Codec.desDecode(bytes));
            vals.add(0, false);
            vals.add(1, value);
        } else {
            byte[] bytes = Codec.desEncode(value);
            value = "{DES}" + Codec.b64Encode(bytes);
            vals.add(0, true);
            vals.add(1, value);
        }
        return vals;
    }

    public static String decode(String value) throws IOException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        int begin = value.indexOf('{');
        int end = value.indexOf('}');
        if (begin > -1 && end > -1 && "DES".equals(value.substring(begin + 1, end))) {
            byte[] bytes = Codec.b64Decode(value.substring(end + 1));
            return new String(Codec.desDecode(bytes));
        }
        return value;
    }

    public static String autoDec(PropsModel.Prop prop, int encrpyt) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, IOException {
        if(prop.getKey().contains(PropsModel.ENCRPYT) ) {
            String value = prop.getVal();
            int begin = value.indexOf('{');
            int end = value.indexOf('}');
            value = value.substring(end + 1);
            return Codec.dec(encrpyt, value);
        } else {
            return prop.getVal();
        }
    }

    /**
     * Tests the basic functionality of the PasswordHash class
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            // Print out 10 hashes
            for (int i = 0; i < 10; i++)
                System.out.println(Codec.pbkEncode("p\r\nassw0Rd!"));

            // Test password validation
            boolean failure = false;
            System.out.println("Running tests...");
            for (int i = 0; i < 100; i++) {
                String password = "" + i;
                String hash = pbkEncode(password);
                String secondHash = pbkEncode(password);
                if (hash.equals(secondHash)) {
                    System.out.println("FAILURE: TWO HASHES ARE EQUAL!");
                    failure = true;
                }
                String wrongPassword = "" + (i + 1);
                if (validatePassword(wrongPassword, hash)) {
                    System.out.println("FAILURE: WRONG PASSWORD ACCEPTED!");
                    failure = true;
                }
                if (!validatePassword(password, hash)) {
                    System.out.println("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
                    failure = true;
                }
            }
            if (failure)
                System.out.println("TESTS FAILED!");
            else
                System.out.println("TESTS PASSED!");
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex);
        }
    }
}
