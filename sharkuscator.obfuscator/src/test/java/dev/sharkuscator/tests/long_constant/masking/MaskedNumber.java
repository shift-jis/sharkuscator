package dev.sharkuscator.tests.long_constant.masking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class MaskedNumber {
    private final MaskedNumber[] associatedObjects = new MaskedNumber[2];
    private final int[] transformationTable = {5, -3, 12, 0, -7, 2, 8, -1, 0, 15, -4, 11};
    private final long obfuscatedValue;

    public static LongMaskingConfigurator resolveConfigurator(long obfuscatedValue) {
        return new LongMaskingConfigurator(of(obfuscatedValue));
    }

    public static MaskedNumber of(long obfuscatedValue) {
        return new MaskedNumber(obfuscatedValue);
    }

    public void registerAssociated(MaskedNumber associatedObject1, MaskedNumber associatedObject2) {
        associatedObjects[0] = associatedObject1;
        associatedObjects[1] = associatedObject2;
    }

    public long deobfuscation() {
        if (associatedObjects[0] == null || associatedObjects[1] == null) {
            return obfuscatedValue;
        }

        long intermediateValue = obfuscatedValue ^ associatedObjects[0].deobfuscation();
        for (int index = 0; index < transformationTable.length; index++) {
            int transformation = transformationTable[index];
            if (transformation > 0) {
                intermediateValue = (intermediateValue >>> transformation) | (intermediateValue << (64 - transformation));
            } else if (transformation < 0) {
                intermediateValue = (intermediateValue << -transformation) | (intermediateValue >>> (64 - -transformation));
            } else {
                intermediateValue ^= (associatedObjects[1].deobfuscation() * (long) (index + 1));
            }
        }
        return intermediateValue;
    }

//    public long getValue() {
//        if (deobfuscationKey == 0 || transformationTable == null) {
//            throw new IllegalStateException("ObfuscatedLong is not configured yet.");
//        }
//
//        long intermediateValue = obfuscatedValue ^ deobfuscationKey;
//        for (int index = 0; index < transformationTable.length; index++) {
//            int transformation = transformationTable[index];
//            if (transformation > 0) {
//                intermediateValue = (intermediateValue >>> transformation) | (intermediateValue << (64 - transformation));
//            } else if (transformation < 0) {
//                intermediateValue = (intermediateValue << -transformation) | (intermediateValue >>> (64 - -transformation));
//            } else {
//                intermediateValue ^= (0xDEADBEEFCAFEBABEL * (long) (index + 1));
//            }
//        }
//        return intermediateValue;
//    }
}
