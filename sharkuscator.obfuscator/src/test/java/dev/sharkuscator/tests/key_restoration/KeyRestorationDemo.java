package dev.sharkuscator.tests.key_restoration;

import java.util.Random;

public class KeyRestorationDemo {
    private static final Random RANDOM_INSTANCE = new Random();

    private static final String[] PRIMARY_STRINGS;
    private static final String[] SECONDARY_STRINGS;

    private static final int[] PRIMARY_INTS;
    private static final int[] SECONDARY_INTS;

    static {
        PRIMARY_STRINGS = new String[2];
        PRIMARY_STRINGS[0] = "Hello";
        PRIMARY_STRINGS[1] = "ABCD";

        SECONDARY_STRINGS = new String[2];
        SECONDARY_STRINGS[0] = "Fih";
        SECONDARY_STRINGS[1] = "World";

        PRIMARY_INTS = new int[2];
        PRIMARY_INTS[0] = 668;
        PRIMARY_INTS[1] = 6969;

        SECONDARY_INTS = new int[2];
        SECONDARY_INTS[0] = 9999;
        SECONDARY_INTS[1] = 669;
    }

    public static void main(String[] args) {
        int i8 = -2;
        while (true) {
            int i9 = i8 + 2;
            int charAt = "가감,\u0083\u0090 Òwc\u0092TÚ\u0090Áb\tHd가갈3SL$*Öb¨각갈¹ª\u0006$j9âf갂감ë\u0089\u0082lVv\u0089t\u0006\u0016&ð\u0003\u007fs-".charAt(i9) - 44032;
            int charAt2 = "가감,\u0083\u0090 Òwc\u0092TÚ\u0090Áb\tHd가갈3SL$*Öb¨각갈¹ª\u0006$j9âf갂감ë\u0089\u0082lVv\u0089t\u0006\u0016&ð\u0003\u007fs-".charAt(i9 + 1) - 44032;
            System.out.println(i9 + " " + charAt + " " + charAt2);
            return;
        }

//        int[][] instructionData = {
//                {0, 0},
//                {1, 1},
//                {2, 0}
//        };
//
//        StringBuilder instructionBuilder = new StringBuilder();
//        for (int[] instruction : instructionData) {
//            instructionBuilder.append((char) instruction[0]); // Append selector
//            instructionBuilder.append((char) instruction[1]); // Append index
//        }
//
//        System.out.println(reconstructStringFromInstructions(instructionBuilder.toString()));
//        System.out.println(reconstructIntFromInstructions(instructionBuilder.toString()));
    }

    private static String reconstructStringFromInstructions(String instructionString) {
        String reconstructed = "";
        for (int i = 0; i < instructionString.length(); i += 2) {
            try {
                int selector = instructionString.charAt(i);
                int index = instructionString.charAt(i + 1);
                switch (selector) {
                    case 0:
                        reconstructed += PRIMARY_STRINGS[index];
                        break;
                    case 1:
                        reconstructed += SECONDARY_STRINGS[index];
                        break;
                    case 2:
                        reconstructed += PRIMARY_STRINGS[999];
                        break;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return reconstructed;
    }

    private static int reconstructIntFromInstructions(String instructionString) {
        int reconstructed = 0;
        for (int i = 0; i < instructionString.length(); i += 2) {
            try {
                int selector = instructionString.charAt(i);
                int index = instructionString.charAt(i + 1);
                switch (selector) {
                    case 0:
                        reconstructed += PRIMARY_INTS[index];
                        break;
                    case 1:
                        reconstructed += SECONDARY_INTS[index];
                        break;
                    case 2:
                        reconstructed += PRIMARY_INTS[999];
                        break;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return reconstructed;
    }
}
