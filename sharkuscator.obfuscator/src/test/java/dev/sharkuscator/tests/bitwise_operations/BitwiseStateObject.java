package dev.sharkuscator.tests.bitwise_operations;

import java.util.Vector;

public class BitwiseStateObject implements OperationTarget {

    private static final Object poolLock = new Object();
    private static final int SHIFT_LIMIT_H = 51;
    private static final int SORT_DEPTH_N = 17;
    private static final int POOL_BATCH_L = 128;

    public static int[] staticShiftMap;
    private static final Vector<BitwiseStateObject> instancePool;
    private static final Vector<Object> associatedObjects;
    private static int initialPoolSize;
    private static final long[] staticBitMasks = new long[64];

    static {
        long bit = 1L;
        for (int i = 0; i < 64; i++) {
            staticBitMasks[i] = bit;
            bit <<= 1;
        }
        associatedObjects = new Vector<>();
        instancePool = new Vector<>();
        populateInitialPool();
        initialPoolSize = instancePool.size();
        sortInstancePool();
    }

    private long stateValue;
    private OperationTarget nextTarget;
    private long auxiliaryStateValue;
    private final int[] instanceShiftMap;
    private final long[] instanceBitMasks;

    private BitwiseStateObject(long stateValue) {
        this.stateValue = stateValue;
        this.instanceShiftMap = staticShiftMap;
        this.instanceBitMasks = staticBitMasks;
    }

    public static OperationTarget createAndRegisterInstance(long value1, long value2, Object associatedObject) {
        OperationTarget linkedTarget = StateConfigurator.mapOperationTargets(createNewInstance(value1), createNewInstance(value2));
        if (associatedObject != null) {
            associatedObjects.addElement(associatedObject);
        }
        return linkedTarget;
    }

    static OperationTarget getInstanceFromPool(long value) {
        int poolIndex = (int) applyShiftAndMask(value, SHIFT_LIMIT_H, 63, staticShiftMap, staticBitMasks);

        if (poolIndex < instancePool.size()) {
            return instancePool.elementAt(poolIndex);
        }

        if (instancePool.size() % POOL_BATCH_L == 0) {
            staticShiftMap = staticShiftMap.clone();
        }

        BitwiseStateObject newInstance = new BitwiseStateObject(value);
        instancePool.addElement(newInstance);
        return newInstance;
    }

    private static OperationTarget createNewInstance(long value) {
        return new BitwiseStateObject(value);
    }

    static void setupInitialPoolAndConfigurator(StateConfigurator configurator) {
        initialPoolSize = instancePool.size();
        sortInstancePool();
        configurator.applyConfiguratorSettingsPhase1();
    }

    static void applyConfiguratorShiftMap(StateConfigurator configurator) {
        staticShiftMap = new int[]{-40, -44, -2, -23, 2, -43, -36, -8, -53, -35, -11, -6, -18, -5, -40, 8, -22, 6, 5, -36, -7, 11, -3, -9, -27, 3, 23, 7, -13, -5, 18, -32, 9, -19, 5, -22, -24, -22, 22, -8, 40, 13, 36, -3, 35, 44, 3, 8, 43, -13, -3, 27, 19, 3, 40, 36, -2, 22, 2, 22, 24, 53, 13, 32};
        configurator.applyConfiguratorSettingsPhase2();
    }

    private static long applyShiftAndMask(long value, int startBit, int endBit, int[] shiftMap, long[] masks) {
        long resultValue = 0L;
        int mapLength = shiftMap.length;

        for (int i = 0; i < mapLength; i++) {
            long maskedBit = value & masks[i];
            int shiftAmount = shiftMap[i];

            if (maskedBit != 0) {
                if (shiftAmount > 0) {
                    maskedBit >>>= shiftAmount;
                } else if (shiftAmount < 0) {
                    maskedBit <<= -shiftAmount; // Simplified from (~shiftAmount) + 1
                }
                resultValue |= maskedBit;
            }
        }

        // Simplified bit extraction
        if (startBit == 0 && endBit == 63) {
            return resultValue;
        }
        int length = endBit - startBit + 1;
        if (length <= 0 || length > 64) { // Basic validation, adjust if specific error handling is needed
            return 0L; // Or throw an exception
        }
        long extractMask = (1L << length) - 1L;
        return (resultValue >>> startBit) & extractMask;
    }

