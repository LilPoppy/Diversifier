package org.cobnet.mc.diversifier.reference;

import org.cobnet.mc.diversifier.Diversifier;
import org.cobnet.mc.diversifier.plugin.support.YAMLConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class GlobalPluginConfiguration {

    public static final String PLUGIN_CONFIGURATION_DIRECTORY = Diversifier.class.getSimpleName();
    public static final String PLUGIN_CONFIGURATION_FILE_NAME = "config.yml";

    @ConfigurationProperty(name = "plugin.class-loader.file-extensions")
    public static final String[] CLASS_LOADER_FILE_EXTENSIONS = { ".class" };

    @ConfigurationProperty(name = "plugin.class-loader.excluded-packages")
    public static final String[] CLASS_LOADER_EXCLUDED_PACKAGES = { "lombok.*", "org.intellij.*", "org.jetbrains.*", "org.yaml.*", "com.fasterxml.*", "net.bytebuddy.*", "META-INF.*" };

    public static void writeYAML(File file) throws IllegalAccessException, IOException {
        YAMLConfiguration configuration = new YAMLConfiguration(file);
        for(Field field : GlobalPluginConfiguration.class.getDeclaredFields()) {
            ConfigurationProperty property = field.getAnnotation(ConfigurationProperty.class);
            if(property == null) continue;
            configuration.set(property.name(), field.get(null));
        }
        configuration.save();
    }


}
