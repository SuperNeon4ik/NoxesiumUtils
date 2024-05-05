package me.superneon4ik.noxesiumutils.modules;

import me.superneon4ik.noxesiumutils.objects.ObfuscatedMethodInfo;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Deobfuscator {
    private final Logger logger;
    private final Map<String, String> classMappings = new HashMap<>();
    private final ArrayList<ObfuscatedMethodInfo> methodMappings = new ArrayList<>();

    public Deobfuscator(Logger logger, List<String> classes) {
        this.logger = logger;

        logger.info("Reading mappings file...");
        try (InputStream in = Bukkit.getServer().getClass().getResourceAsStream("/META-INF/mappings/reobf.tiny")) {
            assert in != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                AtomicReference<String> classUnobfName = new AtomicReference<>();
                AtomicReference<String> classObfName = new AtomicReference<>();
                reader.lines().forEach(line -> {
                    if (line.startsWith("c")) {
                        String[] tokens = line.split("\t");
                        classUnobfName.set(tokens[1]);
                        classObfName.set(tokens[2]);
                        classMappings.put(tokens[2], tokens[1]);
                    }
                    else if (line.startsWith("\tm")) {
                        if (classes.contains(classUnobfName.get())) {
                            String[] tokens = line.split("\t");
                            String signature = tokens[2];
                            String unobf = tokens[3];
                            String obf = tokens[4];

                            methodMappings.add(new ObfuscatedMethodInfo(classObfName.get(), unobf, obf, signature));
                        }
                    }
                });
            }
        } catch (IOException e) {
            logger.warning("Could not read the mappings file.");
        }

        logger.info("Mapping methods...");
        methodMappings.forEach(methodInfo -> {
            String className = getObfuscatedClassOrDefault(methodInfo.getClassName(), methodInfo.getClassName());

            try {
                Class<?> clazz = Class.forName(className.replace('/', '.'));
                Method method = clazz.getMethod(methodInfo.getObfuscatedName(), getClassesFromDeobfuscated(methodInfo.getSignature()));
                methodInfo.setMethod(method);

                logger.info(String.format("Mapped method %s%s (in %s) to %s", methodInfo.getUnobfuscatedName(), methodInfo.getSignature(), methodInfo.getClassName(), method.getName()));
            } catch (ClassNotFoundException e) {
                logger.warning(String.format("Couldn't find class %s (%s) when searching for method %s%s - %s", className, methodInfo.getClassName(), methodInfo.getUnobfuscatedName(), methodInfo.getSignature(), e.getLocalizedMessage()));
            } catch (NoSuchMethodException e) {
                logger.warning(String.format("Couldn't find method %s%s - %s", methodInfo.getUnobfuscatedName(), methodInfo.getSignature(), e.getLocalizedMessage()));
            }
        });

        logger.info("Finished initializing the deobfuscator!");
    }

    @Nullable
    public Method getMethod(String className, String methodName, String signature) {
        className = className.replace('.', '/');
        String obfClassName = getObfuscatedClassOrDefault(className, className);
        logger.info(String.format("Searching for %s%s in '%s'", methodName, signature, obfClassName));

        for (ObfuscatedMethodInfo methodMapping : methodMappings) {
            if (methodMapping.getClassName().equals(obfClassName) && methodMapping.getUnobfuscatedName().equals(methodName) &&
                methodMapping.getSignature().equals(signature)) {
                return methodMapping.getMethod();
            }
        }

        try {
            Class<?> clazz = Class.forName(obfClassName.replace('/', '.'));
            return clazz.getMethod(methodName, getClassesFromDeobfuscated(signature));
        } catch (ClassNotFoundException e) {
            logger.warning(String.format("Couldn't find class: %s (%s)", obfClassName, className));
        } catch (NoSuchMethodException e) {
            logger.warning(String.format("Couldn't find method %s in class %s", methodName, obfClassName));
        }

        return null;
    }

    public Class<?>[] getClassesFromDeobfuscated(String signature) {
        Matcher matcher = Pattern.compile("\\(.*\\)").matcher(signature);
        if (matcher.matches()) {
            Matcher parametersMatcher = Pattern.compile("\\(.*\\)").matcher(matcher.group(1));
            if (parametersMatcher.matches()) {
                Class<?>[] classes = new Class<?>[parametersMatcher.groupCount() - 1];
                for (int i = 1; i < parametersMatcher.groupCount(); i++) {
                    try {
                        String className = getObfuscatedClassOrDefault(parametersMatcher.group(i), parametersMatcher.group(i));
                        Class<?> clazz = Class.forName(className.replace('/', '.'));
                        classes[i - 1] = clazz;
                    } catch (ClassNotFoundException e) {
                        logger.warning(String.format("Couldn't obfuscate class '%s'", parametersMatcher.group(i)));
                        classes[i - 1] = void.class;
                    }
                }
                return classes;
            }
        }
        return new Class[0];
    }

    public String getObfuscatedClassOrDefault(String key, String defaultValue) {
        for (Map.Entry<String, String> entry : classMappings.entrySet()) {
            if (entry.getValue().equals(key)) {
                return entry.getKey();
            }
        }
        return defaultValue;
    }
}
