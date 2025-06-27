package dev.sharkuscator.tests.long_constant.masking;

import java.util.Random;

public class LongMaskingDemo {
    private static final Random RANDOM_INSTANCE = new Random();
    public static final int[] TRANSFORMATION_TABLE = { 5, -3, 12, 0, -7, 2, 8, -1, 0, 15, -4, 11 };

    public static void main(String[] args) {
        long originalValue = RANDOM_INSTANCE.nextLong();
        System.out.println("Original Value: " + originalValue);

        long primaryTransformKey = 0xDEADBEEFCAFEBABEL;
        long primaryMaskingKey = 0x1A2B3C4D5E6F7890L;

        long obfuscatedValue = obfuscateNumber(originalValue, primaryMaskingKey, primaryTransformKey);
        System.out.println("Obfuscated Value: " + obfuscatedValue);

        long associatedValue1 = obfuscateNumber(primaryMaskingKey, 0x133713371337L, 0x454545454545L);
        MaskedNumber associatedNumber1 = MaskedNumber.resolveConfigurator(associatedValue1).registerAssociated(0x133713371337L, 0x454545454545L);

        long associatedValue2 = obfuscateNumber(primaryTransformKey, 0x133713371337L, 0x454545454545L);
        MaskedNumber associatedNumber2 = MaskedNumber.resolveConfigurator(associatedValue2).registerAssociated(0x133713371337L, 0x454545454545L);

        MaskedNumber maskedNumber = MaskedNumber.resolveConfigurator(obfuscatedValue).registerAssociated(associatedNumber1, associatedNumber2);
        System.out.println(maskedNumber.deobfuscation());
    }

    private static long obfuscateNumber(long originalValue, long maskingKey, long transformKey) {
        long intermediateValue = originalValue;
        for (int index = TRANSFORMATION_TABLE.length - 1; index >= 0; index--) {
            int transformation = TRANSFORMATION_TABLE[index];
            if (transformation > 0) {
                intermediateValue = (intermediateValue << transformation) | (intermediateValue >>> (64 - transformation));
            } else if (transformation < 0) {
                intermediateValue = (intermediateValue >>> -transformation) | (intermediateValue << (64 - -transformation));
            } else {
                intermediateValue ^= (transformKey * (long)(index + 1));
            }
        }
        return intermediateValue ^ maskingKey;
    }
}
