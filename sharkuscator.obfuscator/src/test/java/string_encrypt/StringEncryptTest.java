package string_encrypt;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class StringEncryptTest {
    private static final Map<Long, Object[]> THREAD_LOCAL_CIPHER_OBJECTS = new HashMap<>();
    private static final byte[] KEY_BYTES = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
    private static final String[] WORDS = new String[]{"hello", "world", "あいうえおかきくけこくけこさしすせそたちつてとな"};

    private static final String[] deobfuscatedStrings;
    private static final String[] obfuscatedStrings;

    private static final IvParameterSpec initializationVectorSpec = new IvParameterSpec(new byte[8]);
    private static final SecretKeyFactory secretKeyFactory;
    private static final Cipher cipher;

    static {
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("DES");
            cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        deobfuscatedStrings = new String[WORDS.length];
        obfuscatedStrings = new String[WORDS.length];
    }

    public static void main(String[] args) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException {
//        for (int i = 0; i < WORDS.length; i++) {
//            obfuscatedStrings[i] = obfuscateString(WORDS[i]);
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < obfuscatedStrings.length; i++) {
//            stringBuilder.append(obfuscatedStrings[i]);
//            if (obfuscatedStrings.length > i + 1) {
//                stringBuilder.append((char) obfuscatedStrings[i + 1].length());
//            }
//        }
//
//        System.out.println(stringBuilder);

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

        byte[] byteArray = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteArray[i] = (byte) i;
        }

        cipher.init(Cipher.DECRYPT_MODE, secretKeyFactory.generateSecret(new DESKeySpec(byteArray)), new IvParameterSpec(new byte[8]));

        String packedStringsData = "Ø\u0085í\u009D\"\u001D0F×Ëó\u0019\u0005\u0015\u0096V ÛæÊ{GSTU©\u008D\u000F\u009B6¥~EÝì¸QÃ/ðòVø\u009F\u001C1B&v";
        int packedDataLength = packedStringsData.length();
        int currentSegmentLength = 18;
        int currentReadPosition = -1;
        int outputStringIndex = 0;

        while (true) {
            int segmentStartIndex = ++currentReadPosition;
            byte[] decryptedSegmentBytes = cipher.doFinal(packedStringsData.substring(segmentStartIndex, segmentStartIndex + currentSegmentLength).getBytes(StandardCharsets.ISO_8859_1));
            deobfuscatedStrings[outputStringIndex++] = new String(decryptedSegmentBytes, StandardCharsets.UTF_8);
            if ((currentReadPosition += currentSegmentLength) >= packedDataLength) {
                System.out.println("End");
                return;
            }
            currentSegmentLength = packedStringsData.charAt(currentReadPosition);
        }
    }

    public static String funny(int i) {
        return deobfuscatedStrings[i];
    }

    public static String obfuscateString(String string) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeyFactory.generateSecret(new DESKeySpec(KEY_BYTES)), initializationVectorSpec);
            return new String(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)), StandardCharsets.ISO_8859_1);
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
            return new String(desCipher.doFinal(obfuscatedStrings[i].getBytes(StandardCharsets.UTF_8)));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
