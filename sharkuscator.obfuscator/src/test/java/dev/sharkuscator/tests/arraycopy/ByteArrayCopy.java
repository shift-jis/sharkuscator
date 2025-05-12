package dev.sharkuscator.tests.arraycopy;

import java.util.Arrays;

public class ByteArrayCopy {
    private static final byte[] src1 = new byte[]{1, 2, 3};
    private static final byte[] src2 = new byte[]{4, 5};
    private static final byte[] src3 = new byte[]{6};
    private static final byte[] src4 = new byte[]{7, 8};

    public static void main(String[] args) {
        byte[] dest = new byte[8];
        System.arraycopy(src1, 0, dest, 0, src1.length);
        System.arraycopy(src2, 0, dest, 3, src2.length);
        System.arraycopy(src3, 0, dest, 5, src3.length);
        System.arraycopy(src4, 0, dest, 6, src4.length);
        System.out.println(Arrays.toString(dest));
    }
}
