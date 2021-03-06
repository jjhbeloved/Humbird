package org.humbird.soa.common.utils;

/**
 * Unit utils.
 */
public final class UnitUtils {

    private UnitUtils() {
    }

    /**
     * If having a size in bytes and wanting to print this in human friendly\
     * format with xx kB, xx MB, xx GB instead of a large byte number.
     *
     * @param bytes  the value in bytes
     */
    public static String printUnitFromBytes(long bytes) {
        // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "" + "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
