package org.cobnet.mc.diversifier.plugin.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.cobnet.mc.diversifier.utils.BooleanUtils;
import org.cobnet.mc.diversifier.utils.InvokeWith;
import org.cobnet.mc.diversifier.utils.LoopUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YAMLConfiguration extends AbstractConfiguration {

    private final File file;

    public YAMLConfiguration(File file) {
        super(InvokeWith.of(new ObjectMapper(new YAMLFactory())).invoke((mapper) -> {
            try {
                if(file.exists() && file.length() > 0) return mapper.readValue(file, HashMap.class);
                file.getParentFile().mkdir();
                file.createNewFile();
                return new HashMap<>();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        this.file = file;
    }

    @Override
    public @Nullable Object get(@NotNull String key) {
        return this.get(key, super.map);
    }

    private Object get(String key, Map<String, Object> map) {
        char[] chars = key.toCharArray();
        for(int i = 0, k = 0; i < chars.length; i++, k = BooleanUtils.toIntAsBinary(LoopUtils.isTouchEnd(i, chars.length))) {
            if(chars[i] == '.' || k != 0) {
                String name = new String(chars, 0, i + k), node = new String(chars, i + 1, chars.length - i - 1);
                Object val = map.get(name);
                if(val instanceof Map next) {
                    return get(node, next);
                }
                if(k != 0) return val;
            }
        }
        return null;
    }

    @Override
    public boolean hasKey(@NotNull String key) {
        return this.get(key) != null;
    }

    @Override
    public @Nullable Object set(@NotNull String key, @Nullable Object value) {
        return this.set(key, value, super.map);
    }

    private Object set(String key, Object value, Map<String, Object> map) {
        char[] chars = key.toCharArray();
        for(int i = 0, k = 0; i < chars.length; i++, k = BooleanUtils.toIntAsBinary(LoopUtils.isTouchEnd(i, chars.length))) {
            if(chars[i] == '.' || k != 0) {
                String name = new String(chars, 0, i + k), node = new String(chars, i + 1, chars.length - i - 1);
                Object val = map.get(name);
                if(val instanceof Map next) return set(node, value, next);
                if(k != 0) {
                    map.put(name, value);
                    return value;
                }
                Map<String, Object> next = new HashMap<>();
                map.put(name, next);
                return set(node, value, next);
            }
        }
        return null;
    }

    @Override
    public void save() throws IOException {
        this.file.getParentFile().mkdir();
        this.file.createNewFile();
        new ObjectMapper(new YAMLFactory()).writeValue(this.file, this.map);
    }

    @Override
    public void load() throws IOException {
        this.map.clear();
        if(this.file.exists()) this.map.putAll(new ObjectMapper(new YAMLFactory()).readValue(this.file, Map.class));
    }
}
