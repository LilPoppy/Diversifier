package org.cobnet.mc.diversifier.external;

import org.cobnet.mc.diversifier.utils.BooleanUtils;

import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileScanner implements AutoCloseable {

    private final static String[] IGNORED_PACKAGE = { "META-INF" };
    private final JarFile file;

    public JarFileScanner(JarFile file) {
        this.file = file;
    }

    public JarFileScanner(String path) throws IOException {
        this(new JarFile(path));
    }
    public Map<String, List<String>> scan() {
        Map<String, List<String>> map = new HashMap<>();
        JarEntry entry;
        Enumeration<JarEntry> entries = file.entries();
        while(entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
            char[] chars = entry.getName().toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, j = 0, n = chars.length, k = 0; i < n; i++, k = BooleanUtils.toInt(n - 1 == i)) {
                if(chars[i] == '/' || k > 0) {
                    String name = new String(chars, j, i - j + k);
                    boolean isContinue = true;
                    for(String ignored : JarFileScanner.IGNORED_PACKAGE) {
                        if(ignored.equals(name)) {
                            isContinue = false;
                            break;
                        }
                    }
                    if(!isContinue) break;
                    if(k > 0) {
                        int length = sb.length();
                        String key = sb.deleteCharAt(length - 1).toString();
                        List<String> files = map.get(key);
                        if(files == null) files = new ArrayList<>();
                        files.add(name);
                        map.put(key, files);
                        break;
                    }
                    sb.append(name).append(".");
                    j = i + 1;
                }
            }
        }
        return map;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
