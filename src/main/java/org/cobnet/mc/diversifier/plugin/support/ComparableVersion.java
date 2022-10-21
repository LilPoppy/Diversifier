package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.plugin.Version;
import org.cobnet.mc.diversifier.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
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
                if (n != null) {
                    ret = n.get();
                } else ret = compare_dp(x[i], y[i]);

            } else ret = compare_dp(x[i], y[i]);
            if (ret != 0) return ret;
        }
        if (x.length < y.length) return -1;
        else if (x.length > y.length) return 1;
        return 0;
    }

    private int compare_dp(String x, String y) {
        int ret = compare(x, y);
        Map<String, AtomicInteger> dp = ComparableVersion.DP.get(x);
        if (dp == null) dp = new HashMap<>();
        dp.put(y, new AtomicInteger(ret));
        ComparableVersion.DP.put(x, dp);
        return ret;
    }

    private int compare(String x, String y) {
        if (StringUtils.isInteger(x) && StringUtils.isInteger(y))
            return Integer.compare(Integer.parseInt(x), Integer.parseInt(y));
        else {
            int xIdx = shift_num_index(x) + 1;
            int yIdx = shift_num_index(y) + 1;
            if (xIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + x);
            if (yIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + y);
            int ret = compare(x.substring(0, xIdx), y.substring(0, yIdx));
            if (ret != 0) return ret;
            if (x.length() == xIdx) return compare_default_str(y, yIdx);
            if (y.length() == yIdx) return compare_default_str(x, xIdx);
            xIdx = push_index(x, x.charAt(xIdx), xIdx) + 1;
            yIdx = push_index(y, y.charAt(yIdx), yIdx) + 1;
            return compare_str(x.substring(xIdx), y.substring(yIdx));
        }
    }

    private int compare_default_str(String s, int idx) {
        return Integer.compare(ComparableVersion.VERSION_STR(s.substring(push_index(s, s.charAt(idx), idx) + 1)), ComparableVersion.DEFAULT_VERSION_INT);
    }

    private int compare_str(String x, String y) {
        return Integer.compare(ComparableVersion.VERSION_STR(x), ComparableVersion.VERSION_STR(y));
    }

    private int shift_num_index(String s) {
        int idx = -1;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) idx = i;
            else return idx;
        }
        return idx;
    }

    private int push_index(String s, char c, int idx) {
        if (idx >= s.length()) return -1;
        if (Character.isAlphabetic(c)) return idx;
        for (int i = idx; i < s.length(); i++) {
            if (s.charAt(i) == c) return i;
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