    private static void sortInstancePool() {
        int size = instancePool.size();
        if (size == 0) return;
        Vector<BitwiseStateObject> tempVector = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            tempVector.addElement(null); // Initialize with nulls or copy elements
        }
        // It's safer to copy elements if mergeSortMerge expects tempVector to have them
        for (int i = 0; i < size; i++) {
            tempVector.setElementAt(instancePool.elementAt(i), i);
        }
        mergeSortDivide(0, size - 1, instancePool, tempVector, 0);
    }

    private static void mergeSortDivide(int low, int high, Vector<BitwiseStateObject> sourceVector, Vector<BitwiseStateObject> tempVector, int depth) {
        if (low < high) {
            int mid = low + ((high - low) / 2);
            int nextDepth = depth + 1;

            if (nextDepth < SORT_DEPTH_N) {
                mergeSortDivide(low, mid, sourceVector, tempVector, nextDepth);
                mergeSortDivide(mid + 1, high, sourceVector, tempVector, nextDepth);
            }
            mergeSortMerge(low, mid, high, sourceVector, tempVector);
        }
    }

    private static void mergeSortMerge(int low, int mid, int high, Vector<BitwiseStateObject> sourceVector, Vector<BitwiseStateObject> tempVector) {
        for (int i = low; i <= high; i++) {
            tempVector.setElementAt(sourceVector.elementAt(i), i);
        }

        int leftPtr = low;
        int rightPtr = mid + 1;
        int currentPtr = low;

        while (leftPtr <= mid && rightPtr <= high) {
            if (tempVector.elementAt(leftPtr).isOrderedBefore(tempVector.elementAt(rightPtr))) {
                sourceVector.setElementAt(tempVector.elementAt(leftPtr), currentPtr++);
                leftPtr++;
            } else {
                sourceVector.setElementAt(tempVector.elementAt(rightPtr), currentPtr++);
                rightPtr++;
            }
        }
        while (leftPtr <= mid) {
            sourceVector.setElementAt(tempVector.elementAt(leftPtr), currentPtr++);
            leftPtr++;
        }
    }

    private static void populateInitialPool() {
        staticShiftMap = new int[]{-49, -7, -33, -60, -20, -57, -31, -11, 7, -20, -21, -15, -8, -40, -7, -26, -6, -44, 11, -23, 8, 7, 6, -9, 20, -13, 15, -31, -29, 20, -3, 21, 9, 3, -6, 33, -16, 31, 13, -21, 6, 26, 23, -1, 1, -6, -8, -9, -7, 49, -9, 6, 16, 40, 8, 7, 9, 29, 31, 9, 21, 44, 57, 60};
        instancePool.addElement(new BitwiseStateObject(4571342691005442043L));
        instancePool.addElement(new BitwiseStateObject(1486187967619834258L));
        instancePool.addElement(new BitwiseStateObject(-4833259550567966308L));
        instancePool.addElement(new BitwiseStateObject(5853013420897962686L));
        instancePool.addElement(new BitwiseStateObject(4613999590356761709L));
        instancePool.addElement(new BitwiseStateObject(-5958123907058260959L));
    }

    @Override
    public long processLongValue(long inputValue) {
        long bitOpResult = applyShiftAndMask(this.stateValue, 8, 55, this.instanceShiftMap, this.instanceBitMasks);
        this.stateValue ^= inputValue ^ this.auxiliaryStateValue;
        if (this.nextTarget != null) {
            this.nextTarget.processLongValue(inputValue);
        }
        return bitOpResult;
    }

    @Override
    public void setLongState(long stateValue) {
        this.auxiliaryStateValue = stateValue;
    }

    @Override
    public void setNextOperationTarget(OperationTarget nextTarget) {
        if (this != nextTarget) {
            if (this.nextTarget == null) {
                this.nextTarget = nextTarget;
            } else {
                this.nextTarget.setNextOperationTarget(nextTarget);
            }
        }
    }

    @Override
    public int[] getIntArrayState() {
        return this.instanceShiftMap;
    }

    @Override
    public boolean isOrderedBefore(OperationTarget other) {
        if (this == other) return true;
        if (!(other instanceof BitwiseStateObject)) return true; // Or false, depending on desired behavior

        BitwiseStateObject otherA8 = (BitwiseStateObject) other;
        long selfBits = extractBitField(56, 63);
        long otherBits = otherA8.extractBitField(56, 63);
        return selfBits <= otherBits;
    }

    @Override
    public int hashCode() {
        return (int) extractBitField(0, 7);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BitwiseStateObject)) return false;
        BitwiseStateObject otherA8 = (BitwiseStateObject) obj;
        long selfBits = extractBitField(0, 55);
        long otherBits = otherA8.extractBitField(0, 55);
        return selfBits == otherBits;
    }

    private long extractBitField(int endBit) {
        // Assuming startBit is always 0 for this overload
        return applyShiftAndMask(this.stateValue, 0, endBit -1, this.instanceShiftMap, this.instanceBitMasks);
    }

    private long extractBitField(int startBit, int endBit) {
        return applyShiftAndMask(this.stateValue, startBit, endBit, this.instanceShiftMap, this.instanceBitMasks);
    }
}
