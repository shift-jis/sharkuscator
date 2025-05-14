package dev.sharkuscator.tests.key_restoration;

import java.util.Arrays;
import java.util.Random;

public class KeyRestoration {
    private static final Random random = new Random();
    private static byte[] restored = new byte[8];

    private static final int RANDOMIZED_SHIFT_VALUE = random.nextInt(64);
    private static final long seed = random.nextLong();

    public static void main(String[] args) {
        byte[] bArr4 = new byte[8];
        for (int i4 = 0; i4 < bArr4.length; i4++) {
            long leftShiftAmount = ((i4 + 2) % 8) * 8;
            long shiftedSeed = seed << leftShiftAmount;
            byte val = (byte) (shiftedSeed >>> RANDOMIZED_SHIFT_VALUE);
            if (val == 0) {
                byte replacementByte;
                do {
                    replacementByte = (byte) random.nextInt();
                } while (replacementByte == 0);
                val = replacementByte;
            }
            bArr4[i4] = val;
        }
        System.out.println(Arrays.toString(bArr4));
    }
}
