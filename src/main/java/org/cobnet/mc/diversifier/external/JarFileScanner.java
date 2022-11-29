package org.cobnet.mc.diversifier.external;

import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileScanner implements AutoCloseable {

    private final JarFile file;

    private final List<String> extensions;

    private final List<String> packages;

    public JarFileScanner(JarFile file, List<String> extensions, List<String> excluded) {
        this.file = file;
        this.extensions = new ArrayList<>(extensions);
        this.packages = new ArrayList<>(excluded);
    }

    public JarFileScanner(JarFile file) {
        this(file, Collections.singletonList(".class"), Collections.singletonList("META-INF.*"));
    }

    public JarFileScanner(String path) throws IOException {
        this(new JarFile(path));
    }

    public JarFileScanner(String path, List<String> extensions) throws IOException {
        this(path, extensions, Collections.singletonList("META-INF.*"));
    }

    public JarFileScanner(String path, List<String> extensions, List<String> packages) throws IOException {
        this(new JarFile(path), extensions, packages);
    }

    public Map<String, List<String>> scan() {
        Map<String, List<String>> map = new HashMap<>();
        Enumeration<JarEntry> entries = file.entries();
        JarEntry entry;
        String extension = null, s;
        ArrayDeque<char[]> packages = new ArrayDeque<>();
        Iterator<String> it = this.packages.iterator();
        while(it.hasNext() && (s = it.next()) != null) packages.offer(s.toCharArray());
        char[] exclude = null;
        while(entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
            if (entry.isDirectory()) continue;
            char[] chars = entry.getName().toCharArray(), name = null, type = null;
            Stack<String> nodes = new Stack<>();
            Queue<char[]> excludes = packages.clone();
            for(int n = chars.length, i = n - 1, j = i; i >= 0; i--) {
                int idx = n - i - 1;
                if(exclude != null && idx < exclude.length && !is_excluded(chars, exclude, idx)) exclude = null;
                if(exclude != null && idx == exclude.length && (exclude[idx - 1] == '*' || is_name(chars, name, idx))) break;
                if(exclude == null) {
                    for (int k = 0; k < excludes.size(); k++) {
                        char[] chs = excludes.poll();
                        if (chs == null) continue;
                        if (idx < chs.length && is_excluded(chars, chs, idx)) excludes.offer(chs);
                    }
                }
                if(excludes.size() == 1) exclude = excludes.poll();
                if(chars[i] == '.') {
                    type = Arrays.copyOfRange(chars, i, j + 1);
                    it = this.extensions.iterator();
                    do if(extension != null && !Arrays.equals(type, extension.toCharArray())) extension = null;
                    while (it.hasNext() && (extension = it.next()) != null);
                    if(extension == null) break;
                    j = i;
                    continue;
                }
                if(chars[i] == '/') {
                    char[] node = Arrays.copyOfRange(chars, i + 1, j);
                    if(name == null) {
                        name = node;
                        j = i;
                        continue;
                    }
                    nodes.push(new String(node));
                    j = i;
                    continue;
                }
                if(i != 0) continue;
                if(name == null) name = Arrays.copyOfRange(chars, i, j - 1);
                else nodes.push(new String(chars, i, j - i));
                if(type == null) continue;
                StringBuilder sb = new StringBuilder();
                boolean empty = false;
                while(!empty) {
                    sb.append(nodes.pop());
                    if(!(empty = nodes.isEmpty())) sb.append('.');
                }
                String key = sb.toString();
                if(exclude != null && Arrays.equals(exclude, key.toCharArray())) break;
                List<String> files = map.getOrDefault(key, new ArrayList<>());
                files.add(new String(name));
                map.put(key, files);
            }
        }
        return map;
    }


    private boolean is_excluded(char[] chr1, char[] chr2, int idx) {
        char c1 = chr1[idx], c2 = chr2[idx];
        return c2 == '*' || (c1 == '/' && c2 == '.') || c1 == c2;
    }

    private boolean is_name(char[] chr1, char[] chr2, int idx) {
        if(chr2 == null) return true;
        while(chr1[idx] == '/') idx++;
        for(int i = 0; i < chr2.length; i++) {
            if(chr1[idx + i] != chr2[i]) return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
