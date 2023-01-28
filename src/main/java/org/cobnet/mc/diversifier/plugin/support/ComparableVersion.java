package org.cobnet.mc.diversifier.plugin.support;

import lombok.Getter;
import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.PluginConfiguration;
import org.cobnet.mc.diversifier.plugin.Version;
import org.cobnet.mc.diversifier.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public record ComparableVersion(@Getter String version) implements Version {

    public ComparableVersion(int... version) {
        this(String.join(".", Arrays.stream(version).mapToObj(String::valueOf).toArray(String[]::new)));
    }

    private final static Map<String, Map<String, AtomicInteger>> DP = new HashMap<>();

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
        Integer xInt = to_int(x), yInt = to_int(y);
        if (xInt != null && yInt != null) return Integer.compare(xInt, yInt);
        else {
            int xIdx = skip_index(x) + 1;
            int yIdx = skip_index(y) + 1;
            if (xIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + x);
            if (yIdx == -1) throw new UnknownFormatConversionException("Unknown format: " + y);
            int ret = compare(Arrays.copyOfRange(x, 0, xIdx), Arrays.copyOfRange(y, 0, yIdx));
            if (ret != 0) return ret;
            PluginConfiguration config = Diversifier.getConfiguration();
            if (x.length == xIdx) {
                String name = config.getVersionDefault();
                return compare_chs(name.toCharArray(), Arrays.copyOfRange(y, push_index(y, y[yIdx], yIdx), y.length));
            }
            if (y.length == yIdx) {
                String name = config.getVersionDefault();
                return compare_chs(Arrays.copyOfRange(y, push_index(y, y[yIdx], yIdx), y.length), name.toCharArray());
            }
            xIdx = push_index(x, x[xIdx], xIdx);
            yIdx = push_index(y, y[yIdx], yIdx);
            return compare_chs(Arrays.copyOfRange(x, xIdx, x.length), Arrays.copyOfRange(y, yIdx, y.length));
        }
    }



    private int compare_chs(char[] x, char[] y) {
        PluginConfiguration config = Diversifier.getConfiguration();
        List<String> order = config.getVersionOrder();
        return Integer.compare(order.indexOf(new String(x)), order.indexOf(new String(y)));
    }

    private int skip_index(char[] chs) {
        int idx = -1;
        for (int i = 0; i < chs.length; i++) {
            if (Character.isDigit(chs[i])) idx = i;
            else return idx;
        }
        return idx;
    }

    private int push_index(char[] chs, char c, int idx) {
        if (idx >= chs.length) return -1;
        if (Character.isAlphabetic(c)) return idx;
        while(idx < chs.length) {
            if (Character.isAlphabetic(chs[idx])) return idx;
            idx++;
        }
        return -1;
    }

    private Integer to_int(char[] chars) {
        if(chars.length == 0) return null;
        int ret = 0;
        for(int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(!Character.isDigit(c)) return null;
            ret = (ret * 10) + Character.getNumericValue(c);
        }
        return ret;
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
