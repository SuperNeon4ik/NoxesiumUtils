package me.superneon4ik.noxesiumutils.modules;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class FriendlyByteBufRef {
    private final Logger logger;
    private final Deobfuscator deobfuscator;

    public FriendlyByteBufRef(Logger logger, Deobfuscator deobfuscator) {
        this.logger = logger;
        this.deobfuscator = deobfuscator;

        testBasicFunctionality();
    }

    public void testBasicFunctionality() {
        logger.info("Testing FriendlyByteBuf functionality...");

        testUnobfuscatedMethod();
        testObfuscatedMethod1();
        testObfuscatedMethod2();
    }

    private void testObfuscatedMethod2() {
        Method writeJsonWithCodecMethod = deobfuscator.getMethod(
                "net/minecraft/network/FriendlyByteBuf",
                "writeJsonWithCodec",
                "(Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V"
        );

        if (writeJsonWithCodecMethod != null)
            logger.info(String.format("Successfully found method: %s (writeJsonWithCodec)", writeJsonWithCodecMethod.getName()));
        else
            logger.warning("Failed to find method: writeJsonWithCodec");
    }

    private void testObfuscatedMethod1() {
        Method writeVarIntMethod = deobfuscator.getMethod(
                "net/minecraft/network/FriendlyByteBuf",
                "writeVarInt",
                "(I)Lnet/minecraft/network/FriendlyByteBuf;"
        );

        if (writeVarIntMethod != null)
            logger.info(String.format("Successfully found method: %s (writeVarInt)", writeVarIntMethod.getName()));
        else
            logger.warning("Failed to find method: writeVarInt");
    }

    private void testUnobfuscatedMethod() {
        Method readIntMethod = deobfuscator.getMethod(
                "net/minecraft/network/FriendlyByteBuf",
                "readInt",
                "()[I"
        );

        if (readIntMethod != null)
            logger.info(String.format("Successfully found method: %s (readInt)", readIntMethod.getName()));
        else
            logger.warning("Failed to find method: readInt");
    }

}
