package org.cobnet.mc.diversifier.utils;

import java.util.regex.Pattern;

public class StringUtils {

    public static boolean isInteger(String s) {
        Pattern pattern = Pattern.compile("\\d+");
        return pattern.matcher(s).matches();
    }
}
