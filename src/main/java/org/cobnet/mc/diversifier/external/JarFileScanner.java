package org.cobnet.mc.diversifier.external;

import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileScanner implements AutoCloseable {

    private final JarFile file;

    private final List<String> extensions;

    private final List<String> excludedPackages;

    public JarFileScanner(JarFile file, List<String> extensions, List<String> excludedPackages) {
        this.file = file;
        this.extensions = new ArrayList<>(extensions);
        this.excludedPackages = new ArrayList<>(excludedPackages);
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

    public JarFileScanner(String path, List<String> extensions, List<String> excludedPackages) throws IOException {
        this(new JarFile(path), extensions, excludedPackages);
    }

    public Map<String, List<String>> scan() {
        Map<String, List<String>> map = new HashMap<>();
        Enumeration<JarEntry> entries = file.entries();
        JarEntry entry;
        String meme = null, s;
        ArrayDeque<char[]> packages = new ArrayDeque<>();
        Iterator<String> it = this.excludedPackages.iterator();
        while(it.hasNext() && (s = it.next()) != null) packages.offer(s.toCharArray());
        char[] exclude = null;
        while(entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
            if (entry.isDirectory()) continue;
            String name = null, type = null;
            char[] chars = entry.getName().toCharArray();
            Stack<String> nodes = new Stack<>();
            Queue<char[]> excludes = packages.clone();
            for(int n = chars.length, i = n - 1, j = i; i >= 0; i--) {
                int idx = n - i - 1;
                if(exclude != null && idx < exclude.length && !chr_cmp(chars, exclude, idx)) exclude = null;
                if(exclude != null && idx >= exclude.length - 1 && exclude[exclude.length - 1] == '*') {
                    break;
                }
                if(exclude == null) {
                    for (int k = 0, m = excludes.size(); k < m; k++) {
                        char[] chs = excludes.poll();
                        if (chs == null) continue;
                        if (chr_cmp(chars, chs, idx)) excludes.offer(chs);
                    }
                } else {
                    excludes.clear();
                    excludes.offer(exclude);
                }
                if(excludes.size() == 1) exclude = excludes.poll();
                if(chars[i] == '.') {
                    type = new String(chars, i, n - i);
                    it = this.extensions.iterator();
                    do {
                        if(meme != null && !type.equalsIgnoreCase(meme)) {
                            meme = null;
                        }
                    } while (it.hasNext() && (meme = it.next()) != null);
                    if(meme == null) break;
                    j = i;
                    continue;
                }
                if(chars[i] == '/') {
                    String node = new String(chars, i + 1, j - i - 1);
                    if(name == null) {
                        name = node;
                        j = i;
                        continue;
                    }
                    nodes.push(node);
                    j = i;
                    continue;
                }
                if(i != 0) continue;
                if(name == null || type == null || nodes.size() == 0) continue;
                nodes.push(new String(chars, i, j - i));
                StringBuilder sb = new StringBuilder();
                boolean flag = nodes.isEmpty();
                while(!flag) {
                    sb.append(nodes.pop());
                    if(!(flag = nodes.isEmpty())) sb.append('.');
                }
                String key = sb.toString();
                List<String> files = map.getOrDefault(key, new ArrayList<>());
                files.add(name);
                map.put(key, files);
            }
        }
        return map;
    }

    private boolean chr_cmp(char[] chr1, char[] chr2, int idx) {
        char c1 = chr1[idx], c2 = chr2[idx];
        return c2 == '*' || (c1 == '/' && c2 == '.') || c1 == c2;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
