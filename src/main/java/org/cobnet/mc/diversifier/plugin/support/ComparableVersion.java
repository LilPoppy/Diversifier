package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.Version;
import org.cobnet.mc.diversifier.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public record ComparableVersion(@Getter String version) implements Version {

    private final static String DEFAULT_VERSION_STRING = "snapshot";

    private final static int DEFAULT_VERSION_INT = ComparableVersion.VERSION_STR(ComparableVersion.DEFAULT_VERSION_STRING);

    private final static boolean USE_DEFAULT_VERSION_FOR_UNKNOWN = true;

    private final static Map<String, Map<String, AtomicInteger>> DP = new HashMap<>();

    private static int VERSION_STR(String s) {
        switch (s.toLowerCase()) {
            case "alpha":
                return 0;
            case "beta":
                return 1;
            case "rc":
            case "r":
                return 2;
            case "snapshot":
                return 3;
            case "dev":
                return 4;
            case "release":
                return 5;
            case "stable":
                return 6;
            case "final":
                return 7;
            case "latest":
                return 8;
            default:
                if (ComparableVersion.USE_DEFAULT_VERSION_FOR_UNKNOWN) return ComparableVersion.DEFAULT_VERSION_INT;
                else throw new UnknownFormatConversionException("Unknown version string: " + s);
        }
    }


    @Override
    public int compareTo(@NotNull Version other) {
        String[] x = this.version.split("\\.");
        String[] y = other.version().split("\\.");
        int ret;
        int length = Math.min(x.length, y.length);
        for (int i = 0; i < length; i++) {
            Map<String, AtomicInteger> dp = DP.get(x[i]);
            if (dp != null) {
                AtomicInteger n = dp.get(y[i]);
                if (n != null) ret = n.get();
                else ret = compare(x[i], y[i]);
            } else ret = compare(x[i], y[i]);
            if (ret != 0) return ret;
        }
        if (x.length < y.length) return -1;
        else if (x.length > y.length) return 1;
        return 0;
    }

    private int compare(String x, String y) {
        int ret = compare(x.toCharArray(), y.toCharArray());
        Map<String, AtomicInteger> dp = ComparableVersion.DP.get(x);
        if (dp == null) dp = new HashMap<>();
        dp.put(y, new AtomicInteger(ret));
        ComparableVersion.DP.put(x, dp);
        return ret;
    }

    private int compare(char[] x, char[] y) {
        if (StringUtils.isInteger(new String(x)) && StringUtils.isInteger(new String(y)))
            return Integer.compare(Integer.parseInt(new String(x)), Integer.parseInt(new String(y)));
        else {
            int xIdx = skip_index_num(x) + 1;
            int yIdx = skip_index_num(y) + 1;
            if (xIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + x);
            if (yIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + y);
            int ret = compare(Arrays.copyOfRange(x, 0, xIdx), Arrays.copyOfRange(y, 0, yIdx));
            if (ret != 0) return ret;
            if (x.length == xIdx) return compare_default_str(y, yIdx);
            if (y.length == yIdx) return compare_default_str(x, xIdx);
            xIdx = push_index_alph(x, x[xIdx], xIdx) + 1;
            yIdx = push_index_alph(y, y[yIdx], yIdx) + 1;
            return chs_compare(Arrays.copyOfRange(x, xIdx, x.length), Arrays.copyOfRange(y, yIdx, y.length));
        }
    }

    private int compare_default_str(char[] chs, int idx) {
        return Integer.compare(ComparableVersion.VERSION_STR(new String(Arrays.copyOfRange(chs, push_index_alph(chs, chs[idx], idx), chs.length))), ComparableVersion.DEFAULT_VERSION_INT);
    }

    private int chs_compare(char[] x, char[] y) {
        return Integer.compare(ComparableVersion.VERSION_STR(new String(x)), ComparableVersion.VERSION_STR(new String(y)));
    }

    private int skip_index_num(char[] chs) {
        int idx = -1;
        for (int i = 0; i < chs.length; i++) {
            if (Character.isDigit(chs[i])) idx = i;
            else return idx;
        }
        return idx;
    }

    private int push_index_alph(char[] chs, char c, int idx) {
        if (idx >= chs.length) return -1;
        if (Character.isAlphabetic(c)) return idx;
        for (int i = idx; i < chs.length; i++) {
            if (chs[i] == c) return i;
        }
        return -1;
    }

    @Override
    public @NotNull Version clone() {
        return new ComparableVersion(this.version);
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparableVersion that = (ComparableVersion) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return version != null ? version.hashCode() : 0;
    }
}
