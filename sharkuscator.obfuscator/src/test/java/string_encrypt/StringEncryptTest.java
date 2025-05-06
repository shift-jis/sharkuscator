package string_encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class StringEncryptTest {
    private static final Map<Long, Object[]> THREAD_LOCAL_CIPHER_OBJECTS = new HashMap<>();
    private static final String[] OBFUSCATED_STRINGS = new String[10];
    private static final byte[] KEY_BYTES = Base64.getDecoder().decode("+hpRpVtclcBQbZ8xNcHxcPbEB+oupykQkmK7OLJHs7L9MnXkaB8LRw==");

    public static void main(String[] args) {
        OBFUSCATED_STRINGS[0] = obfuscateString("ABCD Test");
        System.out.println(OBFUSCATED_STRINGS[0]);
        System.out.println(deobfuscateString(0));
    }

    public static String obfuscateString(String string) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[8]);
            Cipher desCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

            System.out.println(KEY_BYTES.length);
            desCipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(new DESKeySpec(KEY_BYTES)), ivSpec);
            return new String(desCipher.doFinal(string.getBytes(StandardCharsets.ISO_8859_1)), StandardCharsets.ISO_8859_1);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static String deobfuscateString(int i) {
        try {
            long currentThreadId = Thread.currentThread().getId();
            Object[] cipherObjects = THREAD_LOCAL_CIPHER_OBJECTS.get(currentThreadId);
            if (cipherObjects == null) {
                cipherObjects = new Object[]{
                        Cipher.getInstance("DES/CBC/PKCS5Padding"),
                        SecretKeyFactory.getInstance("DES"),
                        new IvParameterSpec(new byte[8])
                };
                THREAD_LOCAL_CIPHER_OBJECTS.put(currentThreadId, cipherObjects);
            }

            SecretKeyFactory keyFactory = (SecretKeyFactory) cipherObjects[1];
            IvParameterSpec ivSpec = (IvParameterSpec) cipherObjects[2];
            Cipher desCipher = (Cipher) cipherObjects[0];

            desCipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(new DESKeySpec(KEY_BYTES)), ivSpec);
            return new String(desCipher.doFinal(OBFUSCATED_STRINGS[i].getBytes(StandardCharsets.ISO_8859_1)));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
