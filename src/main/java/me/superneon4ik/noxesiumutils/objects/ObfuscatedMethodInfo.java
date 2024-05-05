package me.superneon4ik.noxesiumutils.objects;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Setter
@Getter
public class ObfuscatedMethodInfo {
    String className, unobfuscatedName, obfuscatedName, signature;
    Method method = null;

    public ObfuscatedMethodInfo(String className, String unobfuscatedName, String obfuscatedName, String signature) {
        this.className = className;
        this.unobfuscatedName = unobfuscatedName;
        this.obfuscatedName = obfuscatedName;
        this.signature = signature;
    }
}
